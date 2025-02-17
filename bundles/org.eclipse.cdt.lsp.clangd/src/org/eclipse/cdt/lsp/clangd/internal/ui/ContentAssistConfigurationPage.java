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

import org.eclipse.cdt.lsp.clangd.ClangdContentAssistConfiguration;
import org.eclipse.cdt.lsp.clangd.ClangdContentAssistOptions;
import org.eclipse.cdt.lsp.ui.ConfigurationArea;
import org.eclipse.cdt.lsp.ui.ConfigurationPage;
import org.eclipse.cdt.lsp.util.LspUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;

public final class ContentAssistConfigurationPage
		extends ConfigurationPage<ClangdContentAssistConfiguration, ClangdContentAssistOptions> {
	private final String id = "org.eclipse.cdt.lsp.clangd.editor.contentAssistPreferencePage"; //$NON-NLS-1$

	@Override
	protected ClangdContentAssistConfiguration getConfiguration(IWorkbench workbench) {
		return workbench.getService(ClangdContentAssistConfiguration.class);
	}

	@Override
	protected ClangdContentAssistOptions configurationDefaults() {
		return configuration.defaults();
	}

	@Override
	protected ClangdContentAssistOptions configurationOptions(IAdaptable element) {
		return configuration.options(element);
	}

	@Override
	protected ConfigurationArea<ClangdContentAssistOptions> getConfigurationArea(Composite composite,
			boolean isProjectScope) {
		return new ContentAssistConfigurationArea(composite, isProjectScope);
	}

	@Override
	protected String getPreferenceId() {
		return id;
	}

	@Override
	public boolean performOk() {
		var settingsChanged = configurationSettingsChanged(); // must be called prior to super.performOK(), otherwise we cannot detect pref changes.
		var done = super.performOk();
		if (done && LspUtils.isLsActive() && settingsChanged) {
			LspUtils.restartClangd();
		}
		return done;
	}

	/**
	 * Returns true when the page settings differ from the stored.
	 * @return
	 */
	private boolean configurationSettingsChanged() {
		return ((ContentAssistConfigurationArea) area).optionsChanged(configuration.options(getElement()));
	}

	@Override
	protected boolean hasProjectSpecificOptions() {
		// We support only workspace wide settings for content assist. Because we currently only support a single LS for the whole workspace.
		return false;
	}

}
