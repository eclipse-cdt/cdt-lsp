/*******************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import org.eclipse.core.resources.IProject;

/**
 * @since 2.2
 */
public interface ClangFormatFile {

	/**
	 * Opens the .clang-format file in the given project. Creates a file with default values, if not yet existing prior to the opening.
	 * @param formatFile
	 */
	void openClangFormatFile(IProject project);

	/**
	 * Creates a new .clang-format file with default settings in the project root directory if not yet existing.
	 * @param project
	 */
	void createClangFormatFile(IProject project);

}