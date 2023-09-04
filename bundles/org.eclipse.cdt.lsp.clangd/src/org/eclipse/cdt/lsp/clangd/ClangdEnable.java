/*******************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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
 * Should be implemented by bundles as OSGi service to enable the Language
 * Server and LSP based C/C++ Editor via project properties.
 */
public interface ClangdEnable {

	/**
	 * Checks whether clangd and the LSP based C/C++ Editor should be enabled for the given project.
	 * The enable can be linked with certain project properties (e.g. project natures).
	 * @param project
	 * @return true when clangd and LSP based editor should be enabled for the given project
	 */
	public boolean isEnabledFor(IProject project);
}
