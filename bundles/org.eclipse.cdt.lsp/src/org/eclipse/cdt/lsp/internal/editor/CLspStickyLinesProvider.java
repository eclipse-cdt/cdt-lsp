/*******************************************************************************
 * Copyright (c) 2026 John Dallaway and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Dallaway - initial implementation (#632)
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.editor;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.cdt.lsp.services.ClangdLanguageServer;
import org.eclipse.cdt.lsp.services.ast.AstNode;
import org.eclipse.cdt.lsp.services.ast.AstParams;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.texteditor.stickyscroll.IStickyLine;
import org.eclipse.ui.texteditor.stickyscroll.IStickyLinesProvider;
import org.eclipse.ui.texteditor.stickyscroll.StickyLine;

public class CLspStickyLinesProvider implements IStickyLinesProvider {

	private static final boolean DEBUG = Boolean
			.parseBoolean(Platform.getDebugOption(LspPlugin.PLUGIN_ID + "/debug/editor/stickyLines")); //$NON-NLS-1$

	private record AstRecord(AstNode node, long stamp) {
	}

	// the most recent successful AST fetch
	private AtomicReference<AstRecord> fAstCache = new AtomicReference<>();

	// the modification stamp of the most recent AST fetch request
	private AtomicLong fAstRequestStamp = new AtomicLong(IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP);

	public CLspStickyLinesProvider() {
		fAstCache.set(new AstRecord(null, IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP));
	}

	@Override
	public List<IStickyLine> getStickyLines(ISourceViewer sourceViewer, int lineNumber,
			StickyLinesProperties properties) {

		final long startTime = System.currentTimeMillis();
		if (DEBUG) {
			System.out.println("Sticky lines request at source line: " + (lineNumber + 1)); //$NON-NLS-1$
		}

		// get document modification stamp
		final IEditorPart editor = properties.editor();
		final IDocument document = sourceViewer.getDocument();
		final long documentStamp = getModificationStamp(document);

		// use the cached AST for this sticky lines request for performance reasons
		// the cached AST may be stale if the document has been modified since the
		// preceding sticky lines request but it will deliver correct sticky lines
		// in the majority case where document changes between requests are small
		final AstRecord ast = fAstCache.get();
		if (DEBUG) {
			System.out.println("> Document stamp: " + documentStamp); //$NON-NLS-1$
			System.out.println("> Cached AST stamp: " + ast.stamp()); //$NON-NLS-1$
		}

		// if AST cache is stale, trigger async AST fetch to update it for subsequent sticky lines requests
		getUri(editor).ifPresent(fileUri -> {
			if ((IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP == documentStamp)
					|| (documentStamp != fAstRequestStamp.getAndSet(documentStamp))) {
				if (DEBUG) {
					System.out.println("> Fetching AST (async): " + editor.getEditorInput().toString()); //$NON-NLS-1$
				}
				final AstParams params = new AstParams(new TextDocumentIdentifier(fileUri.toString()));
				LanguageServers.forDocument(document)
						.computeFirst(
								server -> (server instanceof ClangdLanguageServer cls) ? cls.getAst(params) : null)
						.thenAccept(newAst -> setAstCache(newAst.orElse(null), documentStamp));
			}
		});

		final List<IStickyLine> stickyLines = new LinkedList<>();
		if (null != ast.node()) {
			final CLspStickyLinesProcessor processor = new CLspStickyLinesProcessor(document, ast.node());
			processor.calculateStickyLines(lineNumber)
					.forEach(line -> stickyLines.add(new StickyLine(line, sourceViewer)));
		}
		if (DEBUG) {
			System.out.println("> Sticky line count: " + stickyLines.size()); //$NON-NLS-1$
			System.out.println("> Execution time (ms): " + (System.currentTimeMillis() - startTime)); //$NON-NLS-1$
		}
		return stickyLines;
	}

	private synchronized void setAstCache(AstNode node, long stamp) {
		if (null != node) { // if async AST fetch succeeded
			if (fAstCache.get().stamp() < stamp) { // if new AST more recent than cache
				// use new AST for future sticky lines processing
				fAstCache.set(new AstRecord(node, stamp));
			} // otherwise silently discard this superseded AST
		} else { // async AST fetch request failed
			// force retry if no further AST fetches requested since this one
			fAstRequestStamp.compareAndSet(stamp, IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP);
		}
	}

	private static long getModificationStamp(IDocument document) {
		return document instanceof IDocumentExtension4 doc4 ? doc4.getModificationStamp()
				: IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
	}

	private static Optional<URI> getUri(IEditorPart editor) {
		final IEditorInput editorInput = editor.getEditorInput();
		if (editorInput instanceof IFileEditorInput fileEditorInput) {
			return Optional.of(fileEditorInput.getFile().getLocationURI());
		} else if (editorInput instanceof IURIEditorInput uriEditorInput) {
			return Optional.of(uriEditorInput.getURI());
		} else {
			return Optional.empty();
		}
	}

}
