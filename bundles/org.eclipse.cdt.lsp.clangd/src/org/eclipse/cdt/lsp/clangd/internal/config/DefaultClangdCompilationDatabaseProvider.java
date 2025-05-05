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
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
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
	public Optional<String> getCompilationDatabasePath(IResourceChangeEvent event, IProject project) {
		if (project != null && !isClangdFileInParentFolders(project)) {
			return getConfiguration(project) //
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
		if (event.getProject() != null && !isClangdFileInParentFolders(event.getProject())
				&& event.getNewCProjectDescription() != null) {
			ICConfigurationDescription config = event.getNewCProjectDescription().getDefaultSettingConfiguration();
			var cwdBuilder = config.getBuildSetting().getBuilderCWD();
			if (cwdBuilder != null) {
				try {
					var cwdString = new MacroResolver().resolveValue(cwdBuilder.toOSString(), EMPTY, null, config);
					return Optional.ofNullable(event.getProject().getLocation())
							.map(loc -> loc.addTrailingSeparator().toOSString())
							.map(projectLoc -> cwdString.replace(projectLoc, EMPTY));
				} catch (CdtVariableException e) {
					Platform.getLog(getClass()).log(e.getStatus());
				}
			}
		}
		return Optional.empty();
	}

	private Optional<ICBuildConfiguration> getConfiguration(IProject project) {
		try {
			return Optional.ofNullable(project.getActiveBuildConfig()).map(configuration -> {
				try {
					return build.getBuildConfiguration(configuration);
				} catch (CoreException e) {
					Platform.getLog(getClass()).error(e.getMessage(), e);
					return null;
				}
			});
		} catch (CoreException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}
		return Optional.empty();
	}

	/**
	 * Check if .clangd file is not in the project root but in one of its parent folders.
	 * All parent folders until the root directory of the file system are being searched.
	 * This covers the use case that the compile_commands.json is located in a projects parent folder as well as the .clangd file.
	 * @param project
	 * @return true if .clangd is not in project root directory and in one of its parent folders.
	 */
	private boolean isClangdFileInParentFolders(IProject project) {
		if (project.getFile(ClangdCompilationDatabaseSetterBase.CLANGD_CONFIG_FILE_NAME).exists()) {
			return false;
		}
		//Okay, lets start in parent folder, if it exists, to look for .clangd:
		IFileStore currentDirStore = EFS.getLocalFileSystem().getStore(project.getLocation()).getParent();
		if (currentDirStore == null) {
			return false;
		}
		IFileStore clangdFileStore = currentDirStore
				.getChild(ClangdCompilationDatabaseSetterBase.CLANGD_CONFIG_FILE_NAME);

		while (!clangdFileStore.fetchInfo().exists() && currentDirStore.getParent() != null
				&& currentDirStore.getParent().fetchInfo().exists()) {
			// move up one level to the parent directory and check again
			currentDirStore = currentDirStore.getParent();
			clangdFileStore = currentDirStore.getChild(ClangdCompilationDatabaseSetterBase.CLANGD_CONFIG_FILE_NAME);
		}

		if (clangdFileStore.fetchInfo().exists()) {
			return true;
		}
		return false;
	}
}
