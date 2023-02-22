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

import org.eclipse.cdt.lsp.editor.ui.LspEditorUiPlugin;
import org.eclipse.cdt.lsp.editor.ui.LspEditorUiMessages;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class LspEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public LspEditorPreferencePage() {
		super(GRID);
		setPreferenceStore(LspEditorUiPlugin.getDefault().getLspEditorPreferences());
		setDescription(LspEditorUiMessages.LspEditorPreferencePage_description);
	}

	@Override
	public void init(IWorkbench workbench) {
	}
	

	@Override
	public void createFieldEditors() {
		PreferenceMetadata<Boolean> prefer = LspEditorPreferences.getPreferenceMetadata();
		addField(new BooleanFieldEditor(prefer.identifer(), prefer.name(), getFieldEditorParent()));
	}

}
