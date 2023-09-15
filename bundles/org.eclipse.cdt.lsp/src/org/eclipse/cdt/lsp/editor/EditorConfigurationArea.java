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
package org.eclipse.cdt.lsp.editor;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.OsgiPreferenceMetadataStore;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

public final class EditorConfigurationArea extends ConfigurationArea {

	private final Button prefer;
	private ConfigurationVisibility visibility;

	public EditorConfigurationArea(Composite parent, EditorMetadata metadata, boolean isProjectScope) {
		super(1);
		this.visibility = PlatformUI.getWorkbench().getService(ConfigurationVisibility.class);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(columns).create());
		if (visibility.showPreferLsp(isProjectScope)) {
			this.prefer = createButton(metadata.preferLspEditor(), composite, SWT.CHECK, 0);
		} else {
			this.prefer = null;
		}
	}

	@Override
	public void load(Object options, boolean enable) {
		if (options instanceof EditorOptions editorOptions) {
			if (prefer != null) {
				prefer.setSelection(editorOptions.preferLspEditor());
				prefer.setEnabled(enable);
			}
		}
	}

	@Override
	public void store(IEclipsePreferences prefs) {
		OsgiPreferenceMetadataStore store = new OsgiPreferenceMetadataStore(prefs);
		buttons.entrySet().forEach(e -> store.save(e.getValue().getSelection(), e.getKey()));
	}

}
