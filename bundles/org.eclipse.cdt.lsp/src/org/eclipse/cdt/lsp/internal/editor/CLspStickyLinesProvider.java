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
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.cdt.lsp.services.ClangdLanguageServer;
import org.eclipse.cdt.lsp.services.ast.AstNode;
import org.eclipse.cdt.lsp.services.ast.AstParams;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.lsp4e.LSPEclipseUtils;
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

	private AtomicReference<AstNode> fAst = new AtomicReference<>();
	private long fModificationStamp = IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;

	@Override
	public List<IStickyLine> getStickyLines(ISourceViewer sourceViewer, int lineNumber,
			StickyLinesProperties properties) {

		final long startTime = System.currentTimeMillis();
		if (DEBUG) {
			System.out.println("Sticky lines request at source line: " + (lineNumber + 1)); //$NON-NLS-1$
		}

		// determine whether document has been modified since last request
		boolean documentModified = true;
		if (sourceViewer.getDocument() instanceof IDocumentExtension4 document) {
			final long modificationStamp = document.getModificationStamp();
			documentModified = (modificationStamp != fModificationStamp);
			fModificationStamp = modificationStamp;
			if (DEBUG) {
				System.out.println("> Modification stamp: " + fModificationStamp); //$NON-NLS-1$
			}
		}

		// fetch AST on first request or if document modified
		final IEditorPart editor = properties.editor();
		final IDocument document = LSPEclipseUtils.getDocument(editor.getEditorInput());
		if (documentModified) {
			getUri(editor).ifPresent(fileUri -> {
				if (DEBUG) {
					System.out.println("> Fetching AST: " + editor.getEditorInput().toString()); //$NON-NLS-1$
				}
				final AstParams params = new AstParams(new TextDocumentIdentifier(fileUri.toString()));
				LanguageServers.forDocument(document)
						.computeFirst(
								server -> (server instanceof ClangdLanguageServer cls) ? cls.getAst(params) : null)
						.join().ifPresent(fAst::set);
			});
		}

		final List<IStickyLine> stickyLines = new LinkedList<>();
		final CLspStickyLinesProcessor processor = new CLspStickyLinesProcessor(document, fAst.get());
		try {
			processor.calculateStickyLines(lineNumber)
					.forEach(line -> stickyLines.add(new StickyLine(line, sourceViewer)));
		} catch (BadLocationException e) {
			ILog.get().error("Error calculating sticky lines", e); //$NON-NLS-1$
		}

		if (DEBUG) {
			System.out.println("> Sticky line count: " + stickyLines.size()); //$NON-NLS-1$
			System.out.println("> Execution time (ms): " + (System.currentTimeMillis() - startTime)); //$NON-NLS-1$
		}
		return stickyLines;
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
