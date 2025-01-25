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

package org.eclipse.cdt.lsp.clangd.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.cdt.lsp.clangd.plugin.ClangdPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.lsp4e.LSPEclipseUtils;

public class ClangFormatValidator {
	public static final String CLANG_FORMAT_MARKER = ClangdPlugin.PLUGIN_ID + ".format.marker"; //$NON-NLS-1$
	private static final String ClangFormatError = "(?<file>.*?\\.clang-format):(?<line>\\d+):(?<column>\\d+:)?\\s*(([Ee]rror)|(ERROR)): (.*)"; //$NON-NLS-1$
	private static final String group1 = "$1"; //$NON-NLS-1$
	private static final String group2 = "$2"; //$NON-NLS-1$
	private static final String group3 = "$3"; //$NON-NLS-1$
	private static final String group7 = "$7"; //$NON-NLS-1$
	private final RegexMarkerPattern pattern = new RegexMarkerPattern(ClangFormatError, group1, group2, group3, group7,
			IMarker.SEVERITY_ERROR, CLANG_FORMAT_MARKER);

	public void validateFile(List<String> commandLine, IFile clangFormatFile) throws IOException {
		var fileDocument = LSPEclipseUtils.getDocument(clangFormatFile);
		if (fileDocument == null) {
			return;

		}
		File directory = null;
		if (clangFormatFile.getParent() != null && clangFormatFile.getParent().getLocation() != null) {
			directory = clangFormatFile.getParent().getLocation().toFile();
		}
		// Startup the command
		ProcessBuilder processBuilder = new ProcessBuilder(commandLine).directory(directory);
		Process process = processBuilder.start();

		//remove existing marker first:
		try {
			clangFormatFile.deleteMarkers(CLANG_FORMAT_MARKER, false, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}

		Thread clangdStderrReaderThread = new Thread("Clangd Format Validator") { //$NON-NLS-1$
			@Override
			public void run() {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
					for (String line = reader.readLine(); line != null; line = reader.readLine()) {
						pattern.processLine(line, clangFormatFile, fileDocument);
					}
				} catch (IOException e) {
					Platform.getLog(getClass()).error(e.getMessage(), e);
				}
			}
		};
		clangdStderrReaderThread.start();
		try {
			clangdStderrReaderThread.join();
			process.waitFor();
		} catch (InterruptedException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}
	}

}
