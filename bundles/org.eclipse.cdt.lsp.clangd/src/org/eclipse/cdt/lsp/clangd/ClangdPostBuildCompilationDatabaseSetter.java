/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 *
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

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;

/**
 * This interface will be called to set the the compilation database (compile_commands.json) path in the .clangd configuration file.
 * It will be triggered by a post build listener if the setting of the compilation database has been enabled via
 * {@link ClangdCompilationDatabaseSettings#enableSetCompilationDatabasePath(IProject)}.
 * The primary usage are CMake projects which are not covered by the {@link ClangdCProjectDescriptionListener},
 * because {@code ClangdCProjectDescriptionListener} won't be called.
 *
 * @since 3.0
 */
public interface ClangdPostBuildCompilationDatabaseSetter {

	/**
	 * Sets the compilation database path in the .clangd configuration file based on the given IBuildConfiguration.
	 * It will be called if {@link ClangdCompilationDatabaseSettings#enableSetCompilationDatabasePath(IProject)} returns true.
	 * @param configuration
	 */
	void setCompilationDatabase(IBuildConfiguration configuration);
}
