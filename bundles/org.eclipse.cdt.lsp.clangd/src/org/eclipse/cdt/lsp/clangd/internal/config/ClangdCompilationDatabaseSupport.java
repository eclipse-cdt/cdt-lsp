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
import java.util.function.Supplier;

import org.eclipse.cdt.lsp.clangd.ClangdCompilationDatabaseProvider;
import org.eclipse.cdt.lsp.clangd.ClangdCompilationDatabaseSettings;
import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.clangd.internal.config.ClangdCompilationDatabaseStatus.Source;
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

	Optional<WorkspaceJob> synchronize(IProject project, Supplier<Optional<String>> automaticDirectorySupplier) {
		if (project == null || !isAutomaticManagementEnabled(project)) {
			return Optional.empty();
		}
		return configuredDirectory(project, automaticDirectorySupplier).map(path -> setCompilationDatabase(project, path));
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
		Optional<String>[] detected = new Optional[] { Optional.empty() };
		provider.call(p -> detected[0] = p.getCompilationDatabasePath(project));
		return detected[0];
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
			return "Automatic .clangd compilation database updates are disabled."; //$NON-NLS-1$
		}
		if (manualDirectory.isPresent() && !compileCommandsExists) {
			return "Manual override is configured, but compile_commands.json was not found at " + compileCommandsPath; //$NON-NLS-1$
		}
		if (manualDirectory.isPresent()) {
			return "Using the manual compilation database directory override."; //$NON-NLS-1$
		}
		if (automaticDirectory.isPresent() && !compileCommandsExists) {
			return "Detected compilation database directory, but compile_commands.json was not found at "
					+ compileCommandsPath;
		}
		if (automaticDirectory.isPresent()) {
			return "Using the active build configuration to manage CompilationDatabase in .clangd."; //$NON-NLS-1$
		}
		if (project.getLocation() != null && DefaultClangdCompilationDatabaseProvider.hasClangdFileInParentFolders(project)) {
			return "A parent .clangd file was found. Project-level CompilationDatabase updates are skipped."; //$NON-NLS-1$
		}
		return "No compilation database directory could be detected for the active build configuration."; //$NON-NLS-1$
	}
}
