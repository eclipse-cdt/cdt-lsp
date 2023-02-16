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
 *******************************************************************************/
package org.eclipse.cdt.lsp.services;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageServer;

/**
 * Interface extending the {@link LanguageServer} with clangd extensions.
 *
 * @see https://clangd.llvm.org/extensions
 */
public interface ClangdLanguageServer extends LanguageServer {

	/**
	 * The switchSourceHeader request is sent from the client to the server to
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
}
