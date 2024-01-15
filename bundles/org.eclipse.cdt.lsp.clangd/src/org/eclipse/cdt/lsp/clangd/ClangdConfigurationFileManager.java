/*******************************************************************************
 * Copyright (c) 2024 Bachmann electronic GmbH and others.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;

import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.LspUtils;
import org.eclipse.cdt.lsp.internal.clangd.editor.ClangdPlugin;
import org.eclipse.cdt.lsp.internal.clangd.editor.LspEditorUiMessages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

/**
 * Default implementation of the {@link ClangdCProjectDescriptionListener}.
 * Can be replaced by vendors if needed. This implementation sets the path to
 * the compile_commands.json in the .clangd file in the projects root directory.
 * This is needed by CDT projects since the compile_commands.json is generated in the build folder.
 * When the active build configuration changes in managed build projects, this manager updates the path to the database in
 * the .clangd file to ensure that clangd uses the compile_commads.json of the active build configuration.
 *
 * This class can be extended by vendors.
 */
@Component(property = { "service.ranking:Integer=0" })
public class ClangdConfigurationFileManager implements ClangdCProjectDescriptionListener {
	private static final String COMPILE_FLAGS = "CompileFlags"; //$NON-NLS-1$
	private static final String COMPILATTION_DATABASE = "CompilationDatabase"; //$NON-NLS-1$
	private static final String SET_COMPILATION_DB = COMPILE_FLAGS + ": {" + COMPILATTION_DATABASE + ": %s}"; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String EMPTY = ""; //$NON-NLS-1$

	@Reference
	private ICBuildConfigurationManager build;

	@Override
	public void handleEvent(CProjectDescriptionEvent event, MacroResolver macroResolver) {
		setCompilationDatabasePath(event.getProject(), event.getNewCProjectDescription(), macroResolver);
	}

	@Override
	public void setCompilationDatabasePath(IProject project, ICProjectDescription newCProjectDescription,
			MacroResolver macroResolver) {
		if (project != null && newCProjectDescription != null) {
			if (enableSetCompilationDatabasePath(project)) {
				var relativeDatabasePath = getRelativeDatabasePath(project, newCProjectDescription, macroResolver);
				if (!relativeDatabasePath.isEmpty()) {
					try {
						setCompilationDatabase(project, relativeDatabasePath);
					} catch (ScannerException e) {
						var status = new Status(IStatus.ERROR, ClangdPlugin.PLUGIN_ID, e.getMessage());
						var projectLocation = project.getLocation().addTrailingSeparator().toPortableString();
						LspUtils.showErrorMessage(LspEditorUiMessages.CProjectChangeMonitor_yaml_scanner_error,
								LspEditorUiMessages.CProjectChangeMonitor_yaml_scanner_error_message + projectLocation
										+ CLANGD_CONFIG_FILE_NAME,
								status);
					}
				} else {
					Platform.getLog(getClass()).log(new Status(Status.ERROR, ClangdPlugin.PLUGIN_ID,
							"Cannot determine path to compile_commands.json")); //$NON-NLS-1$
				}
			}
		}
	}

	@Override
	public boolean enableSetCompilationDatabasePath(IProject project) {
		return Optional.ofNullable(LspPlugin.getDefault()).map(LspPlugin::getCLanguageServerProvider)
				.map(provider -> provider.isEnabledFor(project)).orElse(Boolean.FALSE);
	}

	/**
	 * Get project relative path to compile_commands.json file.
	 * By de
	 * @param project
	 * @param newCProjectDescription
	 * @param macroResolver
	 * @return project relative path to active build folder or empty String
	 */
	private String getRelativeDatabasePath(IProject project, ICProjectDescription newCProjectDescription,
			MacroResolver macroResolver) {
		if (project != null && newCProjectDescription != null) {
			ICConfigurationDescription config = newCProjectDescription.getDefaultSettingConfiguration();
			var cwdBuilder = config.getBuildSetting().getBuilderCWD();
			var projectLocation = project.getLocation().addTrailingSeparator().toOSString();
			if (cwdBuilder != null) {
				try {
					var cwdString = macroResolver.resolveValue(cwdBuilder.toOSString(), EMPTY, null, config);
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

	/**
	 * Set the <code>CompilationDatabase</code> entry in the .clangd file in the given project root.
	 * The file will be created, if it's not existing.
	 * A ScannerException will be thrown if the configuration file contains invalid yaml syntax.
	 *
	 * @param project to write the .clangd file
	 * @param databasePath project relative path to .clangd file
	 * @throws IOException
	 * @throws ScannerException
	 * @throws CoreException
	 */
	@SuppressWarnings("unchecked")
	public void setCompilationDatabase(IProject project, String databasePath) {
		var configFile = project.getFile(CLANGD_CONFIG_FILE_NAME);
		try {
			if (createClangdConfigFile(configFile, project.getDefaultCharset(), databasePath, false)) {
				return;
			}
			Map<String, Object> data = null;
			Yaml yaml = new Yaml();
			try (var inputStream = configFile.getContents()) {
				//throws ScannerException
				data = yaml.load(inputStream);
			}
			if (data == null) {
				//empty file: (re)create .clangd file:
				createClangdConfigFile(configFile, project.getDefaultCharset(), databasePath, true);
				return;
			}
			Map<String, Object> map = (Map<String, Object>) data.get(COMPILE_FLAGS);
			if (map != null) {
				var cdb = map.get(COMPILATTION_DATABASE);
				if (cdb != null && cdb instanceof String) {
					if (cdb.equals(databasePath)) {
						return;
					}
				}
				map.put(COMPILATTION_DATABASE, databasePath);
				data.put(COMPILE_FLAGS, map);
				try (var yamlWriter = new PrintWriter(configFile.getLocation().toFile())) {
					yaml.dump(data, yamlWriter);
				}
			}
		} catch (CoreException e) {
			Platform.getLog(getClass()).log(e.getStatus());
		} catch (IOException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}
	}

	private boolean createClangdConfigFile(IFile configFile, String charset, String databasePath,
			boolean overwriteContent) {
		if (!configFile.exists() || overwriteContent) {
			try (final var data = new ByteArrayInputStream(
					String.format(SET_COMPILATION_DB, databasePath).getBytes(charset))) {
				if (overwriteContent) {
					configFile.setContents(data, IResource.KEEP_HISTORY, new NullProgressMonitor());
				} else {
					configFile.create(data, false, new NullProgressMonitor());
				}
				return true;
			} catch (CoreException e) {
				Platform.getLog(getClass()).log(e.getStatus());
			} catch (IOException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}
		}
		return false;
	}

}
