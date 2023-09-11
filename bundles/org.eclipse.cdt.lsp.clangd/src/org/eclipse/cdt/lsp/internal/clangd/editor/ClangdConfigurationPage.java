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

package org.eclipse.cdt.lsp.internal.clangd.editor;

import org.eclipse.cdt.lsp.LspUtils;
import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.cdt.lsp.editor.Configuration;
import org.eclipse.cdt.lsp.editor.ConfigurationArea;
import org.eclipse.cdt.lsp.editor.EditorConfigurationPage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
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
		var restartRequired = isRestartRequired();
		var done = super.performOk();
		if (done && restartRequired) {
			openRestartDialog();
		}
		return done;
	}

	private boolean isRestartRequired() {
		return ((ClangdConfigurationArea) area).optionsChanged((ClangdOptions) configuration.options(getElement()))
				&& isLsActive();
	}

	private boolean isLsActive() {
		return LspUtils.getLanguageServers().findAny().isPresent();
	}

	private void openRestartDialog() {
		final var dialog = new MessageDialog(getShell(),
				LspEditorUiMessages.LspEditorPreferencePage_restart_dialog_title, null,
				LspEditorUiMessages.LspEditorPreferencePage_restart_dialog_message, MessageDialog.INFORMATION,
				new String[] { IDialogConstants.NO_LABEL, LspEditorUiMessages.LspEditorPreferencePage_restart_button },
				1);
		if (dialog.open() == 1) {
			LspUtils.getLanguageServers().forEach(w -> w.stop());
		}
	}

	@Override
	protected boolean hasProjectSpecificOptions() {
		return projectScope()//
				.map(p -> p.getNode(configuration.qualifier()))//
				.map(n -> n.get(((ClangdMetadata) configuration.metadata()).clangdPath().identifer(), null))//
				.isPresent();
	}

}
