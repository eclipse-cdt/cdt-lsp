/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
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

package org.eclipse.cdt.lsp.internal.clangd.editor.preferences;

import org.eclipse.cdt.lsp.internal.clangd.editor.LspEditorUiMessages;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

public final class LspEditorPreferences {
	public static final String PREFER_LSP_EDITOR = "prefer_lsp_editor";
	public static final String SERVER_PATH = "server_path";
	public static final String SERVER_OPTIONS = "server_options";
	
	public static PreferenceMetadata<Boolean> getPreferenceMetadata() {
		return new PreferenceMetadata<>(Boolean.class, PREFER_LSP_EDITOR, false, //$NON-NLS-1$
				LspEditorUiMessages.LspEditorPreferencePage_preferLspEditor,
				LspEditorUiMessages.LspEditorPreferencePage_preferLspEditor_description);
	}

}
