/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
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

/**
 * Provides access to the visibility of configuration elements in the UI taking into account the scope (project or workspace).
 * Should be implemented as OSGi service when {@link ClangdEnable} service has been provided
 * to hide the 'Prefer C/C++ Editor (LSP)' check-box.
 */
public interface ClangdConfigurationVisibility {

	/**
	 * Changes the visibility of the 'Prefer C/C++ Editor (LSP)' check-box.
	 * @param isProjectScope
	 * @return true when the check-box should be displayed.
	 */
	boolean showPreferClangd(boolean isProjectScope);

}
