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

package org.eclipse.cdt.lsp.clangd.internal.config;

import java.io.IOException;

import org.eclipse.cdt.lsp.clangd.plugin.ClangdPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.MarkedYAMLException;

/**
 * Checks the <code>.clangd</code> file for syntax errors and notifies the user via error markers in the file and Problems view.
 */
public class ClangdConfigFileChecker {
	public static final String CLANGD_MARKER = ClangdPlugin.PLUGIN_ID + ".config.marker"; //$NON-NLS-1$

	/**
	 * Checks if the .clangd file contains valid yaml syntax. Adds error marker to the file if not.
	 * @param configFile
	 */
	public void checkConfigFile(IFile configFile) {
		if (!configFile.exists()) {
			return;
		}
		Yaml yaml = new Yaml();
		try (var inputStream = configFile.getContents()) {
			try {
				removeMarkerFromClangdConfig(configFile);
				//throws ScannerException and ParserException:
				yaml.load(inputStream);
			} catch (MarkedYAMLException yamlException) {
				// re-read the file, because the buffer which comes along with MarkedYAMLException is limited to ~800 bytes.
				try (var reReadStream = configFile.getContents()) {
					addMarkerToClangdConfig(configFile, yamlException, reReadStream.readAllBytes());
				}
			} catch (Exception exception) {
				//log unexpected exception:
				Platform.getLog(getClass()).error("Expected MarkedYAMLException, but was: " + exception.getMessage(), //$NON-NLS-1$
						exception);
			}
		} catch (IOException | CoreException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}
	}

	private void addMarkerToClangdConfig(IFile configFile, MarkedYAMLException yamlException, byte[] buffer) {
		try {
			var configMarker = parseYamlException(yamlException, buffer);
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

	private void removeMarkerFromClangdConfig(IFile configFile) {
		try {
			configFile.deleteMarkers(CLANGD_MARKER, false, IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			Platform.getLog(getClass()).log(e.getStatus());
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
	 * @param exception
	 * @param file
	 * @return
	 */
	private ClangdConfigMarker parseYamlException(MarkedYAMLException exception, byte[] buffer) {
		var marker = new ClangdConfigMarker();
		var context = exception.getContext();
		marker.message = context != null ? context + " " + exception.getProblem() : exception.getProblem(); //$NON-NLS-1$
		var problemMark = exception.getProblemMark();
		if (problemMark == null) {
			return marker;
		}
		marker.line = problemMark.getLine() + 1; //getLine() is zero based, IMarker wants 1-based
		int index = problemMark.getIndex();
		if (index == buffer.length) {
			// When index == buffer.length() the marker index points to the non visible
			// \r or \n character and the marker is not displayed in the editor.
			// Or, even worse, there is no next line and index + 1 would be > buffer.length
			// Therefore we have to find the last visible char:
			index = getIndexOfLastVisibleChar(buffer);
		}
		marker.charStart = index;
		marker.charEnd = index + 1;
		return marker;
	}

	private int getIndexOfLastVisibleChar(byte[] buffer) {
		for (int i = buffer.length - 1; i >= 0; i--) {
			if ('\r' != ((char) buffer[i]) && '\n' != ((char) buffer[i])) {
				return i;
			}
		}
		return Math.max(0, buffer.length - 2);
	}
}
