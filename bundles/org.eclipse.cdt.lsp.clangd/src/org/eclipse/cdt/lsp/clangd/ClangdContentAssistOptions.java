/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/
package org.eclipse.cdt.lsp.clangd;

/**
 * clangd content assist options
 *
 * @since 3.0
 */
public interface ClangdContentAssistOptions {

	/**
	 * When disabled, completions contain only parentheses for function calls. When enabled, completions also contain placeholders for function parameters
	 * @return true if completions shall also contain placeholders for function parameters
	 */
	default boolean fillFunctionArguments() {
		return true;
	}
}
