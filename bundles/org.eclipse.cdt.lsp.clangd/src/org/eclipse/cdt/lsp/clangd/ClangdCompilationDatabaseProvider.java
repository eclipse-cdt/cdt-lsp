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

import java.util.Optional;

import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IProject;

/**
 * This interface will be called to set the the compilation database (compile_commands.json) path in the .clangd configuration file.
 *
 * @since 3.0
 */
public interface ClangdCompilationDatabaseProvider {

	/**
	 * Gets the project relative path to the folder which contains the compilation database (compile_commands.json) based on the given IBuildConfiguration.
	 * It will be called if {@link ClangdCompilationDatabaseSettings#enableSetCompilationDatabasePath(IProject)} returns true.
	 * @param configuration
	 * @return project relative path to compilation database (compile_commands.json) or empty optional
	 */
	Optional<String> getCompilationDatabasePath(IBuildConfiguration configuration);

	/**
	 * Gets the project relative path to the folder which contains the compilation database (compile_commands.json) based on the given CProjectDescriptionEvent.
	 * It will be called if {@link ClangdCompilationDatabaseSettings#enableSetCompilationDatabasePath(IProject)} returns true.
	 * @param event
	 * @return project relative path to compilation database (compile_commands.json) or empty optional
	 */
	Optional<String> getCompilationDatabasePath(CProjectDescriptionEvent event);
}
