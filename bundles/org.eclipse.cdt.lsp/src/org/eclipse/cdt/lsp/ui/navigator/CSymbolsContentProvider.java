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
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.outline.SymbolsModel;
import org.eclipse.lsp4e.outline.SymbolsModel.DocumentSymbolWithFile;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

public class CSymbolsContentProvider extends CNavigatorContentProvider {
	protected final SymbolsModel symbolsModel = new SymbolsModel();
	private volatile CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> symbols;

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
		if (parentElement instanceof DocumentSymbolWithFile) {
			return symbolsModel.getChildren(parentElement);
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
			refreshTreeContentFromLS((IFile) unit.getResource());
			return symbolsModel.getElements();
		}
		return NO_CHILDREN;
	}

	protected void refreshTreeContentFromLS(IFile file) {
		if (symbols != null) {
			symbols.cancel(true);
		}

		boolean temporaryLoadedDocument = false;
		try {
			IDocument document = LSPEclipseUtils.getExistingDocument(file);
			if (document == null) {
				document = LSPEclipseUtils.getDocument(file);
				temporaryLoadedDocument = true;
			}
			if (document != null) {
				final var params = new DocumentSymbolParams(
						LSPEclipseUtils.toTextDocumentIdentifier(file.getLocationURI()));
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
					symbolsModel.setUri(file.getLocationURI());
					symbolsModel.update(response);
				}).join();
			} else {
				temporaryLoadedDocument = false;
				symbolsModel.setUri(file.getLocationURI());
				symbolsModel.update(null);
			}
		} catch (Exception e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		} finally {
			if (temporaryLoadedDocument) {
				try {
					FileBuffers.getTextFileBufferManager().disconnect(file.getFullPath(), LocationKind.IFILE,
							new NullProgressMonitor());
				} catch (CoreException e) {
					Platform.getLog(getClass()).error(e.getMessage(), e);
				}
			}
		}
	}

}
