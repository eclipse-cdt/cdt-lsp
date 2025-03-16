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

package org.eclipse.cdt.lsp.clangd.internal.config;

import java.util.Optional;

import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.lsp.clangd.ClangdPostBuildListener;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(property = { "service.ranking:Integer=0" })
public class ClangdPostBuildListenerHandler extends ClangdConfigurationFileHandlerBase
		implements ClangdPostBuildListener {

	@Reference
	protected ICBuildConfigurationManager build;

	@Override
	public void handleEvent(IBuildConfiguration configuration) {
		setCompilationDatabasePath(configuration);
	}

	protected void setCompilationDatabasePath(IBuildConfiguration configuration) {
		if (configuration != null && configuration.getProject() != null) {
			getConfiguration(configuration) //
					.map(bc -> {
						if (bc instanceof CBuildConfiguration cbc) {
							try {
								var container = cbc.getBuildContainer();
								return container != null && container.exists() ? container : null;
							} catch (CoreException e) {
								Platform.getLog(getClass()).log(e.getStatus());
							}
						}
						return null;
					}) //
					.map(c -> c.getProjectRelativePath().toOSString())
					.ifPresent(relativePath -> setCompilationDatabase(configuration.getProject(), relativePath));
		}
	}

	private Optional<ICBuildConfiguration> getConfiguration(IBuildConfiguration configuration) {
		try {
			return Optional.ofNullable(build.getBuildConfiguration(configuration));
		} catch (CoreException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}
		return Optional.empty();
	}

}
