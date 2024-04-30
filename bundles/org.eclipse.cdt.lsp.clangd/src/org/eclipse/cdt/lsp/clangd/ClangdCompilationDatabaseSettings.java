/*******************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import org.eclipse.core.resources.IProject;

public interface ClangdCompilationDatabaseSettings {

	/**
	 * Enabler for {@link org.eclipse.cdt.lsp.clangd.internal.config.ClangdConfigurationFileManager#setCompilationDatabase(IProject, String)}.
	 * Can be overriden for customization.
	 * @param project
	 * @return true if the database path should be written to .clangd file in the project root.
	 */
	boolean enableSetCompilationDatabasePath(IProject project);

}