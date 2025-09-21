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

package org.eclipse.cdt.lsp.clangd.format;

import java.util.Optional;

import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.core.resources.IProject;

public class CLanguageServerCheckEnabledProvider {

	/**
	 * Checks if the language server is enabled for the given project.
	 *
	 * @param project
	 * @return true if the language server is enabled for the project.
	 */
	boolean isEnabledFor(IProject project) {
		return Optional.ofNullable(LspPlugin.getDefault()).map(LspPlugin::getCLanguageServerProvider)
				.map(provider -> provider.isEnabledFor(project)).orElse(Boolean.FALSE);
	}
}
