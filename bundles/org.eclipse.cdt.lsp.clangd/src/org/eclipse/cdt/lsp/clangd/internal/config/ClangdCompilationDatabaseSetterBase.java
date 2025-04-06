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

public abstract class ClangdCompilationDatabaseSetterBase {
	public static final String CLANGD_CONFIG_FILE_NAME = ".clangd"; //$NON-NLS-1$
	private static final String COMPILE_FLAGS = "CompileFlags"; //$NON-NLS-1$
	private static final String COMPILATTION_DATABASE = "CompilationDatabase"; //$NON-NLS-1$
	protected static final String SET_COMPILATION_DB = COMPILE_FLAGS + ": {" + COMPILATTION_DATABASE + ": %s}"; //$NON-NLS-1$ //$NON-NLS-2$
	// matches the value of CompilationDatabase if the value is followed by either end-of-string, newline sequence or ','
	private final Pattern pathMatchPattern = Pattern.compile("(?<=CompilationDatabase:)[^,}\\r\\n\\x0b\\f\\x85]*"); //$NON-NLS-1$
	private final Pattern pathGroupPattern = Pattern.compile(".*CompilationDatabase:\\s*([^,}\\r\\n\\x0b\\f\\x85]*).*"); //$NON-NLS-1$

	/**
	 * Set the <code>CompilationDatabase</code> entry in the .clangd file in the given project root.
	 * The file will be created, if it's not existing.
	 * <p>
	 * The value of the <code>CompilationDatabase</code> entry in the .clangd file will be replaced with <code>databaseDirectoryPath</code>, if
	 * the <code>CompilationDatabase</code> entry can be found in the .clangd file. It changes only the first occurrence.
	 * </p>
	 * <p>
	 * NOTE: The file won't be updated if the file is not empty and the <code>CompilationDatabase</code> entry is missing.
	 * </p>
	 * @param project to update its .clangd file
	 * @param databaseDirectoryPath project relative path to the folder which contains the compile_commands.json.
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

	private void updateClangdConfigFile(IFile configFile, String charset, String databaseDirectoryPath,
			IProgressMonitor monitor) throws CoreException, IOException {
		if (configFile.getLocation() != null) {
			var lines = readClangdConfigFile(configFile);
			var isBlank = true;
			for (int i = 0; i < lines.size(); i++) {
				var line = lines.get(i);
				isBlank &= line.isBlank();
				Matcher pathGroupMatcher = pathGroupPattern.matcher(line);
				if (pathGroupMatcher.matches()
						&& !databaseDirectoryPath.contentEquals(pathGroupMatcher.replaceAll("$1").trim())) { //$NON-NLS-1$
					lines.set(i, pathMatchPattern.matcher(line)
							.replaceAll(" " + databaseDirectoryPath.replaceAll("\\\\", "\\\\\\\\"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					writeClangdConfigFile(configFile, charset, lines, monitor);
					break;
				}
			}
			if (isBlank) {
				createClangdConfigFile(configFile, charset, databaseDirectoryPath, true);
			}
		}
	}

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