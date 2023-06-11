/*******************************************************************************
 * Copyright (c) 2023 ArSysOp.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.clangd.editor.configuration;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PropertyPage;

public final class ClangdConfigurationArea {

	private final Composite parent;
	private final List<FieldEditor> fields;

	public ClangdConfigurationArea(Composite parent, ClangdMetadata metadata) {
		this.parent = parent;
		this.fields = new ArrayList<>();
		fields.add(new AdjustableFieldEditor.BooleanData(metadata.preferClangd(), parent));
		fields.add(new AdjustableFieldEditor.FileData(metadata.clangdPath(), parent));
		fields.add(new AdjustableFieldEditor.BooleanData(metadata.useTidy(), parent));
		fields.add(new AdjustableFieldEditor.BooleanData(metadata.useBackgroundIndex(), parent));
		fields.add(new AdjustableFieldEditor.StringData(metadata.completionStyle(), parent));
		fields.add(new AdjustableFieldEditor.BooleanData(metadata.prettyPrint(), parent));
		fields.add(new AdjustableFieldEditor.StringData(metadata.queryDriver(), parent));
	}

	/**
	 * Required for {@link PropertyPage}, not needed for {@link FieldEditorPreferencePage}
	 *
	 * @param store preferences
	 * @param page to report validation result
	 */
	public void init(IPreferenceStore store, DialogPage page) {
		fields.forEach(f -> f.setPreferenceStore(store));
		fields.forEach(f -> f.setPage(page));
		int columns = fields.stream()//
				.map(FieldEditor::getNumberOfControls)//
				.reduce(1, Math::max);
		((GridLayout) parent.getLayout()).numColumns = columns;
		fields.stream()//
				.filter(AdjustableFieldEditor.class::isInstance)//
				.map(AdjustableFieldEditor.class::cast)//
				.forEach(f -> f.adjust(columns));
	}

	public List<FieldEditor> fields() {
		return new ArrayList<>(fields);
	}

}
