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
import org.eclipse.cdt.lsp.config.Configuration;
import org.eclipse.cdt.lsp.internal.messages.LspUiMessages;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
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

public abstract class ConfigurationPage<C extends Configuration, O> extends PropertyPage
		implements IWorkbenchPreferencePage {

	protected C configuration;
	protected IWorkspace workspace;

	protected IWorkingCopyManager manager;

	private Link link;
	private Button specific;
	private Control control;
	//FIXME: AF: we should rework it to private
	protected ConfigurationArea<O> area;

	@Override
	public final void init(IWorkbench workbench) {
		this.configuration = getConfiguration(workbench);
		this.workspace = workbench.getService(IWorkspace.class);
	}

	protected abstract O configurationOptions(IAdaptable element);

	protected abstract O configurationDefaults();

	@Override
	public final void setContainer(IPreferencePageContainer container) {
		super.setContainer(container);
		if (manager == null) {
			manager = Optional.ofNullable(container)//
					.filter(IWorkbenchPreferenceContainer.class::isInstance)//
					.map(IWorkbenchPreferenceContainer.class::cast)//
					.map(IWorkbenchPreferenceContainer::getWorkingCopyManager)//
					.orElseGet(WorkingCopyManager::new);
		}
		if (configuration == null) {
			configuration = getConfiguration(PlatformUI.getWorkbench());
		}
		if (workspace == null) {
			workspace = PlatformUI.getWorkbench().getService(IWorkspace.class);
		}
	}

	protected abstract C getConfiguration(IWorkbench workbench);

	@Override
	protected final Label createDescriptionLabel(Composite parent) {
		if (projectScope().isPresent()) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setFont(parent.getFont());
			composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			specific = new Button(composite, SWT.CHECK);
			specific.setLayoutData(new GridData(SWT.BEGINNING, SWT.TOP, true, false));
			specific.setText(LspUiMessages.LspEditorConfigurationPage_enable_project_specific);
			specific.setFont(JFaceResources.getDialogFont());
			specific.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> specificSelected()));
			link = createLink(composite, LspUiMessages.LspEditorConfigurationPage_configure_ws_specific);
			link.setLayoutData(new GridData(SWT.END, SWT.TOP, false, false));
			Label line = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
			line.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
			line.setFont(composite.getFont());
		}
		return super.createDescriptionLabel(parent);
	}

	private void specificSelected() {
		enableProjectSpecificSettings(specific.getSelection());
		refreshWidgets(configurationOptions(getElement()));
	}

	private Link createLink(Composite composite, String text) {
		Link link = new Link(composite, SWT.NONE);
		link.setFont(composite.getFont());
		link.setText("<A>" + text + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (PreferencesUtil.createPreferenceDialogOn(getShell(), getPreferenceId(),
						new String[] { getPreferenceId() }, null).open() == Window.OK) {
					refreshWidgets(configurationOptions(getElement()));
				}
			}
		});
		return link;
	}

	protected abstract String getPreferenceId();

	@Override
	protected final Control createContents(Composite parent) {
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
		refreshWidgets(configurationOptions(getElement()));
		Dialog.applyDialogFont(composite);
		return composite;
	}

	protected abstract ConfigurationArea<O> getConfigurationArea(Composite composite, boolean isProjectScope);

	private Control createPreferenceContent(Composite parent, boolean isProjectScope) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().create());
		composite.setFont(parent.getFont());
		if (!isProjectScope) {
			createLink(composite, LspUiMessages.LspEditorConfigurationPage_spelling_link,
					LspUiMessages.LspEditorConfigurationPage_spelling_link_tooltip);
			createLink(composite, LspUiMessages.LspEditorConfigurationPage_content_assist_link,
					LspUiMessages.LspEditorConfigurationPage_content_assist_link_tooltip);
			Label line = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
			line.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 2, 1));
		}
		area = getConfigurationArea(composite, isProjectScope);
		return composite;
	}

	private Control createLink(Composite parent, String text, String tooltipText) {
		Link link = new Link(parent, SWT.NONE);
		link.setText(text);
		link.addListener(SWT.Selection,
				event -> PreferencesUtil.createPreferenceDialogOn(getShell(), event.text, null, null));
		link.setToolTipText(tooltipText);
		link.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		return link;
	}

	protected final void refreshWidgets(O options) {
		setErrorMessage(null);
		area.load(options, useProjectSettings() || !projectScope().isPresent());
	}

	protected final Optional<ProjectScope> projectScope() {
		return new ResolveProjectScope(workspace).apply(getElement());
	}

	@Override
	protected final void performDefaults() {
		if (useProjectSettings()) {
			enableProjectSpecificSettings(false);
		}
		IEclipsePreferences prefs = manager.getWorkingCopy(scope().getNode(configuration.qualifier()));
		try {
			var areaKeys = area.getPreferenceKeys();
			for (String key : prefs.keys()) {
				if (areaKeys.contains(key)) {
					prefs.remove(key);
				}
			}
		} catch (BackingStoreException e) {
			Platform.getLog(getClass()).error("Unable to restore default values.", e); //$NON-NLS-1$
		}
		refreshWidgets(configurationDefaults());
		super.performDefaults();
	}

	//FIXME: AF: we should make it final and provide hooks of needed
	@Override
	public boolean performOk() {
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
		return true;
	}

	private IScopeContext scope() {
		return projectScope().map(IScopeContext.class::cast).orElse(InstanceScope.INSTANCE);
	}

	protected abstract boolean hasProjectSpecificOptions();

	protected final boolean useProjectSettings() {
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

	//FIXME: AF: we should make it final and provide hooks of needed
	@Override
	public void dispose() {
		Optional.ofNullable(area).ifPresent(ConfigurationArea::dispose);
		super.dispose();
	}

	@Override
	public void applyData(Object data) {
		Optional.ofNullable(area).ifPresent(area -> area.applyData(data));
	}
}
