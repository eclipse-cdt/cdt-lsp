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
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.ui.navigator;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.lsp.util.LspUtils;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.outline.SymbolsModel;
import org.eclipse.lsp4e.outline.SymbolsModel.DocumentSymbolWithURI;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;

public class SymbolsManager implements IDeferredWorkbenchAdapter {
	protected static final Object[] EMPTY = new Object[0];
	private final ReentrantLock lock = new ReentrantLock();

	class CompileUnit {
		public final IFile file;
		public final URI uri;
		public final SymbolsModel symbolsModel;
		public volatile boolean isDirty = true;

		public CompileUnit(URI uri, IFile file) {
			this.file = file;
			this.uri = uri;
			this.symbolsModel = new SymbolsModel();
			this.symbolsModel.setUri(uri);
		}

		public Object[] getElements() {
			return symbolsModel.getElements();
		}

		public Object[] getChildren(Object parentElement) {
			return symbolsModel.getChildren(parentElement);
		}
	}

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
						var cachedCompileUnit = getCompileUnit(uri);
						if (cachedCompileUnit != null) {
							cachedCompileUnit.isDirty = true;
						}
					}
				}
			} catch (CoreException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}

		}

		private boolean resourceExists(IFileBuffer buffer) {
			return Optional.ofNullable(LSPEclipseUtils.getFile(buffer.getLocation())).map(file -> file.exists())
					.orElse(false);
		}

		private boolean isCElement(IContentType contentType) {
			if (contentType == null) {
				return false;
			}
			return LspUtils.isCContentType(contentType.getId());
		}
	};

	private volatile HashMap<URI, CompileUnit> cachedSymbols = new HashMap<>();
	public static final SymbolsManager INSTANCE = new SymbolsManager();

	public SymbolsManager() {
		FileBuffers.getTextFileBufferManager().addFileBufferListener(fileBufferListener);
	}

	public void dispose() {
		cachedSymbols.clear();
		FileBuffers.getTextFileBufferManager().removeFileBufferListener(fileBufferListener);
	}

	@Override
	public void fetchDeferredChildren(Object object, IElementCollector collector, IProgressMonitor monitor) {
		var children = getCompileUnitElements(object);
		if (monitor.isCanceled() || children.length == 0) {
			return;
		}
		collector.add(children, monitor);
		collector.done();
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	@Override
	public ISchedulingRule getRule(Object object) {
		if (object instanceof ITranslationUnit unit) {
			return unit.getFile();
		} else if (object instanceof IFile file) {
			return file;
		}
		return null;
	}

	public Object[] getTranslationUnitElements(ITranslationUnit translationUnit) {
		CompileUnit compileUnit = getCompileUnit(translationUnit.getLocationURI());
		if (compileUnit != null) {
			return compileUnit.getElements();
		}
		return null;
	}

	public boolean isDirty(ITranslationUnit translationUnit) {
		CompileUnit compileUnit = getCompileUnit(translationUnit.getLocationURI());
		if (compileUnit != null) {
			return compileUnit.isDirty;
		}
		return true;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof DocumentSymbolWithURI documentSymbolWithUri) {
			CompileUnit unit = getCompileUnit(documentSymbolWithUri.uri);
			if (unit != null) {
				return unit.getChildren(parentElement);
			}
		}
		return EMPTY;
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	@Override
	public String getLabel(Object o) {
		if (o instanceof ITranslationUnit unit) {
			return unit.getElementName();
		}
		return "unkown"; //$NON-NLS-1$
	}

	@Override
	public Object getParent(Object o) {
		return null;
	}

	private CompileUnit getCompileUnit(URI key) {
		return getCompileUnit(key, null);
	}

	private synchronized CompileUnit getCompileUnit(URI key, IFile file) {
		return cachedSymbols.computeIfAbsent(key, uri -> new CompileUnit(key, file));
	}

	private Object[] getCompileUnitElements(Object object) {
		if (object instanceof ITranslationUnit unit) {
			CompileUnit compileUnit = getCompileUnit(unit.getLocationURI(), unit.getFile());
			if (compileUnit == null) {
				return EMPTY;
			}
			refreshTreeContentFromLS(compileUnit);
			return compileUnit.getElements();
		}
		return EMPTY;
	}

	private void refreshTreeContentFromLS(CompileUnit compileUnit) {
		if (compileUnit == null || !compileUnit.isDirty) {
			return;
		}
		lock.lock();
		boolean temporaryLoadedDocument = false;
		try {
			IDocument document = LSPEclipseUtils.getExistingDocument(compileUnit.file);
			if (document == null) {
				document = LSPEclipseUtils.getDocument(compileUnit.file);
				if (document == null) {
					document = LSPEclipseUtils.getDocument(compileUnit.uri);
				}
				temporaryLoadedDocument = true;
			}
			if (document != null) {
				var isTimeoutException = new boolean[1];
				CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> symbols;
				final var params = new DocumentSymbolParams(LSPEclipseUtils.toTextDocumentIdentifier(document));
				CompletableFuture<Optional<LanguageServerWrapper>> languageServer = LanguageServers
						.forDocument(document).withCapability(ServerCapabilities::getDocumentSymbolProvider)
						.computeFirst((w, ls) -> CompletableFuture.completedFuture(w));
				try {
					symbols = languageServer.get(1000, TimeUnit.MILLISECONDS).filter(Objects::nonNull)
							.filter(LanguageServerWrapper::isActive)
							.map(s -> s.execute(ls -> ls.getTextDocumentService().documentSymbol(params)))
							.orElse(CompletableFuture.completedFuture(null));
				} catch (TimeoutException | ExecutionException | InterruptedException e) {
					Platform.getLog(getClass()).error(e.getMessage(), e);
					symbols = CompletableFuture.completedFuture(null);
					if (e instanceof InterruptedException) {
						Thread.currentThread().interrupt();
					} else if (e instanceof TimeoutException) {
						isTimeoutException[0] = true;
					}
				}
				symbols.thenAcceptAsync(response -> {
					compileUnit.symbolsModel.update(response);
					compileUnit.isDirty = isTimeoutException[0]; // reset dirty only when no TimeoutException occurred
				}).join();
			} else {
				temporaryLoadedDocument = false;
				compileUnit.symbolsModel.update(null);
			}
		} catch (Exception e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		} finally {
			if (temporaryLoadedDocument) {
				//Note: the LS will be terminated via the shutdown command by LSP4E, when all documents have been disconnected.
				//This is the case when no file is opened in the LSP based C/C++ editor.
				var file = LSPEclipseUtils.getFileHandle(compileUnit.uri);
				if (file != null) {
					try {
						FileBuffers.getTextFileBufferManager().disconnect(file.getFullPath(), LocationKind.IFILE,
								new NullProgressMonitor());
					} catch (CoreException e) {
						Platform.getLog(getClass()).error(e.getMessage(), e);
					}
				} else {
					try {
						ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
						if (bufferManager != null) {
							bufferManager.disconnectFileStore(EFS.getStore(compileUnit.uri), new NullProgressMonitor());
						}
					} catch (CoreException e) {
						Platform.getLog(getClass()).error(e.getMessage(), e);
					}
				}
			}
			lock.unlock();
		}
	}

}
