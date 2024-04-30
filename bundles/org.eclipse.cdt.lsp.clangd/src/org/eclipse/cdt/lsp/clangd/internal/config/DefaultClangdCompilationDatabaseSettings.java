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

package org.eclipse.cdt.lsp.clangd.internal.config;

import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.ClangdCompilationDatabaseSettings;
import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.core.resources.IProject;
import org.osgi.service.component.annotations.Component;

@Component(property = { "service.ranking:Integer=0" })
public class DefaultClangdCompilationDatabaseSettings implements ClangdCompilationDatabaseSettings {

	@Override
	public boolean enableSetCompilationDatabasePath(IProject project) {
		return Optional.ofNullable(LspPlugin.getDefault()).map(LspPlugin::getCLanguageServerProvider)
				.map(provider -> provider.isEnabledFor(project)).orElse(Boolean.FALSE);
	}

}
