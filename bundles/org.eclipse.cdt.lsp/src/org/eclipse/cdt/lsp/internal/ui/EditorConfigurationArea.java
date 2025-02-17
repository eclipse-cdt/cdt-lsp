/*******************************************************************************
 * Copyright (c) 2023, 2025 ArSysOp.
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
package org.eclipse.cdt.lsp.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.lsp.editor.ConfigurationVisibility;
import org.eclipse.cdt.lsp.editor.EditorMetadata;
import org.eclipse.cdt.lsp.editor.EditorOptions;
import org.eclipse.cdt.lsp.ui.ConfigurationArea;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.OsgiPreferenceMetadataStore;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

public final class EditorConfigurationArea extends ConfigurationArea<EditorOptions> {

	private final Button prefer;
	private ConfigurationVisibility visibility;

	public EditorConfigurationArea(Composite parent, boolean isProjectScope) {
		super(1);
		this.visibility = PlatformUI.getWorkbench().getService(ConfigurationVisibility.class);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(columns).create());
		if (visibility.showPreferLsp(isProjectScope)) {
			this.prefer = createButton(EditorMetadata.preferLspEditor, composite, SWT.CHECK, 0);
		} else {
			this.prefer = null;
		}
	}

	@Override
	public void store(IEclipsePreferences prefs) {
		OsgiPreferenceMetadataStore store = new OsgiPreferenceMetadataStore(prefs);
		buttons.entrySet().forEach(e -> store.save(e.getValue().getSelection(), e.getKey()));
	}

	@Override
	public void load(EditorOptions options, boolean enable) {
		if (prefer != null) {
			prefer.setSelection(options.preferLspEditor());
			prefer.setEnabled(enable);
		}
	}

	@Override
	public List<String> getPreferenceKeys() {
		var list = new ArrayList<String>(1);
		list.add(EditorMetadata.preferLspEditor.identifer());
		return list;
	}

}
