/*******************************************************************************
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.editor;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;

public interface EditorOptions {

	/**
	 * Prefer to use LSP based C/C++ Editor
	 *
	 * @return if LSP based C/C++ Editor should be preferred
	 */
	boolean preferLspEditor();

	/**
	 * Format source code on file save action
	 *
	 * @return if source code should be formatted on file save action
	 */
	boolean formatOnSave();

	/**
	 * Format all lines in source file
	 *
	 * @return if all lines should be formatted
	 */
	boolean formatAllLines();

	/**
	 * Format edited lines only
	 *
	 * @return if only edited lines should be formatted
	 */
	boolean formatEditedLines();

	/**
	 * @since 3.0
	 */
	void addPreferenceChangedListener(IPreferenceChangeListener listener);

	/**
	 * @since 3.0
	 */
	void removePreferenceChangedListener(IPreferenceChangeListener listener);

}
