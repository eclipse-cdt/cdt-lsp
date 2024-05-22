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

package org.eclipse.cdt.lsp.clangd.internal.ui;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.cdt.lsp.config.Configuration;
import org.eclipse.cdt.lsp.ui.ConfigurationArea;
import org.eclipse.cdt.lsp.ui.EditorConfigurationPage;
import org.eclipse.cdt.lsp.util.LspUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public final class ClangdConfigurationPage extends EditorConfigurationPage {

	private final String id = "org.eclipse.cdt.lsp.clangd.editor.preferencePage"; //$NON-NLS-1$

	@Override
	public void init(IWorkbench workbench) {
		this.configuration = workbench.getService(ClangdConfiguration.class);
		this.workspace = workbench.getService(IWorkspace.class);
	}

	@Override
	protected Configuration getConfiguration() {
		return PlatformUI.getWorkbench().getService(ClangdConfiguration.class);
	}

	@Override
	protected ConfigurationArea getConfigurationArea(Composite composite, boolean isProjectScope) {
		return new ClangdConfigurationArea(composite, (ClangdMetadata) configuration.metadata(), isProjectScope);
	}

	@Override
	protected String getPreferenceId() {
		return id;
	}

	@Override
	public boolean performOk() {
		var configSettingsChanged = configurationSettingsChanged();
		var projectOptionsDifferFromWorkspace = projectOptionsDifferFromWorkspace();
		var done = super.performOk();
		if (done && isLsActive() && (((!projectScope().isPresent() || useProjectSettings()) && configSettingsChanged)
				|| projectOptionsDifferFromWorkspace)) {
			restartClangd();
		}
		return done;
	}

	private void restartClangd() {
		LspUtils.getLanguageServers().forEach(w -> w.restart());
	}

	/**
	 * Returns true when the page settings differ from the stored.
	 * @return
	 */
	private boolean configurationSettingsChanged() {
		return ((ClangdConfigurationArea) area).optionsChanged((ClangdOptions) configuration.options(getElement()));
	}

	/**
	 * Returns true when project scope AND the 'Enable project-specific settings' check-box has been modified AND
	 *  the current project page settings differ from the stored options in workspace preferences.
	 */
	private boolean projectOptionsDifferFromWorkspace() {
		return hasProjectSpecificOptions() != useProjectSettings()
				&& ((ClangdConfigurationArea) area).optionsChanged((ClangdOptions) configuration.options(null));
	}

	private boolean isLsActive() {
		return LspUtils.getLanguageServers().findAny().isPresent();
	}

	@Override
	protected boolean hasProjectSpecificOptions() {
		return projectScope()//
				.map(p -> p.getNode(configuration.qualifier()))//
				.map(n -> n.get(((ClangdMetadata) configuration.metadata()).clangdPath().identifer(), null))//
				.isPresent();
	}

}
