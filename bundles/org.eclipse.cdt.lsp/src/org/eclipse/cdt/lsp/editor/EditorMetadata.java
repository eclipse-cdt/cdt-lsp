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

import org.eclipse.cdt.lsp.config.ConfigurationMetadata;
import org.eclipse.cdt.lsp.internal.messages.LspUiMessages;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

public interface EditorMetadata extends ConfigurationMetadata {
	/**
	 * @since 3.0
	 */
	public static final String PREFER_LSP_KEY = "prefer_lsp"; //$NON-NLS-1$

	/**
	 * The predefined metadata for the "Prefer C/C++ Editor (LSP)" option
	 *
	 * @see EditorOptions#preferLspEditor()
	 *
	 * @since 3.0
	 */
	PreferenceMetadata<Boolean> preferLspEditor = new PreferenceMetadata<>(Boolean.class, //
			PREFER_LSP_KEY, false, //
			LspUiMessages.LspEditorConfigurationPage_preferLspEditor,
			LspUiMessages.LspEditorConfigurationPage_preferLspEditor_description);

	/**
	 * The predefined metadata for the "Format source code" option
	 *
	 * @see EditorOptions#formatOnSave()
	 *
	 * @since 3.0
	 */
	PreferenceMetadata<Boolean> formatOnSave = new PreferenceMetadata<>(Boolean.class, //
			"format_source", //$NON-NLS-1$
			false, //
			LspUiMessages.SaveActionsConfigurationPage_FormatSourceCode,
			LspUiMessages.SaveActionsConfigurationPage_FormatSourceCode_description);

	/**
	 * The predefined metadata for the "Format all lines" option.
	 *
	 * @see EditorOptions#formatAllLines()
	 *
	 * @since 3.0
	 */
	PreferenceMetadata<Boolean> formatAllLines = new PreferenceMetadata<>(Boolean.class, //
			"format_all_lines", //$NON-NLS-1$
			true, //
			LspUiMessages.SaveActionsConfigurationPage_FormatAllLines,
			LspUiMessages.SaveActionsConfigurationPage_FormatAllLines_description);

	/**
	 * Returns the metadata for the "Format edited lines" option.
	 *
	 * @see EditorOptions#formatEditedLines()
	 *
	 * @since 3.0
	 */
	PreferenceMetadata<Boolean> formatEditedLines = new PreferenceMetadata<>(Boolean.class, //
			"format_edited_lines", //$NON-NLS-1$
			false, //
			LspUiMessages.SaveActionsConfigurationPage_FormatEditedLines,
			LspUiMessages.SaveActionsConfigurationPage_FormatEditedLines_description);

}
