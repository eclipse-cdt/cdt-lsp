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

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.cdt.lsp.services.ast.AstNode;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.lsp4j.Range;

public class CLspStickyLinesProcessor {

	private static final boolean DEBUG = Boolean
			.parseBoolean(Platform.getDebugOption(LspPlugin.PLUGIN_ID + "/debug/editor/stickyLines")); //$NON-NLS-1$

	private static final String AST_NODE_KIND_COMPOUND = "Compound"; //$NON-NLS-1$
	private static final String AST_NODE_KIND_IF = "If"; //$NON-NLS-1$
	private static final String ARCANUM_HAS_ELSE = "has_else"; //$NON-NLS-1$
	private static final Pattern PATTERN_ELSE_LINE = Pattern.compile(".*(?<!//.*)\\belse\\b.*"); //$NON-NLS-1$

	private final IDocument fDocument;
	private final AstNode fAst;

	public CLspStickyLinesProcessor(IDocument document, AstNode ast) {
		fDocument = document;
		fAst = ast;
	}

	public List<Integer> calculateStickyLines(int lineNumber) {
		final List<Integer> stickyLines = new LinkedList<>();
		traverseEnclosingNodes(fAst, lineNumber, stickyLines);
		return stickyLines;
	}

	private AstNode traverseEnclosingNodes(AstNode node, int lineNumber, List<Integer> stickyLines) {
		final Optional<String> arcana = Optional.ofNullable(node.getArcana());
		if (DEBUG) {
			System.out.println("> Examining AST node: " + arcana.orElse(node.toString())); //$NON-NLS-1$
		}
		final AstNode[] children = node.getChildren();
		if (null != children) {
			for (AstNode child : children) {
				if (rangeEnclosesLine(child.getRange(), lineNumber)) {
					if (AST_NODE_KIND_IF.equals(node.getKind()) && (arcana.orElse("").contains(ARCANUM_HAS_ELSE)) //$NON-NLS-1$
							&& (3 == children.length) && child.equals(children[2])) { // if an else clause
						// locate the else keyword between nodes children[1] and children[2]
						// TODO: improve search to avoid possible erroneous match to 'else' word in block comment
						final int startingLine = children[1].getRange().getEnd().getLine();
						final int endingLine = children[2].getRange().getStart().getLine();
						for (int line = endingLine; line >= startingLine; line--) {
							final String documentLine = getDocumentLine(line);
							if (PATTERN_ELSE_LINE.matcher(documentLine).matches()) { // else keyword is sticky
								addStickyLine(line, stickyLines);
							}
						}
					}
					if (!AST_NODE_KIND_COMPOUND.equals(child.getKind())) { // compound nodes are not sticky
						final int line = child.getRange().getStart().getLine();
						addStickyLine(line, stickyLines);
					}
					return traverseEnclosingNodes(child, lineNumber, stickyLines);
				}
			}
		}
		return node;
	}

	private boolean rangeEnclosesLine(Range range, int lineNumber) {
		return (null != range) && (range.getStart().getLine() < lineNumber) && (range.getEnd().getLine() >= lineNumber);
	}

	private String getDocumentLine(int lineNumber) {
		try {
			final IRegion region = fDocument.getLineInformation(lineNumber);
			return fDocument.get(region.getOffset(), region.getLength());
		} catch (BadLocationException e) {
			if (DEBUG) {
				System.out.println("> Bad document location: line " + (lineNumber + 1)); //$NON-NLS-1$
			}
		}
		return ""; //$NON-NLS-1$
	}

	private void addStickyLine(int lineNumber, List<Integer> stickyLines) {
		// suppress duplicate sticky lines
		if (stickyLines.isEmpty() || (stickyLines.getLast() < lineNumber)) {
			if (DEBUG) {
				System.out.println("> Sticky line: " + (lineNumber + 1)); //$NON-NLS-1$
			}
			stickyLines.add(lineNumber);
		}
	}

}
