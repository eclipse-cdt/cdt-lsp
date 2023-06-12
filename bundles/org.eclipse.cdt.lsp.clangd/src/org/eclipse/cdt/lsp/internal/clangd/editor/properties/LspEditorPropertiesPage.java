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

package org.eclipse.cdt.lsp.internal.clangd.editor.properties;

import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.internal.clangd.ResolveProjectScope;
import org.eclipse.cdt.lsp.internal.clangd.editor.configuration.ClangdConfigurationArea;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public final class LspEditorPropertiesPage extends PropertyPage {

	private final ClangdConfiguration configuration;
	private final IWorkspace workspace;
	private ClangdConfigurationArea area;

	public LspEditorPropertiesPage() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		this.configuration = workbench.getService(ClangdConfiguration.class);
		this.workspace = workbench.getService(IWorkspace.class);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		area = new ClangdConfigurationArea(composite, configuration.metadata());
		area.init(store(), this);
		area.fields().forEach(FieldEditor::load);
		return composite;
	}

	private ScopedPreferenceStore store() {
		Optional<ProjectScope> scope = new ResolveProjectScope(workspace).apply(getElement());
		if (scope.isPresent()) {
			ScopedPreferenceStore store = new ScopedPreferenceStore(scope.get(), configuration.qualifier());
			store.setSearchContexts(new IScopeContext[] { scope.get(), InstanceScope.INSTANCE });
			return store;
		}
		return new ScopedPreferenceStore(InstanceScope.INSTANCE, configuration.qualifier());
	}

	@Override
	protected void performDefaults() {
		area.fields().forEach(FieldEditor::loadDefault);
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		area.fields().forEach(FieldEditor::store);
		return super.performOk();
	}

	@Override
	public void dispose() {
		area.fields().forEach(FieldEditor::dispose);
		super.dispose();
	}

}
