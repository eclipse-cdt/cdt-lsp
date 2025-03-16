/*******************************************************************************
 * Copyright (c) 2024, 2025 Bachmann electronic GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd;

import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.core.resources.IProject;

/**
 * Vendors may implement this interface as OSGi service
 * with a service.ranking property > 0 to replace the default
 * implementation {@code DefaultClangdCompilationDatabaseSetter}
 *
 * @since 2.0
 */
public interface ClangdCProjectDescriptionListener {

	/**
	 * Called when the configuration of a managed CDT C/C++ project changes and the setting of the compilation database path in the
	 * .clangd configuration file has been enabled via {@link ClangdCompilationDatabaseSettings#enableSetCompilationDatabasePath(IProject)}.
	 * @param event
	 */
	void handleEvent(CProjectDescriptionEvent event);

}