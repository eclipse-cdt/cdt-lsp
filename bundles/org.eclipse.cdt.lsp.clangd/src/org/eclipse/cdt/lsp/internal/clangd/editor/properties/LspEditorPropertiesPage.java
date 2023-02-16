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

package org.eclipse.cdt.lsp.internal.clangd.editor.properties;

import java.util.Optional;

import org.eclipse.cdt.lsp.internal.clangd.editor.LspEditorUiPlugin;
import org.eclipse.cdt.lsp.internal.clangd.editor.preferences.LspEditorPreferences;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;

public class LspEditorPropertiesPage extends PropertyPage {

	private Button preferLspEditorCheckbox;

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		addSettingsSection(composite);
		load();
		return composite;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		preferLspEditorCheckbox.setSelection(LspEditorPreferences.getPreferenceMetadata().defaultValue());
	}

	@Override
	public boolean performOk() {
		Optional<IProject> project = project();
		if (project.isPresent()) {
			IEclipsePreferences node = new ProjectScope(project.get()).getNode(LspEditorUiPlugin.PLUGIN_ID);
			node.putBoolean(LspEditorPreferences.getPreferenceMetadata().identifer(),
					preferLspEditorCheckbox.getSelection());
			try {
				node.flush();
				return true;
			} catch (BackingStoreException e) {
				Platform.getLog(FrameworkUtil.getBundle(getClass())).error(e.getMessage(), e);
			}
		}
		return false;
	}

	private void addSettingsSection(Composite parent) {
		PreferenceMetadata<Boolean> option = LspEditorPreferences.getPreferenceMetadata();
		Composite composite = createDefaultComposite(parent);
		preferLspEditorCheckbox = new Button(composite, SWT.CHECK);
		preferLspEditorCheckbox.setLayoutData(new GridData());
		preferLspEditorCheckbox.setText(option.name());
		preferLspEditorCheckbox.setToolTipText(option.description());
	}

	private void load() {
		Optional<IProject> project = project();
		PreferenceMetadata<Boolean> option = LspEditorPreferences.getPreferenceMetadata();
		if (project.isPresent()) {
			preferLspEditorCheckbox.setSelection(
					Platform.getPreferencesService().getBoolean(LspEditorUiPlugin.PLUGIN_ID, option.identifer(),
							option.defaultValue(), new IScopeContext[] { new ProjectScope(project.get()) }));
		} else {
			preferLspEditorCheckbox.setSelection(option.defaultValue());
		}
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(GridDataFactory.fillDefaults().create());
		return composite;
	}

	private Optional<IProject> project() {
		return Optional.ofNullable(getElement())//
				.filter(IProject.class::isInstance)//
				.map(IProject.class::cast);
	}

}
