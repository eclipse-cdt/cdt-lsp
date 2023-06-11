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
 * Alexander Fedorov (ArSysOp) - rework access to preferences
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.clangd.editor.preferences;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.internal.clangd.editor.LspEditorUiMessages;
import org.eclipse.cdt.lsp.internal.clangd.editor.configuration.ClangdConfigurationArea;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public final class LspEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private IWorkbench workbench;

	public LspEditorPreferencePage() {
		super(GRID);
		setDescription(LspEditorUiMessages.LspEditorPreferencePage_description);
	}

	@Override
	public void init(IWorkbench workbench) {
		this.workbench = workbench;
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, //
				workbench.getService(ClangdConfiguration.class).qualifier()));
	}

	@Override
	public void createFieldEditors() {
		new ClangdConfigurationArea(getFieldEditorParent(), //
				workbench.getService(ClangdConfiguration.class).metadata())//
						.fields()//
						.forEach(this::addField);
	}

}
