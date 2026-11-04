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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.eclipse.cdt.lsp.util.LspUtils;
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
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

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
	private static final String BACKSLASH = "\\"; //$NON-NLS-1$

	private static final class CompilationDatabaseLocation {
		private final int lineIndex;
		private final int keyStart;
		private final int valueStart;
		private final int valueEnd;
		private final boolean inline;

		private CompilationDatabaseLocation(int lineIndex, int keyStart, int valueStart, int valueEnd, boolean inline) {
			this.lineIndex = lineIndex;
			this.keyStart = keyStart;
			this.valueStart = valueStart;
			this.valueEnd = valueEnd;
			this.inline = inline;
		}

		private String valueFrom(List<String> lines) {
			return lines.get(lineIndex).substring(valueStart, valueEnd);
		}
	}

	private boolean hasClangdFileWritten = false;

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
		hasClangdFileWritten = false;
		var updateClangdJob = new WorkspaceJob("Update .clangd") { //$NON-NLS-1$
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				try {
					if (createClangdConfigFile(configFile, project.getDefaultCharset(), databaseDirectoryPath, false)) {
						return Status.OK_STATUS;
					}
					hasClangdFileWritten = updateClangdConfigFile(configFile, project.getDefaultCharset(),
							databaseDirectoryPath, monitor);
				} catch (CoreException e) {
					Platform.getLog(getClass()).log(e.getStatus());
				} catch (IOException | IllegalArgumentException e) {
					Platform.getLog(getClass()).error(e.getMessage(), e);
				}
				return Status.OK_STATUS;
			}
		};
		updateClangdJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (hasClangdFileWritten && LspUtils.isLsActive()) {
					LspUtils.restartClangd();
				}
			}
		});
		boolean removalMayDeleteFile = databaseDirectoryPath == null || databaseDirectoryPath.isBlank();
		updateClangdJob.setRule(removalMayDeleteFile ? project : (configFile.exists() ? configFile : project));
		updateClangdJob.setSystem(true);
		updateClangdJob.schedule();
		return updateClangdJob;
	}

	public Optional<String> getCurrentPathInConfigFile(IProject project) {
		var configFile = project.getFile(CLANGD_CONFIG_FILE_NAME);
		if (configFile.exists()) {
			try {
				var lines = readClangdConfigFile(configFile);
				var location = findCompilationDatabaseLocation(lines);
				if (location.isPresent()) {
					return Optional.of(removeEscaped(location.get().valueFrom(lines).trim()));
				}
			} catch (CoreException e) {
				Platform.getLog(getClass()).log(e.getStatus());
			} catch (IOException | IllegalArgumentException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}
		}
		return Optional.empty();

	}

	/**
	 * Loads the current {@value #CLANGD_CONFIG_FILE_NAME} content and updates the first matching
	 * {@code CompilationDatabase} entry or inserts a new one when no entry exists yet.
	 * Returns true when the file was updated, false when no changes were necessary.
	 */
	private boolean updateClangdConfigFile(IFile configFile, String charset, String databaseDirectoryPath,
			IProgressMonitor monitor) throws CoreException, IOException {
		if (configFile.getLocation() != null) {
			if (configFile.exists() && (databaseDirectoryPath == null || databaseDirectoryPath.isBlank())) {
				return removeCompilationDatabase(configFile, charset, monitor);
			}
			var lines = readClangdConfigFile(configFile);
			var isBlank = true;
			var changed = false;
			var hasCompilationDatabase = false;
			for (var line : lines) {
				isBlank &= line.isBlank();
			}

			var location = findCompilationDatabaseLocation(lines);
			if (location.isPresent()) {
				hasCompilationDatabase = true;
				var entry = location.get();
				var currentPath = removeEscaped(entry.valueFrom(lines).trim());
				if (!databaseDirectoryPath.trim().contentEquals(currentPath)) {
					var line = lines.get(entry.lineIndex);
					var updatedLine = line.substring(0, entry.valueStart) + databaseDirectoryPath
							+ line.substring(entry.valueEnd);
					lines.set(entry.lineIndex, updatedLine);
					changed = true;
				}
			}
			if (!changed && !hasCompilationDatabase && !isBlank) {
				changed = insertCompilationDatabase(lines, databaseDirectoryPath);
			}
			if (isBlank) {
				createClangdConfigFile(configFile, charset, databaseDirectoryPath, true);
			} else if (changed) {
				writeClangdConfigFile(configFile, charset, lines, monitor);
				return true;
			}
		}
		return false;
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
					String separator = prefix.endsWith("{") ? "" : ","; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					lines.set(i, prefix + separator + " " + COMPILATTION_DATABASE + ": " //$NON-NLS-1$ //$NON-NLS-2$
							+ databaseDirectoryPath + suffix);
					return true;
				}
			} else if (trimmed.matches("^" + Pattern.quote(COMPILE_FLAGS_PREFIX) + "\\s*$")) { //$NON-NLS-1$ //$NON-NLS-2$
				lines.add(i + 1, indent + INDENT + COMPILATION_DATABASE_PREFIX + " " + databaseDirectoryPath); //$NON-NLS-1$
				return true;
			}
		}
		if (!lines.isEmpty() && !lines.get(lines.size() - 1).isBlank()) {
			lines.add(""); //$NON-NLS-1$
		}
		lines.add(COMPILE_FLAGS_PREFIX);
		lines.add(INDENT + COMPILATION_DATABASE_PREFIX + " " + databaseDirectoryPath); //$NON-NLS-1$
		return true;
	}

	/**
	 * Removes the first matching {@code CompilationDatabase} entry and cleans up empty
	 * {@code CompileFlags} blocks. Deletes the file when no YAML entries remain.
	 */
	private boolean removeCompilationDatabase(IFile configFile, String charset, IProgressMonitor monitor)
			throws CoreException, IOException {
		if (configFile.getLocation() == null) {
			return false;
		}
		var lines = readClangdConfigFile(configFile);
		if (lines.isEmpty()) {
			return false;
		}

		boolean changed = removeCompilationDatabaseEntry(lines);
		if (!changed) {
			return false;
		}

		cleanupEmptyCompileFlagsSections(lines);
		if (!hasYamlEntries(lines)) {
			configFile.delete(IResource.KEEP_HISTORY, monitor);
		} else {
			writeClangdConfigFile(configFile, charset, lines, monitor);
		}
		return true;
	}

	/**
	 * Removes a single {@code CompilationDatabase} key/value from either an inline map or block form.
	 */
	private boolean removeCompilationDatabaseEntry(List<String> lines) {
		var location = findCompilationDatabaseLocation(lines);
		if (location.isEmpty()) {
			return false;
		}

		var entry = location.get();
		if (!entry.inline) {
			lines.remove(entry.lineIndex);
			return true;
		}

		String line = lines.get(entry.lineIndex);
		String updated = removeInlineCompilationDatabaseEntry(line, entry.keyStart, entry.valueEnd);
		if (!updated.contentEquals(line)) {
			lines.set(entry.lineIndex, updated);
			return true;
		}
		return false;
	}

	private String removeInlineCompilationDatabaseEntry(String line, int keyStart, int valueEnd) {
		int removeStart = keyStart;
		int removeEnd = valueEnd;

		int next = valueEnd;
		while (next < line.length() && Character.isWhitespace(line.charAt(next))) {
			next++;
		}
		if (next < line.length() && line.charAt(next) == ',') {
			removeEnd = next + 1;
			while (removeEnd < line.length() && Character.isWhitespace(line.charAt(removeEnd))) {
				removeEnd++;
			}
		} else {
			int prev = keyStart - 1;
			while (prev >= 0 && Character.isWhitespace(line.charAt(prev))) {
				prev--;
			}
			if (prev >= 0 && line.charAt(prev) == ',') {
				removeStart = prev;
			}
		}

		String updated = line.substring(0, removeStart) + line.substring(removeEnd);
		updated = updated.replace("{ ", "{").replace(" ,", ","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		updated = updated.replaceAll("\\{\\s*\\}", "{}"); //$NON-NLS-1$ //$NON-NLS-2$
		return updated.stripTrailing();
	}

	private Optional<CompilationDatabaseLocation> findCompilationDatabaseLocation(List<String> lines) {
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			String trimmed = line.trim();
			if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.startsWith(COMPILE_FLAGS_PREFIX)) { //$NON-NLS-1$
				continue;
			}

			int compileFlagsIndent = leadingWhitespaceCount(line);
			int compileFlagsKeyStart = line.indexOf(COMPILE_FLAGS_PREFIX);
			int compileFlagsValueStart = skipWhitespace(line, compileFlagsKeyStart + COMPILE_FLAGS_PREFIX.length());
			if (compileFlagsValueStart < line.length() && line.charAt(compileFlagsValueStart) == '{') {
				CompilationDatabaseLocation inlineLocation = findInlineCompilationDatabaseLocation(i, line,
						compileFlagsValueStart);
				if (inlineLocation != null) {
					return Optional.of(inlineLocation);
				}
				continue;
			}

			for (int j = i + 1; j < lines.size(); j++) {
				String candidate = lines.get(j);
				String candidateTrimmed = candidate.trim();
				if (candidateTrimmed.isBlank()) {
					continue;
				}

				int candidateIndent = leadingWhitespaceCount(candidate);
				if (candidateIndent <= compileFlagsIndent) {
					break;
				}
				if (candidateTrimmed.startsWith("#")) { //$NON-NLS-1$
					continue;
				}
				if (!candidateTrimmed.startsWith(COMPILATION_DATABASE_PREFIX)) {
					continue;
				}

				int keyStart = candidate.indexOf(COMPILATION_DATABASE_PREFIX);
				int valueStart = skipWhitespace(candidate, keyStart + COMPILATION_DATABASE_PREFIX.length());
				int valueEnd = findValueEndBeforeComment(candidate, valueStart);
				return Optional.of(new CompilationDatabaseLocation(j, keyStart, valueStart, valueEnd, false));
			}
		}
		return Optional.empty();
	}

	private CompilationDatabaseLocation findInlineCompilationDatabaseLocation(int lineIndex, String line,
			int openingBraceIndex) {
		boolean inSingleQuoted = false;
		boolean inDoubleQuoted = false;
		boolean escaped = false;
		int depth = 0;
		for (int i = openingBraceIndex; i < line.length(); i++) {
			char c = line.charAt(i);

			if (inDoubleQuoted) {
				if (escaped) {
					escaped = false;
				} else if (c == '\\') {
					escaped = true;
				} else if (c == '"') {
					inDoubleQuoted = false;
				}
				continue;
			}
			if (inSingleQuoted) {
				if (c == '\'') {
					inSingleQuoted = false;
				}
				continue;
			}

			if (c == '"') {
				inDoubleQuoted = true;
				continue;
			}
			if (c == '\'') {
				inSingleQuoted = true;
				continue;
			}
			if (c == '{') {
				depth++;
				continue;
			}
			if (c == '}') {
				depth--;
				if (depth < 0) {
					break;
				}
				continue;
			}
			if (depth != 1 || !line.regionMatches(i, COMPILATTION_DATABASE, 0, COMPILATTION_DATABASE.length())) {
				continue;
			}

			int colonIndex = skipWhitespace(line, i + COMPILATTION_DATABASE.length());
			if (colonIndex >= line.length() || line.charAt(colonIndex) != ':') {
				continue;
			}
			int valueStart = skipWhitespace(line, colonIndex + 1);
			int valueEnd = findInlineValueEnd(line, valueStart);
			return new CompilationDatabaseLocation(lineIndex, i, valueStart, valueEnd, true);
		}
		return null;
	}

	private int findInlineValueEnd(String line, int valueStart) {
		boolean inSingleQuoted = false;
		boolean inDoubleQuoted = false;
		boolean escaped = false;
		int braceDepth = 0;
		int bracketDepth = 0;
		for (int i = valueStart; i < line.length(); i++) {
			char c = line.charAt(i);

			if (inDoubleQuoted) {
				if (escaped) {
					escaped = false;
				} else if (c == '\\') {
					escaped = true;
				} else if (c == '"') {
					inDoubleQuoted = false;
				}
				continue;
			}
			if (inSingleQuoted) {
				if (c == '\'') {
					inSingleQuoted = false;
				}
				continue;
			}

			if (c == '"') {
				inDoubleQuoted = true;
				continue;
			}
			if (c == '\'') {
				inSingleQuoted = true;
				continue;
			}
			if (c == '{') {
				braceDepth++;
				continue;
			}
			if (c == '}') {
				if (braceDepth == 0 && bracketDepth == 0) {
					return i;
				}
				braceDepth--;
				continue;
			}
			if (c == '[') {
				bracketDepth++;
				continue;
			}
			if (c == ']') {
				if (bracketDepth > 0) {
					bracketDepth--;
				}
				continue;
			}
			if (c == ',' && braceDepth == 0 && bracketDepth == 0) {
				return i;
			}
		}
		return line.length();
	}

	private int findValueEndBeforeComment(String line, int valueStart) {
		boolean inSingleQuoted = false;
		boolean inDoubleQuoted = false;
		boolean escaped = false;
		for (int i = valueStart; i < line.length(); i++) {
			char c = line.charAt(i);

			if (inDoubleQuoted) {
				if (escaped) {
					escaped = false;
				} else if (c == '\\') {
					escaped = true;
				} else if (c == '"') {
					inDoubleQuoted = false;
				}
				continue;
			}
			if (inSingleQuoted) {
				if (c == '\'') {
					inSingleQuoted = false;
				}
				continue;
			}

			if (c == '"') {
				inDoubleQuoted = true;
				continue;
			}
			if (c == '\'') {
				inSingleQuoted = true;
				continue;
			}
			if (c == '#') {
				int end = i;
				while (end > valueStart && Character.isWhitespace(line.charAt(end - 1))) {
					end--;
				}
				return end;
			}
		}
		return line.length();
	}

	private int skipWhitespace(String line, int start) {
		int index = start;
		while (index < line.length() && Character.isWhitespace(line.charAt(index))) {
			index++;
		}
		return index;
	}

	private int leadingWhitespaceCount(String line) {
		int count = 0;
		while (count < line.length() && Character.isWhitespace(line.charAt(count))) {
			count++;
		}
		return count;
	}

	private void cleanupEmptyCompileFlagsSections(List<String> lines) {
		for (int i = lines.size() - 1; i >= 0; i--) {
			String line = lines.get(i);
			String trimmed = line.trim();
			if (trimmed.matches("^" + Pattern.quote(COMPILE_FLAGS_PREFIX) + "\\s*\\{\\s*\\}\\s*$")) { //$NON-NLS-1$ //$NON-NLS-2$
				lines.remove(i);
				continue;
			}

			if (!trimmed.matches("^" + Pattern.quote(COMPILE_FLAGS_PREFIX) + "\\s*$")) { //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			}

			String indent = line.substring(0, line.indexOf(trimmed));
			boolean hasEntry = false;
			int j = i + 1;
			while (j < lines.size()) {
				String candidate = lines.get(j);
				String candidateTrimmed = candidate.trim();
				if (candidateTrimmed.isBlank()) {
					j++;
					continue;
				}
				if (!candidate.startsWith(indent + INDENT)) {
					break;
				}
				if (!candidateTrimmed.startsWith("#")) { //$NON-NLS-1$
					hasEntry = true;
					break;
				}
				j++;
			}

			if (!hasEntry) {
				lines.remove(i);
			}
		}
	}

	private boolean hasYamlEntries(List<String> lines) {
		for (String line : lines) {
			String trimmed = line.trim();
			if (!trimmed.isEmpty() && !trimmed.startsWith("#")) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	/**
	 * Normalizes legacy escaped backslashes before comparing stored and requested paths.
	 */
	private String removeEscaped(String databaseDirectoryPath) {
		return databaseDirectoryPath.replace(BACKSLASH_REGEX, BACKSLASH);
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
			if (databasePath == null || databasePath.isBlank()) {
				return true;
			}
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
