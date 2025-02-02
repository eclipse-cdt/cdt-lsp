/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.lsp.clangd.IClangdCommandLineValidator;
import org.eclipse.cdt.lsp.clangd.plugin.ClangdPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;

/**
 * Validates the clangd command line options for clangd major versions >= 12
 * because it relies on the clangd <code>--check</code> mode.
 * This is the default implementation for the {@link IClangdCommandLineValidator} service.
 */
@Component(property = { "service.ranking:Integer=0" })
public final class ClangdCommandLineValidator implements IClangdCommandLineValidator {
	private static final String CLANGD_VERSION_PATTERN = ".*clangd\\s+version\\s+(\\d+\\.)?(\\d+\\.)?(\\*|\\d+).*"; //$NON-NLS-1$
	private final Pattern pattern = Pattern.compile(CLANGD_VERSION_PATTERN);
	private static final String major = "$1"; //$NON-NLS-1$
	Path tempFile = null;

	private interface IClangdChecker {
		IStatus getResult();
	}

	@Override
	public IStatus validateCommandLineOptions(final List<String> commands) {
		var result = supportsValidation(commands.getFirst());
		if (!result.isOK()) {
			return result;
		}
		try {
			return createTempCFile() //
					.map(temp -> this.getValidationCommands(temp, commands)) //
					.map(this::getProcessBuilder) //
					.map(pb -> {
						try {
							var process = pb.start();
							return getProcessResult(process, process.getErrorStream(), new OptionsChecker(),
									new StringBuilder("Validate clangd command line options")); //$NON-NLS-1$
						} catch (IOException e) {
							return new Status(IStatus.ERROR, ClangdPlugin.PLUGIN_ID, e.getMessage(), e);
						}
					}).orElse(Status.OK_STATUS);
		} finally {
			deleteTempCFile();
		}
	}

	/**
	 * Checks if given clangd binary supports the <code>--check</code> option.
	 * This is fulfilled when the clangd major version is >= 12
	 */
	private IStatus supportsValidation(String clangdBinaryPath) {
		var commands = new ArrayList<String>(2);
		commands.add(clangdBinaryPath);
		commands.add("--version"); //$NON-NLS-1$
		return Optional.ofNullable(getProcessBuilder(commands)).map(pb -> {
			try {
				var process = pb.start();
				return getProcessResult(process, process.getInputStream(), new VersionChecker(),
						new StringBuilder("Check clangd version")); //$NON-NLS-1$
			} catch (IOException e) {
				return new Status(IStatus.ERROR, ClangdPlugin.PLUGIN_ID, e.getMessage(), e);
			}
		}).orElse(new Status(IStatus.ERROR, ClangdPlugin.PLUGIN_ID,
				"Cannot determine if clangd command line validation is supported")); //$NON-NLS-1$
	}

	private Optional<Path> createTempCFile() {
		try {
			tempFile = Files.createTempFile("dummy", ".c"); //$NON-NLS-1$ //$NON-NLS-2$
			return Optional.of(tempFile);
		} catch (IOException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}
		return Optional.empty();
	}

	private void deleteTempCFile() {
		try {
			if (tempFile != null) {
				Files.deleteIfExists(tempFile);
			}
		} catch (IOException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}
	}

	private ProcessBuilder getProcessBuilder(final List<String> commands) {
		return commands.isEmpty() ? null : new ProcessBuilder(commands);
	}

	private List<String> getValidationCommands(final Path tempFile, final List<String> commands) {
		if (commands.isEmpty()) {
			return commands;
		}
		commands.add("--check=" + tempFile.toAbsolutePath().toString()); //$NON-NLS-1$
		commands.add("--log=error"); //$NON-NLS-1$
		return commands;
	}

	private IStatus getProcessResult(final Process process, final InputStream inputStream,
			final Consumer<String> consumer, final StringBuilder description) {
		var readerThread = getReaderThread("CDT clangd version check", inputStream, consumer); //$NON-NLS-1$
		readerThread.start();
		try {
			var exited = process.waitFor(5000, TimeUnit.MILLISECONDS);
			if (exited && consumer instanceof IClangdChecker validator) {
				return validator.getResult();
			} else {
				if (!exited) {
					process.destroyForcibly();
				}
				//handle timeout:
				description.append(": process timeout or consumer is not a instance of IClangdValidator!"); //$NON-NLS-1$
				return new Status(IStatus.WARNING, ClangdPlugin.PLUGIN_ID, description.toString());
			}
		} catch (InterruptedException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
			return new Status(IStatus.WARNING, ClangdPlugin.PLUGIN_ID, description.toString());
		}
	}

	private Thread getReaderThread(final String threadName, final InputStream stderr,
			final Consumer<String> validator) {
		return new Thread(threadName) {
			@Override
			public void run() {
				if (stderr == null) {
					Platform.getLog(getClass()).error("input stream is null!"); //$NON-NLS-1$
					return;
				}
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(stderr))) {
					for (String line = reader.readLine(); line != null; line = reader.readLine()) {
						validator.accept(line);
					}
				} catch (IOException e) {
					Platform.getLog(getClass()).error(e.getMessage(), e);
				}
			}
		};
	}

	private class OptionsChecker implements Consumer<String>, IClangdChecker {
		private static final String CLANGD_ERROR_PATTERN = ".*(?<!\\.)clangd(.exe)?\s*:.+"; //$NON-NLS-1$
		private final String ls = System.lineSeparator();
		private StringBuilder builder = new StringBuilder();

		@Override
		public void accept(String line) {
			if (line.matches(CLANGD_ERROR_PATTERN)) {
				if (builder.isEmpty()) {
					builder.append(ls);
				}
				builder.append(ls);
				builder.append(line);
			}
		}

		@Override
		public IStatus getResult() {
			if (builder.isEmpty()) {
				return Status.OK_STATUS;
			}
			return new Status(IStatus.ERROR, ClangdPlugin.PLUGIN_ID, builder.toString());
		}
	}

	private class VersionChecker implements Consumer<String>, IClangdChecker {
		private IStatus result = new Status(IStatus.WARNING, ClangdPlugin.PLUGIN_ID,
				"The clangd version does not support command line option check!"); //$NON-NLS-1$

		@Override
		public void accept(String line) {
			if (versionOK(line)) {
				result = Status.OK_STATUS;
			}
		}

		@Override
		public IStatus getResult() {
			return result;
		}

	}

	private boolean versionOK(String line) {
		var matcher = pattern.matcher(line);
		if (!matcher.matches()) {
			return false;
		}
		return majorVersionOK(matcher, major, line);
	}

	private boolean majorVersionOK(Matcher matcher, String majorExpression, String line) {
		try {
			// Note: the dot replacement is due to the regex grouping of the patterns:
			var majorVersion = matcher.replaceAll(majorExpression).replace('.', ' ').trim();
			if (!majorVersion.isBlank()) {
				return Integer.parseInt(majorVersion) >= 12;
			}
		} catch (NumberFormatException e) {
			// there must be bug in the pattern or group definition:
			Platform.getLog(getClass()).error("Cannot parse clangd major version number from line: " + line); //$NON-NLS-1$+
		}
		return false;
	}

}
