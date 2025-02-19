/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/
package org.eclipse.cdt.lsp.clangd.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.cdt.lsp.ui.ConfigurationArea;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.OsgiPreferenceMetadataStore;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public final class ContentAssistConfigurationArea extends ConfigurationArea<ClangdOptions> {

	private final Button fillFunctionArguments;
	private final Group group;

	public ContentAssistConfigurationArea(Composite parent, boolean isProjectScope) {
		super(1);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(columns).create());
		this.group = createGroup(composite, LspEditorUiMessages.ContentAssistConfigurationPage_insertion_group_name, 3);
		this.fillFunctionArguments = createButton(ClangdMetadata.Predefined.fillFunctionArguments, group, SWT.CHECK, 0);
	}

	@Override
	public void load(ClangdOptions options, boolean enable) {
		fillFunctionArguments.setSelection(options.fillFunctionArguments());
		fillFunctionArguments.setEnabled(enable);
	}

	@Override
	public void store(IEclipsePreferences prefs) {
		OsgiPreferenceMetadataStore store = new OsgiPreferenceMetadataStore(prefs);
		buttons.entrySet().forEach(e -> store.save(e.getValue().getSelection(), e.getKey()));
	}

	@Override
	public List<String> getPreferenceKeys() {
		var list = new ArrayList<String>(1);
		list.add(ClangdMetadata.Predefined.fillFunctionArguments.identifer());
		return list;
	}

	public boolean optionsChanged(ClangdOptions options) {
		return options.fillFunctionArguments() != fillFunctionArguments.getSelection();
	}

}
