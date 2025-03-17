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
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.lsp.clangd.ClangdCompilationDatabaseProvider;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Default implementation of the {@link ClangdCompilationDatabaseProvider}.
 * Can be replaced by vendors if needed. This implementation provides the path to
 * the folder which contains the compile_commands.json.
 * The path is used to set the <code>CompilationDatabase</code> setting in the .clangd file in the projects root directory.
 * This is needed by CDT projects since the compile_commands.json is generated in the build folder.
 * When the active build configuration changes in managed build projects or the selected launch for CMake projects,
 * this provider determines the path to the folder which contains the compile_commands.json.
 */
@Component(property = { "service.ranking:Integer=0" })
public class DefaultClangdCompilationDatabaseProvider implements ClangdCompilationDatabaseProvider {
	protected static final String EMPTY = ""; //$NON-NLS-1$

	@Reference
	protected ICBuildConfigurationManager build;

	@Override
	public Optional<String> getCompilationDatabasePath(IBuildConfiguration configuration) {
		if (configuration != null && configuration.getProject() != null) {
			return getConfiguration(configuration) //
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
					.map(c -> c.getProjectRelativePath().toOSString());
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> getCompilationDatabasePath(CProjectDescriptionEvent event) {
		if (event.getProject() != null && event.getNewCProjectDescription() != null) {
			ICConfigurationDescription config = event.getNewCProjectDescription().getDefaultSettingConfiguration();
			var cwdBuilder = config.getBuildSetting().getBuilderCWD();
			if (cwdBuilder != null) {
				try {
					var cwdString = new MacroResolver().resolveValue(cwdBuilder.toOSString(), EMPTY, null, config);
					return Optional.of(cwdString
							.replace(event.getProject().getLocation().addTrailingSeparator().toOSString(), EMPTY));
				} catch (CdtVariableException e) {
					Platform.getLog(getClass()).log(e.getStatus());
				}
			}
		}
		return Optional.empty();
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
