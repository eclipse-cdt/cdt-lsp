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

package org.eclipse.cdt.lsp.internal.clangd;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.cdt.lsp.internal.clangd.editor.ClangdPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.yaml.snakeyaml.Yaml;

/**
 * Checks the <code>.clangd</code> file for syntax errors and notifies the user via error markers in the file and Problems view.
 */
public class ClangdConfigFileChecker {
	public static final String CLANGD_MARKER = ClangdPlugin.PLUGIN_ID + ".config.marker"; //$NON-NLS-1$
	private final Pattern pattern = Pattern.compile(".*line (\\d+), column (\\d+).*"); //$NON-NLS-1$
	private boolean temporaryLoadedFile = false;

	/**
	 * Checks if the .clangd file contains valid yaml syntax. Adds error marker to the file if not.
	 * @param configFile
	 */
	public void checkConfigFile(IFile configFile) {
		Yaml yaml = new Yaml();
		try (var inputStream = configFile.getContents()) {
			try {
				removeMarkerFromClangdConfig(configFile);
				//throws ScannerException and ParserException:
				yaml.load(inputStream);
			} catch (Exception yamlException) {
				addMarkerToClangdConfig(configFile, yamlException);
			}
		} catch (IOException | CoreException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}
	}

	private void addMarkerToClangdConfig(IFile configFile, Exception e) {
		try {
			var configMarker = parseYamlException(e, configFile);
			var marker = configFile.createMarker(CLANGD_MARKER);
			marker.setAttribute(IMarker.MESSAGE, configMarker.message);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute(IMarker.LINE_NUMBER, configMarker.line);
			marker.setAttribute(IMarker.CHAR_START, configMarker.charStart);
			marker.setAttribute(IMarker.CHAR_END, configMarker.charEnd);
		} catch (CoreException core) {
			Platform.getLog(getClass()).log(core.getStatus());
		}
	}

	private class ClangdConfigMarker {
		public String message;
		public int line = 1;
		public int charStart = -1;
		public int charEnd = -1;
	}

	/**
	 * Fetch line and char position information from exception to create a marker for the .clangd file.
	 * @param e
	 * @param file
	 * @return
	 */
	private ClangdConfigMarker parseYamlException(Exception e, IFile file) {
		var marker = new ClangdConfigMarker();
		marker.message = getErrorMessage(e);
		var doc = getDocument(file);
		if (doc == null) {
			return marker;
		}
		int startLine = -1;
		int endLine = -1;
		for (var line : toLines(e.getMessage())) {
			var matcher = pattern.matcher(line);
			if (matcher.matches()) {
				var lineInt = Integer.parseInt(matcher.replaceAll("$1")); //$NON-NLS-1$
				var column = Integer.parseInt(matcher.replaceAll("$2")); //$NON-NLS-1$
				if (startLine == -1) {
					startLine = lineInt;
				} else if (endLine == -1) {
					endLine = lineInt;
				}
				try {
					if (marker.charStart == -1 && startLine > -1) {
						var lineOffset = doc.getLineOffset(startLine - 1);
						marker.charStart = lineOffset + column - 1;
					} else if (marker.charEnd == -1 && endLine > -1) {
						var lineOffset = doc.getLineOffset(endLine - 1);
						marker.charEnd = lineOffset + column - 1;
					}
				} catch (BadLocationException bl) {
					Platform.getLog(getClass()).error(bl.getMessage(), bl);
				}
				if (startLine > -1 && endLine > -1)
					break;
			}
		}
		//check if endChar has been found:
		if (marker.charEnd == -1) {
			if (marker.charStart < doc.getLength() - 1) {
				marker.charEnd = marker.charStart + 1;
			} else if (marker.charStart == doc.getLength() - 1 && marker.charStart > 0) {
				marker.charEnd = marker.charStart;
				marker.charStart--;
			} else {
				marker.charStart = 0;
				marker.charEnd = 1;
			}
		}
		cleanUp(file);
		if (startLine > -1) {
			marker.line = startLine;
		}
		return marker;
	}

	private String[] toLines(String message) {
		return Optional.ofNullable(message).map(m -> m.lines().toArray(String[]::new)).orElse(new String[] {});
	}

	private String getErrorMessage(Exception e) {
		return Optional.ofNullable(e.getLocalizedMessage())
				.map(m -> m.replaceAll("[" + System.lineSeparator() + "]", " ")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				.orElse("Unknown yaml error"); //$NON-NLS-1$
	}

	private void removeMarkerFromClangdConfig(IFile configFile) {
		try {
			configFile.deleteMarkers(CLANGD_MARKER, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			Platform.getLog(getClass()).log(e.getStatus());
		}
	}

	private IDocument getDocument(IFile file) {
		IDocument document = FileUtils.getDocumentFromBuffer(file);
		if (document != null)
			return document;
		document = FileUtils.loadFileTemporary(file);
		if (document != null)
			temporaryLoadedFile = true;
		return document;
	}

	private void cleanUp(IFile file) {
		if (temporaryLoadedFile) {
			FileUtils.disconnectTemporaryLoadedFile(file);
			temporaryLoadedFile = false;
		}
	}

}
