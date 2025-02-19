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

import java.util.List;

import org.eclipse.cdt.lsp.config.ConfigurationMetadata;
import org.eclipse.cdt.lsp.internal.messages.LspUiMessages;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

public interface EditorMetadata extends ConfigurationMetadata {

	/**
	 * Predefined preference metadata
	 *
	 * @since 3.0
	 *
	 * @noextend This interface is not intended to be extended by clients.
	 * @noimplement This interface is not intended to be implemented by clients.
	 */
	interface Predefined {

		/**
		 * The predefined metadata for the "Prefer C/C++ Editor (LSP)" option
		 *
		 * @see EditorOptions#preferLspEditor()
		 */
		PreferenceMetadata<Boolean> preferLspEditor = new PreferenceMetadata<>(Boolean.class, //
				"prefer_lsp", //$NON-NLS-1$
				false, //
				LspUiMessages.LspEditorConfigurationPage_preferLspEditor,
				LspUiMessages.LspEditorConfigurationPage_preferLspEditor_description);
		/**
		 * The predefined metadata for the "Format source code" option
		 *
		 * @see EditorOptions#formatOnSave()
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
		 */
		PreferenceMetadata<Boolean> formatEditedLines = new PreferenceMetadata<>(Boolean.class, //
				"format_edited_lines", //$NON-NLS-1$
				false, //
				LspUiMessages.SaveActionsConfigurationPage_FormatEditedLines,
				LspUiMessages.SaveActionsConfigurationPage_FormatEditedLines_description);

		/**
		 * Returns the default {@link List} of {@link PreferenceMetadata}
		 */
		List<PreferenceMetadata<?>> defaults = List.of(//
				preferLspEditor, //
				formatOnSave, //
				formatAllLines, //
				formatEditedLines//
		);
	}

}
