/*******************************************************************************
 * Copyright (c) 2025 Bachmann electronic GmbH and others.
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

package org.eclipse.cdt.lsp.clangd.internal.config;

import java.util.Optional;

import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.lsp.clangd.ClangdCProjectDescriptionListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Default implementation of the {@link ClangdCProjectDescriptionListener}.
 * Can be extended by vendors if needed. This implementation sets the path to
 * the compile_commands.json in the .clangd file in the projects root directory.
 * This is needed by CDT projects since the compile_commands.json is generated in the build folder.
 * When the active build configuration changes in managed build projects, this manager updates the path to the database in
 * the .clangd file to ensure that clangd uses the compile_commads.json of the active build configuration.
 *
 * This class can be extended by vendors.
 */
@Component(property = { "service.ranking:Integer=0" })
public class ClangdCProjectDescriptionListenerHandler extends ClangdConfigurationFileHandlerBase
		implements ClangdCProjectDescriptionListener {

	@Reference
	protected ICBuildConfigurationManager build;

	@Override
	public void handleEvent(CProjectDescriptionEvent event) {
		setCompilationDatabasePath(event.getProject(), event.getNewCProjectDescription());
	}

	/**
	 * Set the <code>CompilationDatabase</code> entry in the <code>.clangd</code> file which is located in the <code>project</code> root,
	 * if the yaml file syntax can be parsed.
	 * The <code>.clangd</code> file will be created, if it's not existing.
	 * The <code>CompilationDatabase</code> points to the build folder of the active build configuration
	 * (in case <code>project</code> is a managed C/C++ project).
	 *
	 * In the following example clangd uses the compile_commands.json file in the Debug folder:
	 * <pre>CompileFlags: {CompilationDatabase: Debug}</pre>
	 *
	 * @param project C/C++ project
	 * @param newCProjectDescription new CProject description
	 */
	protected void setCompilationDatabasePath(IProject project, ICProjectDescription newCProjectDescription) {
		if (project != null && newCProjectDescription != null) {
			var relativeDatabasePath = getRelativeDatabasePath(project, newCProjectDescription);
			if (!relativeDatabasePath.isEmpty()) {
				setCompilationDatabase(project, relativeDatabasePath);
			} else {
				Platform.getLog(getClass()).error("Cannot determine path to compile_commands.json"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Get project relative path to compile_commands.json file.
	 * By de
	 * @param project
	 * @param newCProjectDescription
	 * @return project relative path to active build folder or empty String
	 */
	private String getRelativeDatabasePath(IProject project, ICProjectDescription newCProjectDescription) {
		if (project != null && newCProjectDescription != null) {
			ICConfigurationDescription config = newCProjectDescription.getDefaultSettingConfiguration();
			var cwdBuilder = config.getBuildSetting().getBuilderCWD();
			var projectLocation = project.getLocation().addTrailingSeparator().toOSString();
			if (cwdBuilder != null) {
				try {
					var cwdString = new MacroResolver().resolveValue(cwdBuilder.toOSString(), EMPTY, null, config);
					return cwdString.replace(projectLocation, EMPTY);
				} catch (CdtVariableException e) {
					Platform.getLog(getClass()).log(e.getStatus());
				}
			} else {
				//it is probably a cmake project:
				return buildConfiguration(project)//
						.filter(CBuildConfiguration.class::isInstance)//
						.map(bc -> {
							try {
								return ((CBuildConfiguration) bc).getBuildContainer();
							} catch (CoreException e) {
								Platform.getLog(getClass()).log(e.getStatus());
							}
							return null;
						})//
						.map(c -> c.getLocation())//
						.map(l -> l.toOSString().replace(projectLocation, EMPTY)).orElse(EMPTY);
			}
		}
		return EMPTY;
	}

	private Optional<ICBuildConfiguration> buildConfiguration(IResource initial) {
		try {
			var active = initial.getProject().getActiveBuildConfig();
			if (active != null && build != null) {
				return Optional.ofNullable(build.getBuildConfiguration(active));
			}
		} catch (CoreException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}
		return Optional.empty();
	}

}
