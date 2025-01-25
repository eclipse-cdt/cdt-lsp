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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public final class RegexMarkerPattern {
	private final String markerID;
	private static final String EMPTY_STR = ""; //$NON-NLS-1$
	private final Pattern pattern;
	private final String fileExpression;
	private final String lineExpression;
	private final String columnStartExpression;
	private final String columnEndExpression;
	private final String descriptionExpression;
	private final int severity;

	private class ResourceInfo {
		/**
		 * Offset of start character for IMarker in source document.
		 * An integer value indicating where a text marker starts, -1 if unknown.
		 */
		public int charStart = -1;
		/**
		 * Offset of end character for IMarker in source document.
		 * An integer value indicating where a text marker ends, -1 if unknown.
		 */
		public int charEnd = -1;
	}

	/**
	 * Regular expression pattern to parse a clangd stderr line to create warning, error and info {@link IMarker} from.
	 *
	 * @param pattern               Java regular expression which defines capturing groups for fileExpression, lineExpression and descriptionExpression.
	 * @param fileExpression        "Replacement" expression composed from capturing groups defined in regex to define the file. Can be <code>null</code> is
	 *                                  unused for
	 *                                  this pattern.
	 * @param lineExpression        "Replacement" expression composed from capturing groups defined in regex to define the line in file.
	 * @param columnStartExpression "Replacement" expression composed from capturing groups defined in regex to define the column start in file.
	 * @param columnEndExpression   "Replacement" expression composed from capturing groups defined in regex to define the column end in file.
	 *                                  If <code>null</code> the whole line will be marked.
	 * @param descriptionExpression "Replacement" expression composed from capturing groups defined in regex to define the description (i.e. "$1: $2"). It is
	 *                                  possible to specify more than one capturing group in such expression.
	 * @param severity              This attribute specifies which severity should be used to display the {@link IMarker} in Problems View. There are 3 levels
	 *                                  of severity:
	 *                                  {@link IMarker#SEVERITY_ERROR}, {@link IMarker#SEVERITY_WARNING} and {@link IMarker#SEVERITY_INFO}.
	 * @param markerID				IMarker ID as defined in the <code>org.eclipse.core.resources.markers</code> extension point
	 */
	public RegexMarkerPattern(String pattern, String fileExpression, String lineExpression,
			String columnStartExpression, String columnEndExpression, String descriptionExpression, int severity,
			String markerID) {
		this.pattern = Pattern.compile(pattern != null ? pattern : EMPTY_STR);
		this.fileExpression = fileExpression != null ? fileExpression : EMPTY_STR;
		this.lineExpression = lineExpression != null ? lineExpression : EMPTY_STR;
		this.columnStartExpression = columnStartExpression != null ? columnStartExpression : EMPTY_STR;
		this.columnEndExpression = columnEndExpression != null ? columnEndExpression : EMPTY_STR;
		this.descriptionExpression = descriptionExpression != null ? descriptionExpression : EMPTY_STR;
		this.severity = severity;
		this.markerID = markerID;
	}

	public void processLine(String line, IFile file, IDocument fileDocument) {
		if (line.length() > 0) {
			var matcher = pattern.matcher(line);
			if (!matcher.matches()) {
				return;
			}
			var fileURI = matcher.replaceAll(fileExpression);
			if (!file.getLocation().toOSString().equals(fileURI)) {
				Platform.getLog(getClass()).error("Parsed .clang-format path does not match with IFile location: " //$NON-NLS-1$
						+ fileURI + " != " + file.getLocation().toOSString()); //$NON-NLS-1$
				return;
			}
			var lineNumber = 0;
			try {
				lineNumber = Integer.parseInt(matcher.replaceAll(lineExpression));
			} catch (NumberFormatException e) {
				// there must be bug in the pattern or group definition:
				Platform.getLog(getClass()).error("Cannot parse line number from pattern: " + pattern.pattern() //$NON-NLS-1$
						+ " within group: " + lineExpression); //$NON-NLS-1$
			}
			int columnStart = parseColumn(matcher, columnStartExpression);
			int columnEnd = parseColumn(matcher, columnEndExpression);
			var message = matcher.replaceAll(descriptionExpression);

			var resourceInfo = getCharStartCharEnd(fileDocument, lineNumber, columnStart, columnEnd);
			addMarker(file, message, severity, lineNumber, resourceInfo.charStart, resourceInfo.charEnd);
		}
	}

	private void addMarker(IFile file, String message, int severity, int lineNumber, int charStart, int charEnd) {
		if (existingMarker(file, message, severity, lineNumber)) {
			return;
		}
		// create it:
		try {
			if (file == null || !file.exists()) {
				return;
			}
			var marker = file.createMarker(markerID);

			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber > 0) {
				marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			}
			marker.setAttribute(IMarker.CHAR_START, charStart);
			marker.setAttribute(IMarker.CHAR_END, charEnd);
		} catch (CoreException e) {
			Platform.getLog(getClass())
					.error("Cannot create clang format error marker for " + file.getLocation().toOSString(), e); //$NON-NLS-1$
		}
	}

	private boolean existingMarker(IFile file, String message, int severity, int lineNumber) {
		if (file != null && file.exists()) {
			try {
				IMarker[] markers = file.findMarkers(markerID, true, IResource.DEPTH_ONE);
				var existingMarkers = Arrays.stream(markers).filter(cm -> cm.exists()).toList();
				for (var m : existingMarkers) {
					int mlineNumber = m.getAttribute(IMarker.LINE_NUMBER, -1);
					int mSeverity = m.getAttribute(IMarker.SEVERITY, -1);
					String mMessage = (String) m.getAttribute(IMarker.MESSAGE);
					if (mlineNumber == lineNumber && mSeverity == severity && mMessage.equals(message)) {
						return true;
					}
				}
			} catch (CoreException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}
		}
		return false;
	}

	private int parseColumn(Matcher matcher, String columnExpression) {
		int column = -1;
		try {
			// Note: the colon replacement is due to the regex grouping of the patterns:
			var columnString = matcher.replaceAll(columnExpression).replace(':', ' ').trim();
			if (!columnString.isBlank()) {
				column = Integer.parseInt(columnString);
			}
		} catch (NumberFormatException e) {
			// there must be bug in the pattern or group definition:
			Platform.getLog(getClass()).error("Cannot parse column number from pattern: " + pattern.pattern() //$NON-NLS-1$
					+ " within group: " + columnExpression); //$NON-NLS-1$+
		}
		return column;
	}

	ResourceInfo getCharStartCharEnd(IDocument document, int lineNumber, int columnStart, int columnEnd) {
		var resourceInfo = new ResourceInfo();
		try {
			// NOTE: get getLineOffset is 0-based:
			var zeroBasedLine = lineNumber > 0 ? lineNumber - 1 : lineNumber;
			var offset = document.getLineOffset(zeroBasedLine);
			resourceInfo.charStart = offset + columnStart - 1;
			if (columnEnd > -1) {
				resourceInfo.charEnd = resourceInfo.charStart + (columnEnd - columnStart);
			} else {
				resourceInfo.charEnd = getLineEnd(document, zeroBasedLine);
			}
		} catch (BadLocationException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
			resourceInfo.charStart = -1;
			resourceInfo.charEnd = -1;
		}
		return resourceInfo;
	}

	private int getLineEnd(IDocument document, int line) throws BadLocationException {
		var region = document.getLineInformation(line);
		return region.getOffset() + region.getLength();
	}

}
