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
import org.osgi.service.component.annotations.Component;

/**
 * Centralizes the effective compilation database state for a project.
 * <p>
 * This service resolves the active directory from project settings and provider detection, exposes
 * the status shown in the UI, and performs managed {@value #CLANGD_CONFIG_FILE_NAME} updates only
 * when automatic management is enabled.
 * </p>
 */
@Component(service = ClangdCompilationDatabaseSupport.class)
public final class ClangdCompilationDatabaseSupport extends ClangdCompilationDatabaseSetterBase {
	private static final String COMPILATION_DATABASE_ANCESTORS = "Ancestors"; //$NON-NLS-1$
	private static final String COMPILE_COMMANDS_JSON = "compile_commands.json"; //$NON-NLS-1$

	private final ServiceCaller<ClangdConfiguration> configuration = new ServiceCaller<>(getClass(),
			ClangdConfiguration.class);

	private final ServiceCaller<ClangdCompilationDatabaseProvider> provider = new ServiceCaller<>(getClass(),
			ClangdCompilationDatabaseProvider.class);

	private final ServiceCaller<ClangdCompilationDatabaseSettings> settings = new ServiceCaller<>(getClass(),
			ClangdCompilationDatabaseSettings.class);

	/**
	 * Synchronizes the managed {@value #CLANGD_CONFIG_FILE_NAME} content with the currently detected
	 * compilation database directory.
	 *
	 * @param project project whose managed configuration should be synchronized
	 * @return scheduled update job or an empty optional when no automatic update should happen
	 */
	public Optional<WorkspaceJob> synchronize(IProject project) {
		return synchronize(project, () -> automaticDirectory(project));
	}

	/**
	 * Synchronizes the managed {@value #CLANGD_CONFIG_FILE_NAME} content using a caller-provided
	 * automatically detected directory.
	 *
	 * @param project project whose managed configuration should be synchronized
	 * @param automaticDirectory precomputed automatic directory candidate
	 * @return scheduled update job or an empty optional when no automatic update should happen
	 */
	public Optional<WorkspaceJob> synchronize(IProject project, Optional<String> automaticDirectory) {
		return synchronize(project, () -> automaticDirectory);
	}

	/**
	 * Performs the synchronization decision after all inputs were supplied by the caller.
	 */
	Optional<WorkspaceJob> synchronize(IProject project, Supplier<Optional<String>> automaticDirectorySupplier) {
		if (project == null) {
			return Optional.empty();
		}
		if (!isAutomaticManagementEnabled(project)) {
			return Optional.empty();
		}
		Optional<String> customDirectory = customOverride(project);
		Optional<String> automaticDirectory = customDirectory.isPresent() ? Optional.empty()
				: automaticDirectorySupplier.get();
		if (customDirectory.isEmpty() && automaticDirectory.isEmpty() && hasParentClangdConfiguration(project)) {
			return Optional.empty();
		}
		String configuredDirectory = customDirectory.or(() -> automaticDirectory).orElse(COMPILATION_DATABASE_ANCESTORS);
		return Optional.of(setCompilationDatabase(project, configuredDirectory));
	}

	/**
	 * Returns the current compilation database status for the persisted project settings.
	 *
	 * @param project project whose status should be reported
	 * @return immutable status snapshot used by the UI
	 */
	public ClangdCompilationDatabaseStatus status(IProject project) {
		if (project == null) {
			return new ClangdCompilationDatabaseStatus(Source.NONE, "", "", "", false, false, ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		return status(project, isAutomaticManagementEnabled(project), customOverride(project).orElse("")); //$NON-NLS-1$
	}

	/**
	 * Returns the compilation database status for explicit UI state that may not be stored yet.
	 *
	 * @param project project whose status should be reported
	 * @param automaticManagementEnabled whether automatic management is currently enabled in the UI
	 * @param customOverridePath custom override path currently shown in the UI
	 * @return immutable status snapshot used by the UI
	 */
	public ClangdCompilationDatabaseStatus status(IProject project, boolean automaticManagementEnabled,
			String customOverridePath) {
		if (project == null) {
			return new ClangdCompilationDatabaseStatus(Source.NONE, "", "", "", false, false, ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}
		Optional<String> customDirectory = Optional.ofNullable(customOverridePath).map(String::trim)
				.filter(value -> !value.isBlank());
		Optional<String> automaticDirectory = automaticManagementEnabled && customDirectory.isEmpty()
				? automaticDirectory(project)
				: Optional.empty();
		boolean parentClangd = customDirectory.isEmpty() && automaticDirectory.isEmpty() && hasParentClangdConfiguration(project);
		Source source = customDirectory.isPresent() ? Source.CUSTOM
				: automaticDirectory.isPresent() ? Source.AUTOMATIC
						: automaticManagementEnabled && !parentClangd ? Source.ANCESTORS : Source.NONE;
		String configuredDirectory = customDirectory.or(() -> automaticDirectory)
				.orElse(source == Source.ANCESTORS ? COMPILATION_DATABASE_ANCESTORS : ""); //$NON-NLS-1$
		String compileCommandsPath = compileCommandsPath(project, configuredDirectory).orElse(""); //$NON-NLS-1$
		boolean exists = !compileCommandsPath.isBlank()
				&& Files.isRegularFile(Path.fromOSString(compileCommandsPath).toFile().toPath());
		String buildConfiguration = activeBuildConfiguration(project);
		String message = message(project, automaticManagementEnabled, customDirectory, automaticDirectory, source, exists,
				compileCommandsPath, parentClangd);
		return new ClangdCompilationDatabaseStatus(source, configuredDirectory, compileCommandsPath, buildConfiguration,
				automaticManagementEnabled, exists, message);
	}

	/**
	 * Reads the project-scoped custom override from preferences.
	 */
	private Optional<String> customOverride(IProject project) {
		String[] path = { "" }; //$NON-NLS-1$
		configuration.call(c -> path[0] = c.options(project).compilationDatabaseOverride());
		return Optional.ofNullable(path[0]).map(String::trim).filter(value -> !value.isBlank());
	}

	/**
	 * Delegates compilation database detection to the registered provider service.
	 */
	private Optional<String> automaticDirectory(IProject project) {
		var detected = new AtomicReference<>(Optional.<String>empty());
		provider.call(p -> detected.set(p.getCompilationDatabasePath(project)));
		return detected.get();
	}

	/**
	 * Reads whether automatic {@value #CLANGD_CONFIG_FILE_NAME} management is enabled for the
	 * project.
	 */
	private boolean isAutomaticManagementEnabled(IProject project) {
		boolean[] enabled = new boolean[1];
		settings.call(s -> enabled[0] = s.enableSetCompilationDatabasePath(project));
		return enabled[0];
	}

	/**
	 * Resolves the absolute {@code compile_commands.json} path for the effective directory.
	 */
	private Optional<String> compileCommandsPath(IProject project, String configuredDirectory) {
		if (configuredDirectory == null || configuredDirectory.isBlank()
				|| COMPILATION_DATABASE_ANCESTORS.equals(configuredDirectory) || project.getLocation() == null) {
			return Optional.empty();
		}
		var directory = Path.fromOSString(configuredDirectory);
		var absolute = directory.isAbsolute() ? directory : project.getLocation().append(directory);
		return Optional.of(absolute.append(COMPILE_COMMANDS_JSON).toOSString());
	}

	/**
	 * Returns the active workspace build configuration name for display in the UI.
	 */
	private String activeBuildConfiguration(IProject project) {
		try {
			return Optional.ofNullable(project.getActiveBuildConfig()).map(config -> config.getName()).orElse(""); //$NON-NLS-1$
		} catch (CoreException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
			return ""; //$NON-NLS-1$
		}
	}

	/**
	 * Builds the user-facing status message shown in the project properties page.
	 */
	private String message(IProject project, boolean automaticManagementEnabled, Optional<String> customDirectory,
			Optional<String> automaticDirectory, Source source, boolean compileCommandsExists, String compileCommandsPath,
			boolean parentClangd) {
		if (!automaticManagementEnabled) {
			return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status_disabled;
		}
		if (customDirectory.isPresent() && !compileCommandsExists) {
			return org.eclipse.osgi.util.NLS.bind(
					LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status_custom_missing,
					compileCommandsPath);
		}
		if (customDirectory.isPresent()) {
			return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status_custom;
		}
		if (automaticDirectory.isPresent() && !compileCommandsExists) {
			return org.eclipse.osgi.util.NLS.bind(
					LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status_automatic_missing,
					compileCommandsPath);
		}
		if (automaticDirectory.isPresent()) {
			return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status_automatic;
		}
		if (source == Source.ANCESTORS) {
			return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status_ancestors;
		}
		if (parentClangd || hasParentClangdConfiguration(project)) {
			return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status_parent_clangd;
		}
		return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status_not_detected;
	}

	private boolean hasParentClangdConfiguration(IProject project) {
		return project.getLocation() != null && DefaultClangdCompilationDatabaseProvider.hasClangdFileInParentFolders(project);
	}
}
