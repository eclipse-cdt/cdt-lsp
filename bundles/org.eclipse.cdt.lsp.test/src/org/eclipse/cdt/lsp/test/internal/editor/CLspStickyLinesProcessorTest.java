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
package org.eclipse.cdt.lsp.test.internal.editor;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;

import org.eclipse.cdt.lsp.internal.editor.CLspStickyLinesProcessor;
import org.eclipse.cdt.lsp.services.ast.AstNode;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.Test;

public class CLspStickyLinesProcessorTest {

	@Test
	public void testStickyLinesProcessor() throws Exception {
		// Test that 'Compound' nodes are not sticky
		final CLspStickyLinesProcessor functionProcessor = new CLspStickyLinesProcessor(getFunctionDocument(),
				getFunctionAst());
		testStickyLines(functionProcessor, 0, List.of(), "Function prototype");
		testStickyLines(functionProcessor, 1, List.of(0), "Start compound statement");
		testStickyLines(functionProcessor, 2, List.of(0), "Function body");
		testStickyLines(functionProcessor, 3, List.of(0), "End compound statement");
		testStickyLines(functionProcessor, 4, List.of(), "After function");

		// Test that 'else' keywords within 'If' nodes are sticky
		final CLspStickyLinesProcessor ifThenElseProcessor = new CLspStickyLinesProcessor(getIfThenElseDocument(),
				getIfThenElseAst());
		testStickyLines(ifThenElseProcessor, 0, List.of(), "If statement");
		testStickyLines(ifThenElseProcessor, 1, List.of(0), "Then clause");
		testStickyLines(ifThenElseProcessor, 2, List.of(0), "Before else keyword");
		testStickyLines(ifThenElseProcessor, 3, List.of(0), "Else keyword");
		testStickyLines(ifThenElseProcessor, 4, List.of(0, 3), "After else keyword");
		testStickyLines(ifThenElseProcessor, 5, List.of(0, 3), "Else clause");
		testStickyLines(ifThenElseProcessor, 6, List.of(), "After if statement");
	}

	private IDocument getFunctionDocument() {
		final String content = """
						void func(void) // Function prototype
						{ // Start compound statement
							return; // Function body
						} // End compound statement
						// After function
				""";
		return new Document(content);
	}

	private AstNode getFunctionAst() {
		final AstNode returnStmt = createAstNode("Return", null, 2, 2);
		final AstNode compound = createAstNode("Compound", null, 1, 3, List.of(returnStmt));
		final AstNode builtin = createAstNode("Builtin", null, 0, 0);
		final AstNode functionProto = createAstNode("FunctionProto", null, 0, 0, List.of(builtin));
		final AstNode function = createAstNode("Function", null, 0, 3, List.of(functionProto, compound));
		final AstNode translationUnit = createAstNode("TranslationUnit", null, 0, 3, List.of(function));
		return translationUnit;
	}

	private IDocument getIfThenElseDocument() {
		final String content = """
						if (0) // If statement
							return; // Then clause
							// Before else keyword
						else // Else keyword
							// After else keyword
							return; // Else clause
						// After if statement
				""";
		return new Document(content);
	}

	private AstNode getIfThenElseAst() {
		final AstNode integerLiteral = createAstNode("IntegerLiteral", null, 0, 0);
		final AstNode thenClause = createAstNode("Return", null, 1, 1);
		final AstNode elseClause = createAstNode("Return", null, 5, 5);
		final AstNode ifStmt = createAstNode("If", "has_else", 0, 5, List.of(integerLiteral, thenClause, elseClause));
		final AstNode compound = createAstNode("Compound", null, 0, 5, List.of(ifStmt));
		return compound;
	}

	private AstNode createAstNode(String kind, String arcana, int startLineNumber, int endLineNumber,
			List<AstNode> children) {
		final Position startPosition = new Position(startLineNumber, 0);
		final Position endPosition = new Position(endLineNumber, 0);
		final AstNode node = new AstNode();
		node.setArcana(arcana);
		node.setChildren(children.stream().toArray(AstNode[]::new));
		node.setKind(kind);
		node.setRange(new Range(startPosition, endPosition));
		return node;
	}

	private AstNode createAstNode(String kind, String arcana, int startLineNumber, int endLineNumber) {
		return createAstNode(kind, arcana, startLineNumber, endLineNumber, List.of());
	}

	private static void testStickyLines(CLspStickyLinesProcessor processor, int sourceLine, List<Integer> expected,
			String message) {
		assertIterableEquals(expected, processor.calculateStickyLines(sourceLine), message);
	}

}
