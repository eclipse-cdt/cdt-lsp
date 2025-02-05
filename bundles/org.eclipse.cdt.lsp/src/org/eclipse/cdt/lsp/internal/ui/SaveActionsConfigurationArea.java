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

package org.eclipse.cdt.lsp.internal.ui;

import org.eclipse.cdt.lsp.editor.EditorMetadata;
import org.eclipse.cdt.lsp.editor.EditorOptions;
import org.eclipse.cdt.lsp.ui.ConfigurationArea;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.OsgiPreferenceMetadataStore;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public final class SaveActionsConfigurationArea extends ConfigurationArea<EditorOptions> {

	private final Button format;
	private final Button formatAll;
	private final Button formatEdited;

	public SaveActionsConfigurationArea(Composite parent, boolean isProjectScope) {
		super(1);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(columns).create());

		this.format = createButton(EditorMetadata.formatOnSave, composite, SWT.CHECK, 0);
		this.formatAll = createButton(EditorMetadata.formatAllLines, composite, SWT.RADIO, 15);
		this.formatEdited = createButton(EditorMetadata.formatEditedLines, composite, SWT.RADIO, 15);

		final SelectionAdapter formatListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				var selection = format.getSelection();
				formatAll.setEnabled(selection);
				formatEdited.setEnabled(selection);
			}
		};
		this.format.addSelectionListener(formatListener);
	}

	@Override
	public void load(EditorOptions options, boolean enable) {
		format.setSelection(options.formatOnSave());
		formatAll.setSelection(options.formatAllLines());
		formatEdited.setSelection(options.formatEditedLines());
		format.setEnabled(enable);
		formatAll.setEnabled(enable && format.getSelection());
		formatEdited.setEnabled(enable && format.getSelection());
	}

	@Override
	public void store(IEclipsePreferences prefs) {
		OsgiPreferenceMetadataStore store = new OsgiPreferenceMetadataStore(prefs);
		buttons.entrySet().forEach(e -> store.save(e.getValue().getSelection(), e.getKey()));
	}

}
