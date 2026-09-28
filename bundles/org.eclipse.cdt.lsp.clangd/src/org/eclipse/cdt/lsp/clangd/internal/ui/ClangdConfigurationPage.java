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

package org.eclipse.cdt.lsp.clangd.internal.ui;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.cdt.lsp.ui.ConfigurationArea;
import org.eclipse.cdt.lsp.ui.ConfigurationPage;
import org.eclipse.cdt.lsp.util.LspUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;

public final class ClangdConfigurationPage extends ConfigurationPage<ClangdConfiguration, ClangdOptions> {

	static final String PREFERENCE_PAGE_ID = "org.eclipse.cdt.lsp.clangd.editor.preferencePage"; //$NON-NLS-1$

	@Override
	protected ClangdConfiguration getConfiguration(IWorkbench workbench) {
		return workbench.getService(ClangdConfiguration.class);
	}

	@Override
	protected ClangdOptions configurationDefaults() {
		return configuration.defaults();
	}

	@Override
	protected ClangdOptions configurationOptions(IAdaptable element) {
		return configuration.options(element);
	}

	@Override
	protected ConfigurationArea<ClangdOptions> getConfigurationArea(Composite composite, boolean isProjectScope) {
		return new ClangdConfigurationArea(composite, isProjectScope);
	}

	@Override
	protected String getPreferenceId() {
		return PREFERENCE_PAGE_ID;
	}

	@Override
	public boolean performOk() {
		var configSettingsChanged = configurationSettingsChanged();
		var projectOptionsDifferFromWorkspace = projectOptionsDifferFromWorkspace();
		var done = super.performOk();
		if (done && LspUtils.isLsActive()
				&& (((!projectScope().isPresent() || useProjectSettings()) && configSettingsChanged)
						|| projectOptionsDifferFromWorkspace)) {
			LspUtils.restartClangd();
		}
		return done;
	}

	/**
	 * Returns true when the page settings differ from the stored.
	 * @return
	 */
	private boolean configurationSettingsChanged() {
		return ((ClangdConfigurationArea) area).optionsChanged(configuration.options(getElement()));
	}

	/**
	 * Returns true when project scope AND the 'Enable project-specific settings' check-box has been modified AND
	 *  the current project page settings differ from the stored options in workspace preferences.
	 */
	private boolean projectOptionsDifferFromWorkspace() {
		return hasProjectSpecificOptions() != useProjectSettings()
				&& ((ClangdConfigurationArea) area).optionsChanged(configuration.options(null));
	}

	@Override
	protected boolean hasProjectSpecificOptions() {
		return projectScope()//
				.map(p -> p.getNode(configuration.qualifier()))//
				.filter(ClangdConfigurationPage::hasClangdProjectSpecificOptions)//
				.isPresent();
	}

	static boolean hasClangdProjectSpecificOptions(IEclipsePreferences preferences) {
		return preferences.get(ClangdMetadata.Predefined.clangdPath.identifer(), null) != null
				|| preferences.get(ClangdMetadata.Predefined.useTidy.identifer(), null) != null
				|| preferences.get(ClangdMetadata.Predefined.useBackgroundIndex.identifer(), null) != null
				|| preferences.get(ClangdMetadata.Predefined.completionStyle.identifer(), null) != null
				|| preferences.get(ClangdMetadata.Predefined.prettyPrint.identifer(), null) != null
				|| preferences.get(ClangdMetadata.Predefined.queryDriver.identifer(), null) != null
				|| preferences.get(ClangdMetadata.Predefined.additionalOptions.identifer(), null) != null
				|| preferences.get(ClangdMetadata.Predefined.logToConsole.identifer(), null) != null
				|| preferences.get(ClangdMetadata.Predefined.validateClangdOptions.identifer(), null) != null;
	}

}
