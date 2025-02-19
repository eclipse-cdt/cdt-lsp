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

import org.eclipse.cdt.lsp.editor.EditorConfiguration;
import org.eclipse.cdt.lsp.editor.EditorMetadata;
import org.eclipse.cdt.lsp.editor.EditorOptions;
import org.eclipse.cdt.lsp.ui.ConfigurationArea;
import org.eclipse.cdt.lsp.ui.ConfigurationPage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;

public final class SaveActionsConfigurationPage extends ConfigurationPage<EditorConfiguration, EditorOptions> {
	private final String id = "org.eclipse.cdt.lsp.editor.SaveActionsPreferencePage"; //$NON-NLS-1$

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
		return new SaveActionsConfigurationArea(composite, isProjectScope);
	}

	@Override
	protected String getPreferenceId() {
		return id;
	}

	@Override
	protected boolean hasProjectSpecificOptions() {
		return projectScope()//
				.map(p -> p.getNode(configuration.qualifier()))//
				.map(n -> n.get(EditorMetadata.Predefined.formatAllLines.identifer(), null))//
				.isPresent();
	}

}
