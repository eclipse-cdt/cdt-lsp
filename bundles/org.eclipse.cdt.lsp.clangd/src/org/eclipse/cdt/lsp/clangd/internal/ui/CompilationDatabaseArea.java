/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.cdt.lsp.clangd.internal.config.ClangdCompilationDatabaseStatus;
import org.eclipse.cdt.lsp.clangd.internal.config.ClangdCompilationDatabaseStatus.Source;
import org.eclipse.cdt.lsp.clangd.internal.config.ClangdCompilationDatabaseSupport;
import org.eclipse.cdt.lsp.ui.ConfigurationArea;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.OsgiPreferenceMetadataStore;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Project/workspace UI for clangd compilation database preferences.
 */
public final class CompilationDatabaseArea extends ConfigurationArea<ClangdOptions> {
	// Classic CDT managed-build projects do not expose an Eclipse build configuration here even
	// though their default build folder is "Debug", so use that as the initial custom directory.
	private static final String DEFAULT_CLASSIC_BUILD_DIRECTORY = "Debug"; //$NON-NLS-1$

	private final Button setCompilationDatabase;
	private final Group compilationDatabaseGroup;
	private final Text compilationDatabaseOverride;
	private final Button compilationDatabaseOverrideBrowse;
	private final Text compilationDatabaseStatus;
	private final Label compilationDatabaseSource;
	private final Label compilationDatabaseLocation;
	private final Label compilationDatabaseBuildConfiguration;
	private final IProject project;
	private final ClangdCompilationDatabaseSupport compilationDatabaseSupport;
	private final ServiceCaller<ClangdConfiguration> configuration = new ServiceCaller<>(getClass(),
			ClangdConfiguration.class);
	private final Map<PreferenceMetadata<String>, Text> texts;
	private ControlEnableState enableState;
	private boolean enableProjectSpecificSettings = true;

	public CompilationDatabaseArea(Composite parent, boolean isProjectScope, IProject project) {
		super(3);
		this.project = project;
		this.compilationDatabaseSupport = new ClangdCompilationDatabaseSupport();
		this.texts = new HashMap<>();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(columns).create());
		this.setCompilationDatabase = createButton(ClangdMetadata.Predefined.setCompilationDatabase, composite, SWT.CHECK, 0);
		this.setCompilationDatabase.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			boolean enabled = updateCompilationDatabaseControls();
			refreshCompilationDatabaseStatus(enabled);
			changed(e);
		}));
		this.compilationDatabaseGroup = createGroup(composite,
				LspEditorUiMessages.LspEditorPreferencePage_compilation_database_group, 3);
		this.compilationDatabaseStatus = createStatusTextValue(compilationDatabaseGroup,
				LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status);
		this.compilationDatabaseSource = createStatusValue(compilationDatabaseGroup,
				LspEditorUiMessages.LspEditorPreferencePage_compilation_database_source);
		this.compilationDatabaseLocation = createStatusValue(compilationDatabaseGroup,
				LspEditorUiMessages.LspEditorPreferencePage_compilation_database_location);
		this.compilationDatabaseBuildConfiguration = createStatusValue(compilationDatabaseGroup,
				LspEditorUiMessages.LspEditorPreferencePage_compilation_database_build_configuration);
		this.compilationDatabaseOverride = createText(ClangdMetadata.Predefined.compilationDatabaseOverride,
				compilationDatabaseGroup, false, 1);
		this.compilationDatabaseOverride.addKeyListener(KeyListener.keyReleasedAdapter(e -> {
			refreshCompilationDatabaseStatus(isCompilationDatabaseEnabled());
			changed(e);
		}));
		this.compilationDatabaseOverrideBrowse = new Button(compilationDatabaseGroup, SWT.NONE);
		this.compilationDatabaseOverrideBrowse
				.setText(LspEditorUiMessages.LspEditorPreferencePage_compilation_database_browse_directory);
		this.compilationDatabaseOverrideBrowse
				.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::selectCompilationDatabaseDirectory));
		if (!isProjectScope) {
			compilationDatabaseStatus.setEnabled(false);
			compilationDatabaseSource.setEnabled(false);
			compilationDatabaseLocation.setEnabled(false);
			compilationDatabaseBuildConfiguration.setEnabled(false);
		}
	}

	private Text createText(PreferenceMetadata<String> meta, Composite composite, boolean multiLine, int horizontalSpan) {
		Label label = new Label(composite, SWT.NONE);
		label.setText(meta.name());
		label.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).create());
		Text text = new Text(composite, multiLine ? SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL : SWT.BORDER);
		text.setToolTipText(meta.description());
		text.setData(meta);
		text.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(horizontalSpan, 1)
				.hint(SWT.DEFAULT, multiLine ? 3 * text.getLineHeight() : SWT.DEFAULT).create());
		texts.put(meta, text);
		text.addKeyListener(KeyListener.keyReleasedAdapter(this::changed));
		return text;
	}

	private Label createStatusValue(Composite composite, String labelText) {
		Label label = new Label(composite, SWT.NONE);
		label.setText(labelText);
		label.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).create());
		Label value = new Label(composite, SWT.NONE);
		value.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(columns - 1, 1).create());
		return value;
	}

	private Text createStatusTextValue(Composite composite, String labelText) {
		Label label = new Label(composite, SWT.NONE);
		label.setText(labelText);
		label.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).create());
		Text value = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		value.setEditable(false);
		value.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(columns - 1, 1).create());
		((GridData) value.getLayoutData()).heightHint = value.getLineHeight() * 3;
		value.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		return value;
	}

	private void selectCompilationDatabaseDirectory(SelectionEvent event) {
		DirectoryDialog dialog = new DirectoryDialog(compilationDatabaseOverride.getShell());
		dialog.setText(LspEditorUiMessages.LspEditorPreferencePage_compilation_database_override);
		if (!compilationDatabaseOverride.getText().isBlank()) {
			dialog.setFilterPath(compilationDatabaseOverride.getText());
		}
		String selected = dialog.open();
		if (selected != null) {
			compilationDatabaseOverride.setText(selected);
			refreshCompilationDatabaseStatus(isCompilationDatabaseEnabled());
			changed(event);
		}
	}

	private void enablePreferenceContent(boolean enableProjectSettings) {
		enableProjectSpecificSettings = enableProjectSettings;
		setCompilationDatabase.setEnabled(enableProjectSettings);
		boolean resultingEnable = updateCompilationDatabaseControls();
		refreshCompilationDatabaseStatus(resultingEnable);
	}

	private boolean enableCompilationDatabaseGroup() {
		var enable = isCompilationDatabaseEnabled();
		if (enableState != null) {
			enableState.restore();
		}
		enableState = enable ? null : ControlEnableState.disable(compilationDatabaseGroup);
		return enable;
	}

	private boolean isCompilationDatabaseEnabled() {
		return (enableProjectSpecificSettings && setCompilationDatabase.getSelection()) || workspaceCompilationDatabaseEnabled();
	}

	private boolean workspaceCompilationDatabaseEnabled() {
		boolean[] enabled = new boolean[1];
		configuration.call(c -> enabled[0] = c.options(null).setCompilationDatabase());
		return enabled[0];
	}

	private boolean updateCompilationDatabaseControls() {
		boolean enabled = enableCompilationDatabaseGroup();
		compilationDatabaseOverride.setEnabled(enabled);
		compilationDatabaseOverrideBrowse.setEnabled(enabled);
		return enabled;
	}

	private void refreshCompilationDatabaseStatus(boolean enabled) {
		if (project == null) {
			compilationDatabaseStatus.setText(""); //$NON-NLS-1$
			compilationDatabaseSource.setText(""); //$NON-NLS-1$
			compilationDatabaseLocation.setText(""); //$NON-NLS-1$
			compilationDatabaseBuildConfiguration.setText(""); //$NON-NLS-1$
			return;
		}
		ClangdCompilationDatabaseStatus status = compilationDatabaseSupport.status(project, enabled,
				compilationDatabaseOverride.getText());
		compilationDatabaseStatus.setText(status.message());
		compilationDatabaseStatus.setToolTipText(status.message());
		String sourceLabel = sourceLabel(status);
		compilationDatabaseSource.setText(sourceLabel);
		compilationDatabaseSource.setToolTipText(sourceLabel);
		String compileCommandsPath = status.compileCommandsPath().isBlank() ? "-" : status.compileCommandsPath(); //$NON-NLS-1$
		compilationDatabaseLocation.setText(compileCommandsPath);
		compilationDatabaseLocation.setToolTipText(compileCommandsPath);
		String buildConfiguration = status.buildConfiguration().isBlank() ? "-" : status.buildConfiguration(); //$NON-NLS-1$
		compilationDatabaseBuildConfiguration.setText(buildConfiguration);
		compilationDatabaseBuildConfiguration.setToolTipText(buildConfiguration);
		compilationDatabaseStatus.getParent().layout(true, true);
	}

	private String sourceLabel(ClangdCompilationDatabaseStatus status) {
		if (!status.automaticManagementEnabled()) {
			return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_source_disabled;
		}
		if (status.source() == Source.CUSTOM) {
			return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_source_custom;
		}
		if (status.source() == Source.ANCESTORS) {
			return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_source_ancestors;
		}
		if (status.source() == Source.AUTOMATIC) {
			return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_source_automatic;
		}
		return LspEditorUiMessages.LspEditorPreferencePage_compilation_database_source_not_detected;
	}

	@Override
	public void load(ClangdOptions options, boolean enable) {
		setCompilationDatabase.setSelection(options.setCompilationDatabase());
		compilationDatabaseOverride.setText(defaultCompilationDatabaseOverride(options));
		enablePreferenceContent(enable);
	}

	private String defaultCompilationDatabaseOverride(ClangdOptions options) {
		if (!options.compilationDatabaseOverride().isBlank()) {
			return options.compilationDatabaseOverride();
		}
		if (project != null && compilationDatabaseSupport.status(project).buildConfiguration().isBlank()) {
			return DEFAULT_CLASSIC_BUILD_DIRECTORY;
		}
		return options.compilationDatabaseOverride();
	}

	@Override
	public void store(IEclipsePreferences prefs) {
		OsgiPreferenceMetadataStore store = new OsgiPreferenceMetadataStore(prefs);
		buttons.entrySet().forEach(e -> store.save(e.getValue().getSelection(), e.getKey()));
		texts.entrySet().forEach(e -> store.save(e.getValue().getText(), e.getKey()));
	}

	@Override
	public List<String> getPreferenceKeys() {
		var list = new ArrayList<String>(2);
		list.add(ClangdMetadata.Predefined.setCompilationDatabase.identifer());
		list.add(ClangdMetadata.Predefined.compilationDatabaseOverride.identifer());
		return list;
	}

	public boolean optionsChanged(ClangdOptions options) {
		return options.setCompilationDatabase() != setCompilationDatabase.getSelection()
				|| !defaultCompilationDatabaseOverride(options).equals(compilationDatabaseOverride.getText());
	}
}
