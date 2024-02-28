package org.eclipse.cdt.lsp.internal.editor;

import org.eclipse.cdt.lsp.editor.EditorMetadata;
import org.eclipse.cdt.lsp.editor.EditorOptionsDefaults;

/*******************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

import org.eclipse.cdt.lsp.internal.messages.LspUiMessages;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class EditorMetadataDefaults implements EditorMetadata {

	@Reference
	private EditorOptionsDefaults defaults;

	@Override
	public PreferenceMetadata<Boolean> preferLspEditor() {
		return new PreferenceMetadata<>(Boolean.class, //
				"prefer_lsp", //$NON-NLS-1$
				defaults.preferLspEditor(), //
				LspUiMessages.LspEditorConfigurationPage_preferLspEditor,
				LspUiMessages.LspEditorConfigurationPage_preferLspEditor_description);
	}

	@Override
	public PreferenceMetadata<Boolean> formatOnSave() {
		return new PreferenceMetadata<>(Boolean.class, //
				"format_source", //$NON-NLS-1$
				defaults.formatOnSave(), //
				LspUiMessages.SaveActionsConfigurationPage_FormatSourceCode,
				LspUiMessages.SaveActionsConfigurationPage_FormatSourceCode_description);
	}

	@Override
	public PreferenceMetadata<Boolean> formatAllLines() {
		return new PreferenceMetadata<>(Boolean.class, //
				"format_all_lines", //$NON-NLS-1$
				defaults.formatAllLines(), //
				LspUiMessages.SaveActionsConfigurationPage_FormatAllLines,
				LspUiMessages.SaveActionsConfigurationPage_FormatAllLines_description);
	}

	@Override
	public PreferenceMetadata<Boolean> formatEditedLines() {
		return new PreferenceMetadata<>(Boolean.class, //
				"format_edited_lines", //$NON-NLS-1$
				defaults.formatEditedLines(), //
				LspUiMessages.SaveActionsConfigurationPage_FormatEditedLines,
				LspUiMessages.SaveActionsConfigurationPage_FormatEditedLines_description);
	}

}
