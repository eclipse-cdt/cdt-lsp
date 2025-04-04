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

/**
 * Vendors may implement this interface as OSGi service
 * with a service.ranking property > 0 to replace the default
 * implementation {@code ClangdConfigurationFileManager}
 *
 * @since 2.0
 * @deprecated use {@link ClangdCompilationDatabaseProvider} instead
 */
@Deprecated
public interface ClangdCProjectDescriptionListener {

	/**
	 * Called when the configuration of a CDT C/C++ project changes.
	 * @param event
	 * @deprecated use {@link ClangdCompilationDatabaseProvider#getCompilationDatabasePath(CProjectDescriptionEvent)}
	 */
	@Deprecated
	void handleEvent(CProjectDescriptionEvent event);

}