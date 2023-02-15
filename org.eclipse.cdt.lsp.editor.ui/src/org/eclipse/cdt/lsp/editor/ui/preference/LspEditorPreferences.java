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

package org.eclipse.cdt.lsp.editor.ui.preference;

import org.eclipse.cdt.lsp.editor.ui.LspEditorUiMessages;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

public final class LspEditorPreferences {
	
	public static PreferenceMetadata<Boolean> getPreferenceMetadata() {
		return new PreferenceMetadata<>(Boolean.class, "prefer_lsp_editor", false, //$NON-NLS-1$
				LspEditorUiMessages.LspEditorPreferencePage_preferLspEditor,
				LspEditorUiMessages.LspEditorPreferencePage_preferLspEditor_description);
	}

}
