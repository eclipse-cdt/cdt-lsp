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

import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.cdt.lsp.internal.clangd.ResolveProjectScope;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.eclipse.ui.preferences.WorkingCopyManager;
import org.osgi.service.prefs.BackingStoreException;

public final class ClangdConfigurationPage extends PropertyPage implements IWorkbenchPreferencePage {

	private final String id = "org.eclipse.cdt.lsp.clangd.editor.preference"; //$NON-NLS-1$

	private ClangdConfiguration configuration;
	private IWorkspace workspace;

	private IWorkingCopyManager manager;

	private Link link;
	private Button specific;
	private Control control;
	private ClangdConfigurationArea area;

	@Override
	public void init(IWorkbench workbench) {
		this.configuration = workbench.getService(ClangdConfiguration.class);
		this.workspace = workbench.getService(IWorkspace.class);
	}

	@Override
	public void setContainer(IPreferencePageContainer container) {
		super.setContainer(container);
		if (manager == null) {
			manager = Optional.ofNullable(container)//
					.filter(IWorkbenchPreferenceContainer.class::isInstance)//
					.map(IWorkbenchPreferenceContainer.class::cast)//
					.map(IWorkbenchPreferenceContainer::getWorkingCopyManager)//
					.orElseGet(WorkingCopyManager::new);
		}
		if (configuration == null) {
			configuration = PlatformUI.getWorkbench().getService(ClangdConfiguration.class);
		}
		if (workspace == null) {
			workspace = PlatformUI.getWorkbench().getService(IWorkspace.class);
		}
	}

	@Override
	protected Label createDescriptionLabel(Composite parent) {
		if (projectScope().isPresent()) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setFont(parent.getFont());
			composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			specific = new Button(composite, SWT.CHECK);
			specific.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, false));
			specific.setText(LspEditorUiMessages.LspEditorPreferencePage_enable_project_specific);
			specific.setFont(JFaceResources.getDialogFont());
			specific.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> specificSelected()));
			link = createLink(composite, LspEditorUiMessages.LspEditorPreferencePage_configure_ws_specific);
			link.setLayoutData(new GridData(SWT.END, SWT.TOP, false, false));
			Label line = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
			line.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
			line.setFont(composite.getFont());
		}
		return super.createDescriptionLabel(parent);
	}

	private void specificSelected() {
		enableProjectSpecificSettings(specific.getSelection());
		refreshWidgets(configuration.options(getElement()));
	}

	private Link createLink(Composite composite, String text) {
		Link link = new Link(composite, SWT.NONE);
		link.setFont(composite.getFont());
		link.setText("<A>" + text + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, null)
						.open() == Window.OK) {
					refreshWidgets(configuration.options(getElement()));
				}
			}
		});
		return link;
	}

	@Override
	protected Control createContents(Composite parent) {
		var isProjectScope = projectScope().isPresent();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		composite.setFont(parent.getFont());
		control = createPreferenceContent(composite, isProjectScope);
		control.setLayoutData(new GridData(GridData.FILL_BOTH));
		if (isProjectScope) {
			enableProjectSpecificSettings(hasProjectSpecificOptions());
		}
		refreshWidgets(configuration.options(getElement()));
		Dialog.applyDialogFont(composite);
		return composite;
	}

	private Control createPreferenceContent(Composite parent, boolean isProjectScope) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());
		composite.setFont(parent.getFont());
		area = new ClangdConfigurationArea(composite, configuration.metadata(), isProjectScope);
		return composite;
	}

	private void refreshWidgets(ClangdOptions options) {
		setErrorMessage(null);
		area.load(options, useProjectSettings() || !projectScope().isPresent());
	}

	private Optional<ProjectScope> projectScope() {
		return new ResolveProjectScope(workspace).apply(getElement());
	}

	@Override
	protected void performDefaults() {
		if (useProjectSettings()) {
			enableProjectSpecificSettings(false);
		}
		IEclipsePreferences prefs = manager.getWorkingCopy(scope().getNode(configuration.qualifier()));
		try {
			for (String key : prefs.keys()) {
				prefs.remove(key);
			}
		} catch (BackingStoreException e) {
			Platform.getLog(getClass()).error("Unable to restore default values.", e); //$NON-NLS-1$
		}
		refreshWidgets(configuration.defaults());
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		var restartRequired = area.optionsChanged(configuration.options(getElement())) && isLsActive();
		IEclipsePreferences prefs;
		if (projectScope().isPresent()) {
			prefs = manager.getWorkingCopy(projectScope().get().getNode(configuration.qualifier()));
			if (!useProjectSettings()) {
				try {
					for (String key : prefs.keys()) {
						prefs.remove(key);
					}
				} catch (BackingStoreException e) {
					Platform.getLog(getClass()).error("Unable to reset project preferences.", e); //$NON-NLS-1$
				}
				prefs = null;
			}
		} else {
			prefs = manager.getWorkingCopy(InstanceScope.INSTANCE.getNode(configuration.qualifier()));
		}
		if (prefs != null) {
			area.store(prefs);
		}
		try {
			manager.applyChanges();
		} catch (BackingStoreException e) {
			Platform.getLog(getClass()).error("Unable to save preferences.", e); //$NON-NLS-1$
			return false;
		}
		if (restartRequired) {
			openRestartDialog();
		}
		return true;
	}

	private boolean isLsActive() {
		return LanguageServiceAccessor.getStartedWrappers(null, null, true).stream()
				.filter(w -> "org.eclipse.cdt.lsp.server".equals(w.serverDefinition.id)).findAny().isPresent(); //$NON-NLS-1$
	}

	private void openRestartDialog() {
		final var dialog = new MessageDialog(getShell(),
				LspEditorUiMessages.LspEditorPreferencePage_restart_dialog_title, null,
				LspEditorUiMessages.LspEditorPreferencePage_restart_dialog_message, MessageDialog.INFORMATION,
				new String[] { IDialogConstants.NO_LABEL, LspEditorUiMessages.LspEditorPreferencePage_restart_button },
				1);
		if (dialog.open() == 1) {
			LanguageServiceAccessor.getStartedWrappers(null, null, true).stream()
					.filter(w -> "org.eclipse.cdt.lsp.server".equals(w.serverDefinition.id)) //$NON-NLS-1$
					.forEach(w -> w.stop());
		}
	}

	private IScopeContext scope() {
		return projectScope().map(IScopeContext.class::cast).orElse(InstanceScope.INSTANCE);
	}

	private boolean hasProjectSpecificOptions() {
		return projectScope()//
				.map(p -> p.getNode(configuration.qualifier()))//
				.map(n -> n.get(configuration.metadata().preferClangd().identifer(), null))//
				.isPresent();
	}

	private boolean useProjectSettings() {
		return Optional.ofNullable(specific)//
				.map(s -> s.getSelection())//
				.orElse(Boolean.FALSE);
	}

	private void enableProjectSpecificSettings(boolean use) {
		specific.setSelection(use);
		updateLinkVisibility();
	}

	private void updateLinkVisibility() {
		Optional.ofNullable(link)//
				.filter(l -> !l.isDisposed())//
				.ifPresent(l -> l.setEnabled(!useProjectSettings()));
	}

	@Override
	public void dispose() {
		Optional.ofNullable(area).ifPresent(ClangdConfigurationArea::dispose);
		super.dispose();
	}

}
