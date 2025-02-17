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

package org.eclipse.cdt.lsp.ui;

import java.util.Optional;

import org.eclipse.cdt.lsp.ResolveProjectScope;
import org.eclipse.cdt.lsp.editor.EditorConfiguration;
import org.eclipse.cdt.lsp.editor.EditorMetadata;
import org.eclipse.cdt.lsp.editor.EditorOptions;
import org.eclipse.cdt.lsp.internal.ui.EditorConfigurationArea;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;

public final class EditorConfigurationPage extends ConfigurationPage<EditorConfiguration, EditorOptions> {

	/**
	 * @noreference This field is not intended to be referenced by clients.
	 */
	public static final String PREFERENCE_PAGE_ID = "org.eclipse.cdt.lsp.editor.preferencePage"; //$NON-NLS-1$
	/**
	 * @noreference This field is not intended to be referenced by clients.
	 */
	public static final String PROPERTY_PAGE_ID = "org.eclipse.cdt.lsp.editor.propertyPage"; //$NON-NLS-1$
	/**
	 * @noreference This field is not intended to be referenced by clients.
	 */
	public static final String HIGHLIGHT_PREFER_LSP = "HIGHLIGHT_PREFER_LSP"; //$NON-NLS-1$

	@Override
	protected EditorConfiguration getConfiguration(IWorkbench workbench) {
		return workbench.getService(EditorConfiguration.class);
	}

	@Override
	protected EditorOptions configurationDefaults() {
		return configuration.defaults();
	}

	@Override
	protected EditorOptions configurationOptions(IAdaptable element) {
		return configuration.options(element);
	}

	@Override
	protected ConfigurationArea<EditorOptions> getConfigurationArea(Composite composite, boolean isProjectScope) {
		return new EditorConfigurationArea(composite, isProjectScope);
	}

	@Override
	protected String getPreferenceId() {
		return PREFERENCE_PAGE_ID;
	}

	@Override
	protected boolean hasProjectSpecificOptions() {
		return projectScope()//
				.map(p -> p.getNode(configuration.qualifier()))//
				.map(n -> n.get(EditorMetadata.Predefined.preferLspEditor.identifer(), null))//
				.isPresent();
	}

	@Override
	public void dispose() {
		Optional.ofNullable(area).ifPresent(ConfigurationArea::dispose);
		super.dispose();
	}

}
