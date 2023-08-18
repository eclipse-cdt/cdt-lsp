/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 * Alexander Fedorov (ArSysOp) - use Platform for logging
 *******************************************************************************/

package org.eclipse.cdt.lsp.ui.navigator;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.navigator.CNavigatorContentProvider;
import org.eclipse.cdt.lsp.LspUtils;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.outline.SymbolsModel;
import org.eclipse.lsp4e.outline.SymbolsModel.DocumentSymbolWithURI;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public class CSymbolsContentProvider extends CNavigatorContentProvider {

	class SymbolsContainer {
		public final IFile file;
		public final SymbolsModel symbolsModel;
		public boolean isDirty = true;

		SymbolsContainer(IFile file) {
			this.file = file;
			this.symbolsModel = new SymbolsModel();
			this.symbolsModel.setUri(file.getLocationURI());
		}
	}

	private volatile CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> symbols;
	private HashMap<URI, SymbolsContainer> cachedSymbols = new HashMap<>();

	private final IFileBufferListener fileBufferListener = new FileBufferListenerAdapter() {

		@Override
		public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
			try {
				// Note: resourceExists must be called prior to buffer.getContentType(),
				// because getContentType() throws an exception when the underlying resource does not exists.
				// This can be the case if 'Save as..' has been performed on a file.
				// Then isDirty is true and dirtyStateChanged gets called for the new, not yet existing file.
				if (isDirty && resourceExists(buffer) && isCElement(buffer.getContentType())) {
					var uri = LSPEclipseUtils.toUri(buffer);
					if (uri != null) {
						var cachedSymbol = cachedSymbols.get(uri);
						if (cachedSymbol != null) {
							cachedSymbol.isDirty = true;
						}
					}
				}
			} catch (CoreException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}

		}

		private boolean resourceExists(IFileBuffer buffer) {
			return Optional.ofNullable(buffer.getLocation()).map(l -> l.toFile().exists()).orElse(false);
		}

		private boolean isCElement(IContentType contentType) {
			if (contentType == null) {
				return false;
			}
			return LspUtils.isCContentType(contentType.getId());
		}
	};

	public CSymbolsContentProvider() {
		FileBuffers.getTextFileBufferManager().addFileBufferListener(fileBufferListener);
	}

	@Override
	public void dispose() {
		FileBuffers.getTextFileBufferManager().removeFileBufferListener(fileBufferListener);
		super.dispose();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getPipelinedChildren(Object parent, Set currentChildren) {
		if (parent instanceof ITranslationUnit) {
			//remove children from other provider first:
			currentChildren.clear();
			for (Object child : getChildren(parent)) {
				if (child != null) {
					currentChildren.add(child);
				}
			}
		}
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof DocumentSymbolWithURI) {
			var file = LspUtils.getFile(((DocumentSymbolWithURI) parentElement).uri);
			if (file.isPresent()) {
				refreshTreeContentFromLS(getSymbolsContainer(file.get()));
				var symbolsContainer = cachedSymbols.get(file.get().getLocationURI());
				if (symbolsContainer != null) {
					return symbolsContainer.symbolsModel.getChildren(parentElement);
				}
			}
		} else if (parentElement instanceof ITranslationUnit) {
			try {
				return getTranslationUnitChildren((ITranslationUnit) parentElement);
			} catch (CModelException e) {
			}
		}
		return NO_CHILDREN;
	}

	@Override
	protected Object[] getTranslationUnitChildren(ITranslationUnit unit) throws CModelException {
		if (unit.getResource() instanceof IFile) {
			var file = (IFile) unit.getResource();
			refreshTreeContentFromLS(getSymbolsContainer(file));
			var symbolsContainer = cachedSymbols.get(file.getLocationURI());
			if (symbolsContainer != null)
				return symbolsContainer.symbolsModel.getElements();
		}
		return NO_CHILDREN;
	}

	protected SymbolsContainer getSymbolsContainer(IFile file) {
		SymbolsContainer symbolsContainer = cachedSymbols.get(file.getLocationURI());
		if (symbolsContainer == null) {
			symbolsContainer = new SymbolsContainer(file);
			cachedSymbols.put(file.getLocationURI(), symbolsContainer);
		}
		return symbolsContainer;
	}

	protected void refreshTreeContentFromLS(SymbolsContainer symbolsContainer) {
		if (symbolsContainer == null || !symbolsContainer.isDirty) {
			return;
		}
		boolean temporaryLoadedDocument = false;

		try {
			IDocument document = LSPEclipseUtils.getExistingDocument(symbolsContainer.file);
			if (document == null) {
				document = LSPEclipseUtils.getDocument(symbolsContainer.file);
				temporaryLoadedDocument = true;
			}
			if (document != null) {
				final var params = new DocumentSymbolParams(
						LSPEclipseUtils.toTextDocumentIdentifier(symbolsContainer.file.getLocationURI()));
				CompletableFuture<Optional<LanguageServerWrapper>> languageServer = LanguageServers
						.forDocument(document)
						.withFilter(
								capabilities -> LSPEclipseUtils.hasCapability(capabilities.getDocumentSymbolProvider()))
						.computeFirst((w, ls) -> CompletableFuture.completedFuture(w));
				try {
					symbols = languageServer.get(500, TimeUnit.MILLISECONDS).filter(Objects::nonNull)
							.filter(LanguageServerWrapper::isActive)
							.map(s -> s.execute(ls -> ls.getTextDocumentService().documentSymbol(params)))
							.orElse(CompletableFuture.completedFuture(null));
				} catch (TimeoutException | ExecutionException | InterruptedException e) {
					Platform.getLog(getClass()).error(e.getMessage(), e);
					symbols = CompletableFuture.completedFuture(null);
					if (e instanceof InterruptedException) {
						Thread.currentThread().interrupt();
					}
				}
				symbols.thenAcceptAsync(response -> {
					symbolsContainer.symbolsModel.update(response);
					symbolsContainer.isDirty = false;
				}).join();
			} else {
				temporaryLoadedDocument = false;
				symbolsContainer.symbolsModel.update(null);
			}
		} catch (Exception e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		} finally {
			if (temporaryLoadedDocument) {
				//Note: the LS will be terminated via the shutdown command by LSP4E, when all documents have been disconnected.
				//This is the case when no file is opened in the LSP based C/C++ editor.
				try {
					FileBuffers.getTextFileBufferManager().disconnect(symbolsContainer.file.getFullPath(),
							LocationKind.IFILE, new NullProgressMonitor());
				} catch (CoreException e) {
					Platform.getLog(getClass()).error(e.getMessage(), e);
				}
			}
		}
	}

}
