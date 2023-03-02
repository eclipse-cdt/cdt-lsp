/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
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

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.editor.ui.LspEditorUiPlugin;
import org.eclipse.cdt.lsp.editor.ui.properties.LspEditorPropertiesPage;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class LsPreferenceInitializer extends AbstractPreferenceInitializer {
	private static final IPreferenceStore preferenceStore = LspEditorUiPlugin.getDefault().getLsPreferences();

	@Override
	public void initializeDefaultPreferences() {
		var cLanguageServerProvider = LspPlugin.getDefault().getCLanguageServerProvider();
		if (cLanguageServerProvider == null) {
			LspEditorUiPlugin.logError("Cannot determine language server provider");
			return;
		}
		preferenceStore.setDefault(LspEditorPreferences.SERVER_PATH, cLanguageServerProvider.getDefaultServerPath());
		preferenceStore.setDefault(LspEditorPreferences.SERVER_OPTIONS, cLanguageServerProvider.getDefaultOptionsAsString());
		preferenceStore.setDefault(LspEditorPropertiesPage.COMPILE_COMMANDS_DIR, LspEditorPropertiesPage.DEFAULT_COMPILE_COMMANDS_DIR);
		
		preferenceStore.setValue(LspEditorPreferences.SERVER_PATH, cLanguageServerProvider.getServerPath());
		preferenceStore.setValue(LspEditorPreferences.SERVER_OPTIONS, cLanguageServerProvider.getOptionsAsString());
		preferenceStore.setValue(LspEditorPropertiesPage.COMPILE_COMMANDS_DIR, LspEditorPropertiesPage.DEFAULT_COMPILE_COMMANDS_DIR);
	}
}
