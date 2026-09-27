/*******************************************************************************
 * Copyright (c) 2025, 2026 Contributors to the Eclipse Foundation.
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * Shared helper for reading, writing, inserting, and updating the managed
 * {@value #CLANGD_CONFIG_FILE_NAME} {@code CompilationDatabase} entry.
 * <p>
 * The listener-driven setter and the project-status support use this base class whenever automatic
 * synchronization needs to create or adjust managed clangd configuration content.
 * </p>
 */
public abstract class ClangdCompilationDatabaseSetterBase {
	public static final String CLANGD_CONFIG_FILE_NAME = ".clangd"; //$NON-NLS-1$
	private static final String COMPILE_FLAGS = "CompileFlags"; //$NON-NLS-1$
	private static final String COMPILATTION_DATABASE = "CompilationDatabase"; //$NON-NLS-1$
	private static final String COMPILE_FLAGS_PREFIX = COMPILE_FLAGS + ":"; //$NON-NLS-1$
	private static final String COMPILATION_DATABASE_PREFIX = COMPILATTION_DATABASE + ":"; //$NON-NLS-1$
	private static final String INDENT = "  "; //$NON-NLS-1$
	protected static final String SET_COMPILATION_DB = COMPILE_FLAGS + ": {" + COMPILATTION_DATABASE + ": %s}"; //$NON-NLS-1$ //$NON-NLS-2$
	private static final String BACKSLASH_REGEX = "\\\\"; //$NON-NLS-1$
	private static final String BACKSLASH_ESCAPE = "\\\\\\\\"; //$NON-NLS-1$
	// matches the value of CompilationDatabase if the value is followed by either end-of-string,
	// newline sequence or ','
	private final Pattern pathGroupPattern = Pattern.compile(".*CompilationDatabase:\\s*\\{?\\s*([^,}\\r\\n]*).*"); //$NON-NLS-1$

	/**
	 * Schedules an update that writes the managed {@code CompilationDatabase} entry to the project's
	 * {@value #CLANGD_CONFIG_FILE_NAME} file.
	 * The file is created when it does not exist yet.
	 * <p>
	 * Existing managed content is updated in place when a {@code CompilationDatabase} entry is already present.
	 * Otherwise the entry is inserted into an existing {@code CompileFlags} section or appended as a new section.
	 * </p>
	 *
	 * @param project project whose {@value #CLANGD_CONFIG_FILE_NAME} file should be updated
	 * @param databaseDirectoryPath project-relative path to the directory that contains
	 *            {@code compile_commands.json}
	 * @return the scheduled WorkspaceJob
	 */
	public WorkspaceJob setCompilationDatabase(IProject project, String databaseDirectoryPath) {
		var configFile = project.getFile(CLANGD_CONFIG_FILE_NAME);
		var updateClangdJob = new WorkspaceJob("Update .clangd") { //$NON-NLS-1$
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				try {
					if (createClangdConfigFile(configFile, project.getDefaultCharset(), databaseDirectoryPath, false)) {
						return Status.OK_STATUS;
					}
					updateClangdConfigFile(configFile, project.getDefaultCharset(), databaseDirectoryPath, monitor);
				} catch (CoreException e) {
					Platform.getLog(getClass()).log(e.getStatus());
				} catch (IOException | IllegalArgumentException e) {
					Platform.getLog(getClass()).error(e.getMessage(), e);
				}
				return Status.OK_STATUS;
			}
		};
		updateClangdJob.setRule(configFile.exists() ? configFile : project);
		updateClangdJob.setSystem(true);
		updateClangdJob.schedule();
		return updateClangdJob;
	}

	/**
	 * Loads the current {@value #CLANGD_CONFIG_FILE_NAME} content and updates the first matching
	 * {@code CompilationDatabase} entry or inserts a new one when no entry exists yet.
	 */
	private void updateClangdConfigFile(IFile configFile, String charset, String databaseDirectoryPath,
			IProgressMonitor monitor) throws CoreException, IOException {
		if (configFile.getLocation() != null) {
			var lines = readClangdConfigFile(configFile);
			var isBlank = true;
			var changed = false;
			var hasCompilationDatabase = false;
			for (int i = 0; i < lines.size(); i++) {
				var line = lines.get(i);
				isBlank &= line.isBlank();
				Matcher pathGroupMatcher = pathGroupPattern.matcher(line);
				if (pathGroupMatcher.matches()) {
					hasCompilationDatabase = true;
					var currentPath = pathGroupMatcher.group(1).trim();
					if (!databaseDirectoryPath.trim().contentEquals(currentPath)) {
						var updatedLine = line.substring(0, pathGroupMatcher.start(1)) + escaped(databaseDirectoryPath)
								+ line.substring(pathGroupMatcher.end(1));
						lines.set(i, updatedLine);
						changed = true;
					}
					break;
				}
			}
			if (!changed && !hasCompilationDatabase && !isBlank) {
				changed = insertCompilationDatabase(lines, databaseDirectoryPath);
			}
			if (isBlank) {
				createClangdConfigFile(configFile, charset, databaseDirectoryPath, true);
			} else if (changed) {
				writeClangdConfigFile(configFile, charset, lines, monitor);
			}
		}
	}

	/**
	 * Inserts a managed {@code CompilationDatabase} entry into an existing {@code CompileFlags}
	 * section or appends a new section when the file does not contain one yet.
	 */
	private boolean insertCompilationDatabase(List<String> lines, String databaseDirectoryPath) {
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			String trimmed = line.trim();
			String indent = line.substring(0, line.indexOf(trimmed));
			if (trimmed.matches("^" + Pattern.quote(COMPILE_FLAGS_PREFIX) + "\\s*\\{.*\\}\\s*$")) { //$NON-NLS-1$ //$NON-NLS-2$
				int closingBracket = line.lastIndexOf('}');
				if (closingBracket >= 0) {
					String prefix = line.substring(0, closingBracket).stripTrailing();
					String suffix = line.substring(closingBracket);
					String separator = prefix.endsWith("{") ? "" : ","; //$NON-NLS-1$ //$NON-NLS-2$
					lines.set(i, prefix + separator + " " + COMPILATTION_DATABASE + ": " //$NON-NLS-1$ //$NON-NLS-2$
							+ escaped(databaseDirectoryPath) + suffix);
					return true;
				}
			} else if (trimmed.matches("^" + Pattern.quote(COMPILE_FLAGS_PREFIX) + "\\s*$")) { //$NON-NLS-1$ //$NON-NLS-2$
				lines.add(i + 1, indent + INDENT + COMPILATION_DATABASE_PREFIX + " " + escaped(databaseDirectoryPath)); //$NON-NLS-1$
				return true;
			}
		}
		if (!lines.isEmpty() && !lines.get(lines.size() - 1).isBlank()) {
			lines.add(""); //$NON-NLS-1$
		}
		lines.add(COMPILE_FLAGS_PREFIX);
		lines.add(INDENT + COMPILATION_DATABASE_PREFIX + " " + escaped(databaseDirectoryPath)); //$NON-NLS-1$
		return true;
	}

	/**
	 * Escapes backslashes so Windows-style paths remain valid in YAML.
	 */
	private String escaped(String databaseDirectoryPath) {
		return databaseDirectoryPath.replaceAll(BACKSLASH_REGEX, BACKSLASH_ESCAPE);
	}

	/**
	 * Reads the {@value #CLANGD_CONFIG_FILE_NAME} file line by line.
	 */
	private List<String> readClangdConfigFile(IFile configFile) throws IOException, CoreException {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(configFile.getContents()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}
		return lines;
	}

	/**
	 * Writes the updated {@value #CLANGD_CONFIG_FILE_NAME} content back to the workspace file.
	 */
	private void writeClangdConfigFile(IFile configFile, String charset, List<String> lines, IProgressMonitor monitor)
			throws UnsupportedEncodingException, CoreException {
		var stringBuilder = new StringBuilder();
		var counter = new AtomicInteger(0);
		int size = lines.size();
		String lineSeparator = System.lineSeparator();
		lines.stream().forEach(line -> {
			if (counter.incrementAndGet() == size) {
				stringBuilder.append(line);
			} else {
				stringBuilder.append(line).append(lineSeparator);
			}
		});
		configFile.setContents(stringBuilder.toString().getBytes(charset), IResource.KEEP_HISTORY, monitor);
	}

	/**
	 * Creates the {@value #CLANGD_CONFIG_FILE_NAME} file or overwrites blank content with the
	 * managed default structure.
	 */
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