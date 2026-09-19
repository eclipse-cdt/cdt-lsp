/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.cdt.lsp.clangd.ClangdCompilationDatabaseProvider;
import org.eclipse.cdt.lsp.clangd.ClangdCompilationDatabaseSettings;
import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.clangd.internal.config.ClangdCompilationDatabaseStatus.Source;
import org.eclipse.cdt.lsp.clangd.internal.ui.LspEditorUiMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ServiceCaller;

public final class ClangdCompilationDatabaseSupport extends ClangdCompilationDatabaseSetterBase {
	private static final String COMPILE_COMMANDS_JSON = "compile_commands.json"; //$NON-NLS-1$

	private final ServiceCaller<ClangdConfiguration> configuration = new ServiceCaller<>(getClass(),
			ClangdConfiguration.class);

	private final ServiceCaller<ClangdCompilationDatabaseProvider> provider = new ServiceCaller<>(getClass(),
			ClangdCompilationDatabaseProvider.class);

	private final ServiceCaller<ClangdCompilationDatabaseSettings> settings = new ServiceCaller<>(getClass(),
			ClangdCompilationDatabaseSettings.class);

	public Optional<WorkspaceJob> synchronize(IProject project) {
		return synchronize(project, () -> automaticDirectory(project));
	}

	public Optional<WorkspaceJob> synchronize(IProject project, Optional<String> automaticDirectory) {
		return synchronize(project, () -> automaticDirectory);
	}

	Optional<WorkspaceJob> synchronize(IProject project, Supplier<Optional<String>> automaticDirectorySupplier) {
		if (project == null) {
			return Optional.empty();
		}
		if (!isAutomaticManagementEnabled(project)) {
			return clearCompilationDatabase(project);
		}
		return configuredDirectory(project, automaticDirectorySupplier).map(path -> setCompilationDatabase(project, path))
				.or(() -> clearCompilationDatabase(project));
	}

	public ClangdCompilationDatabaseStatus status(IProject project) {
		if (project == null) {
			return new ClangdCompilationDatabaseStatus(Source.NONE, "", "", "", false, false, ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		return status(project, isAutomaticManagementEnabled(project), manualOverride(project).orElse("")); //$NON-NLS-1$
	}

	public ClangdCompilationDatabaseStatus status(IProject project, boolean automaticManagementEnabled,
			String manualOverridePath) {
		if (project == null) {
			return new ClangdCompilationDatabaseStatus(Source.NONE, "", "", "", false, false, ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		Optional<String> manualDirectory = Optional.ofNullable(manualOverridePath).map(String::trim)
				.filter(value -> !value.isBlank());
		Optional<String> automaticDirectory = automaticManagementEnabled && manualDirectory.isEmpty()
				? automaticDirectory(project)
				: Optional.empty();
		Source source = manualDirectory.isPresent() ? Source.MANUAL
				: automaticDirectory.isPresent() ? Source.AUTOMATIC : Source.NONE;
		String configuredDirectory = manualDirectory.or(() -> automaticDirectory).orElse(""); //$NON-NLS-1$
		String compileCommandsPath = compileCommandsPath(project, configuredDirectory).orElse(""); //$NON-NLS-1$
		boolean exists = !compileCommandsPath.isBlank()
				&& Files.isRegularFile(Path.fromOSString(compileCommandsPath).toFile().toPath());
		String buildConfiguration = activeBuildConfiguration(project);
		String message = message(project, automaticManagementEnabled, manualDirectory, automaticDirectory, exists,
				compileCommandsPath);
		return new ClangdCompilationDatabaseStatus(source, configuredDirectory, compileCommandsPath, buildConfiguration,
				automaticManagementEnabled, exists, message);
	}

	private Optional<String> configuredDirectory(IProject project, Supplier<Optional<String>> automaticDirectorySupplier) {
		return manualOverride(project).or(automaticDirectorySupplier).map(String::trim).filter(path -> !path.isBlank());
	}

	private Optional<String> manualOverride(IProject project) {
		String[] path = { "" }; //$NON-NLS-1$
		configuration.call(c -> path[0] = c.options(project).compilationDatabaseOverride());
		return Optional.ofNullable(path[0]).map(String::trim).filter(value -> !value.isBlank());
	}

	private Optional<String> automaticDirectory(IProject project) {
		var detected = new AtomicReference<>(Optional.<String>empty());
		provider.call(p -> detected.set(p.getCompilationDatabasePath(project)));
		return detected.get();
	}

	private boolean isAutomaticManagementEnabled(IProject project) {
		boolean[] enabled = new boolean[1];
		settings.call(s -> enabled[0] = s.enableSetCompilationDatabasePath(project));
		return enabled[0];
	}

	private Optional<String> compileCommandsPath(IProject project, String configuredDirectory) {
		if (configuredDirectory == null || configuredDirectory.isBlank() || project.getLocation() == null) {
			return Optional.empty();
		}
		var directory = Path.fromOSString(configuredDirectory);
		var absolute = directory.isAbsolute() ? directory : project.getLocation().append(directory);
		return Optional.of(absolute.append(COMPILE_COMMANDS_JSON).toOSString());
	}

	private String activeBuildConfiguration(IProject project) {
		try {
			return Optional.ofNullable(project.getActiveBuildConfig()).map(config -> config.getName()).orElse(""); //$NON-NLS-1$
		} catch (CoreException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
			return ""; //$NON-NLS-1$
		}
	}

	private String message(IProject project, boolean automaticManagementEnabled, Optional<String> manualDirectory,
			Optional<String> automaticDirectory, boolean compileCommandsExists, String compileCommandsPath) {
		if (!automaticManagementEnabled) {
			return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status_disabled;
		}
		if (manualDirectory.isPresent() && !compileCommandsExists) {
			return org.eclipse.osgi.util.NLS.bind(
					LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status_manual_missing,
					compileCommandsPath);
		}
		if (manualDirectory.isPresent()) {
			return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status_manual;
		}
		if (automaticDirectory.isPresent() && !compileCommandsExists) {
			return org.eclipse.osgi.util.NLS.bind(
					LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status_automatic_missing,
					compileCommandsPath);
		}
		if (automaticDirectory.isPresent()) {
			return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status_automatic;
		}
		if (project.getLocation() != null && DefaultClangdCompilationDatabaseProvider.hasClangdFileInParentFolders(project)) {
			return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status_parent_clangd;
		}
		return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status_not_detected;
	}
}
