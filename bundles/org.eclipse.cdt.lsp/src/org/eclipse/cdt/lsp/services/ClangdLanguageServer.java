/*******************************************************************************
 * Copyright (c) 2023 COSEDA Technologies GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Dominic Scharfe (COSEDA Technologies GmbH) - initial implementation
 * Dietrich Travkin (Solunar GmbH) - extensions for AST and symbol info
 *******************************************************************************/
package org.eclipse.cdt.lsp.services;

import java.util.concurrent.CompletableFuture;

import org.eclipse.cdt.lsp.services.ast.AstNode;
import org.eclipse.cdt.lsp.services.ast.AstParams;
import org.eclipse.cdt.lsp.services.symbolinfo.SymbolDetails;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Interface extending the {@link LanguageServer} with clangd extensions.
 * More details about LSP usage and extension see the
 * <a href="https://github.com/eclipse-lsp4j/lsp4j/blob/main/documentation/jsonrpc.md">
 * org.eclipse.lsp4j project's documentation</a>.
 *
 * @see https://clangd.llvm.org/extensions
 */
public interface ClangdLanguageServer extends LanguageServer {

	/**
	 * The <em>textDocument/switchSourceHeader</em> request is sent from the client to the server to
	 * <ul>
	 * <li>get the corresponding header if a source file was provided</li>
	 * <li>get the source file if a header was provided</li>
	 * </ul>
	 *
	 * @param textDocument open file
	 * @return URI of the corresponding header/source file
	 *
	 * @see https://clangd.llvm.org/extensions#switch-between-sourceheader
	 */
	@JsonRequest(value = "textDocument/switchSourceHeader")
	CompletableFuture<String> switchSourceHeader(TextDocumentIdentifier textDocument);

	/**
	 * The <em>textDocument/ast</em> request is sent from the client to the server in order to get
	 * details about the program structure (so called abstract syntax tree or AST) in a C++ file.
	 * The structure can be requested for the whole file or for a certain range.
	 *
	 * @param astParameters request parameters containing the document identifier and requested documented range
	 * @return the abstract syntax tree root node (with child hierarchy) for the requested document and range
	 *
	 * @see https://clangd.llvm.org/extensions#ast
	 */
	@JsonRequest(value = "textDocument/ast")
	CompletableFuture<AstNode> getAst(AstParams astParameters);

	/**
	 * The <em>textDocument/symbolInfo</em> request is sent from the client to the server in order to access
	 * details about the element under the cursor. The response provides details like the element's name,
	 * its parent container's name, and some clangd-specific element IDs (e.g. the "unified symbol resolution"
	 * identifier).
	 *
	 * @param positionParameters request parameters containing the document identifier and the current cursor position
	 * @return the details about the symbol on the given position
	 *
	 * @see https://clangd.llvm.org/extensions#symbol-info-request
	 */
	@JsonRequest(value = "textDocument/symbolInfo")
	CompletableFuture<SymbolDetails[]> getSymbolInfo(TextDocumentPositionParams positionParameters);
}
