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
import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.cdt.lsp.clangd.internal.config.ClangdCompilationDatabaseStatus;
import org.eclipse.cdt.lsp.clangd.internal.config.ClangdCompilationDatabaseStatus.Source;
import org.eclipse.cdt.lsp.clangd.internal.config.ClangdCompilationDatabaseSupport;
import org.eclipse.cdt.lsp.ui.ConfigurationArea;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Project/workspace UI for clangd compilation database preferences.
 */
public final class CompilationDatabaseArea extends ConfigurationArea<ClangdOptions> {
	private final Button setCompilationDatabase;
	private final Composite compilationDatabaseComposite;
	private final Text compilationDatabaseOverride;
	private final Button compilationDatabaseOverrideBrowse;
	private final Text compilationDatabaseStatus;
	private final Label compilationDatabaseSource;
	private final Label compilationDatabaseLocation;
	private final Label compilationDatabaseBuildConfiguration;
	private final IProject project;
	private final boolean isProjectScope;
	private final ClangdCompilationDatabaseSupport compilationDatabaseSupport;
	private final Map<PreferenceMetadata<String>, Text> texts;
	private ControlEnableState groupEnableState;
	private boolean enableProjectSpecificSettings = true;
	private Optional<WorkspaceJob> getCompileCommandsPathJob = Optional.empty();
	private Optional<String> compileCommandsPathFromConfigFile = Optional.empty();

	public CompilationDatabaseArea(Composite parent, boolean isProjectScope, IProject project) {
		super(3);
		this.isProjectScope = isProjectScope;
		this.project = project;
		this.compilationDatabaseSupport = new ClangdCompilationDatabaseSupport();
		this.texts = new HashMap<>();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(columns).create());
		this.setCompilationDatabase = createButton(ClangdMetadata.Predefined.setCompilationDatabase, composite,
				SWT.CHECK, 0);
		this.setCompilationDatabase.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> {
			updateCompilationDatabaseControls();
			refreshCompilationDatabaseStatus(effectiveCompilationDatabaseEnabled());
			changed(e);
		}));
		this.compilationDatabaseComposite = createCompilationDatabaseComposite(composite);
		if (isProjectScope) {
			this.compilationDatabaseStatus = createStatusTextValue(compilationDatabaseComposite,
					LspEditorUiMessages.LspEditorPreferencePage_compilation_database_status);
			this.compilationDatabaseSource = createStatusValue(compilationDatabaseComposite,
					LspEditorUiMessages.LspEditorPreferencePage_compilation_database_source);
			this.compilationDatabaseLocation = createStatusValue(compilationDatabaseComposite,
					LspEditorUiMessages.LspEditorPreferencePage_compilation_database_location);
			this.compilationDatabaseBuildConfiguration = createStatusValue(compilationDatabaseComposite,
					LspEditorUiMessages.LspEditorPreferencePage_compilation_database_build_configuration);
			this.getCompileCommandsPathJob = getCompileCommandsPathFromConfigFileJob(project);
		} else {
			this.compilationDatabaseStatus = null;
			this.compilationDatabaseSource = null;
			this.compilationDatabaseLocation = null;
			this.compilationDatabaseBuildConfiguration = null;
		}
		this.compilationDatabaseOverride = createText(ClangdMetadata.Predefined.compilationDatabaseOverride,
				compilationDatabaseComposite, false, 1);
		this.compilationDatabaseOverride.addKeyListener(KeyListener.keyReleasedAdapter(e -> {
			refreshCompilationDatabaseStatus(effectiveCompilationDatabaseEnabled());
			changed(e);
		}));
		this.compilationDatabaseOverrideBrowse = new Button(compilationDatabaseComposite, SWT.NONE);
		this.compilationDatabaseOverrideBrowse
				.setText(LspEditorUiMessages.LspEditorPreferencePage_compilation_database_browse_directory);
		this.compilationDatabaseOverrideBrowse.addSelectionListener(
				SelectionListener.widgetSelectedAdapter(this::selectCompilationDatabaseDirectory));
	}

	private Composite createCompilationDatabaseComposite(Composite parent) {
		if (isProjectScope) {
			return createGroup(parent, LspEditorUiMessages.LspEditorPreferencePage_compilation_database_group, 3);
		}
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(columns).create());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return composite;
	}

	private Text createText(PreferenceMetadata<String> meta, Composite composite, boolean multiLine,
			int horizontalSpan) {
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
			var directory = Path.fromOSString(compilationDatabaseOverride.getText());
			var absolute = directory.isAbsolute() || project == null ? directory
					: project.getLocation().append(directory);
			dialog.setFilterPath(absolute.toOSString());
		} else if (project != null) {
			dialog.setFilterPath(project.getLocation().toOSString());
		}
		String selected = dialog.open();
		if (selected != null) {
			String selectedValue = selected;
			if (project != null && project.getLocation() != null) {
				var selectedPath = Path.fromOSString(selected);
				var projectLocation = project.getLocation();
				if (projectLocation.isPrefixOf(selectedPath)) {
					selectedValue = selectedPath.makeRelativeTo(projectLocation).toOSString();
				}
			}
			compilationDatabaseOverride.setText(selectedValue);
			refreshCompilationDatabaseStatus(effectiveCompilationDatabaseEnabled());
			changed(event);
		}
	}

	private void enablePreferenceContent(boolean enableProjectSettings) {
		enableProjectSpecificSettings = enableProjectSettings;
		setCompilationDatabase.setEnabled(enableProjectSettings);
		updateCompilationDatabaseControls();
		refreshCompilationDatabaseStatus(effectiveCompilationDatabaseEnabled());
	}

	private void updateCompilationDatabaseGroupEnablement() {
		if (!isProjectScope) {
			return;
		}
		if (groupEnableState != null) {
			groupEnableState.restore();
		}
		groupEnableState = enableProjectSpecificSettings ? null
				: ControlEnableState.disable(compilationDatabaseComposite);
	}

	private boolean effectiveCompilationDatabaseEnabled() {
		return setCompilationDatabase.getSelection();
	}

	private boolean areCompilationDatabaseControlsEnabled() {
		if (!isProjectScope) {
			return effectiveCompilationDatabaseEnabled();
		}
		return enableProjectSpecificSettings && effectiveCompilationDatabaseEnabled();
	}

	private boolean updateCompilationDatabaseControls() {
		updateCompilationDatabaseGroupEnablement();
		boolean enabled = areCompilationDatabaseControlsEnabled();
		compilationDatabaseOverride.setEnabled(enabled);
		compilationDatabaseOverrideBrowse.setEnabled(enabled);
		return enabled;
	}

	private void refreshCompilationDatabaseStatus(boolean enabled) {
		if (project == null) {
			return;
		}
		ClangdCompilationDatabaseStatus status = compilationDatabaseSupport.status(project, enabled,
				compilationDatabaseOverride.getText(), compileCommandsPathFromConfigFile);
		compilationDatabaseStatus.setText(status.message());
		compilationDatabaseStatus.setToolTipText(status.message());
		String sourceLabel = sourceLabel(status);
		compilationDatabaseSource.setText(sourceLabel);
		compilationDatabaseSource.setToolTipText(sourceLabel);
		String compileCommandsPath = status.compileCommandsPath().isBlank() ? "-" : status.compileCommandsPath(); //$NON-NLS-1$
		// Schedule a job to read the compile_commands.json path from the .clangd file if it is not already set and the job is available.
		if (status.compileCommandsPath().isBlank() && getCompileCommandsPathJob.isPresent()) {
			getCompileCommandsPathJob.get().schedule();
		}
		compilationDatabaseLocation.setText(compileCommandsPath);
		compilationDatabaseLocation.setToolTipText(compileCommandsPath);
		String buildConfiguration = status.buildConfiguration().isBlank() ? "-" : status.buildConfiguration(); //$NON-NLS-1$
		compilationDatabaseBuildConfiguration.setText(buildConfiguration);
		compilationDatabaseBuildConfiguration.setToolTipText(buildConfiguration);
		compilationDatabaseStatus.getParent().layout(true, true);
	}

	private Optional<WorkspaceJob> getCompileCommandsPathFromConfigFileJob(IProject project) {
		if (project == null) {
			return Optional.empty();
		}
		var configFile = project.getFile(".clangd"); //$NON-NLS-1$
		if (!configFile.exists()) {
			return Optional.empty();
		}
		var updateClangdJob = new WorkspaceJob("Read .clangd") { //$NON-NLS-1$
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				var path = compilationDatabaseSupport.getCurrentPathInConfigFile(project);
				if (path.isEmpty()) {
					return Status.CANCEL_STATUS;
				}
				try {
					var display = compilationDatabaseOverride.getDisplay();
					display.asyncExec(() -> {
						compileCommandsPathFromConfigFile = path;
						if (!compilationDatabaseOverride.isDisposed()) {
							compilationDatabaseOverride.setText(path.get());
							refreshCompilationDatabaseStatus(effectiveCompilationDatabaseEnabled());
						}
					});
				} catch (Exception e) {
					Platform.getLog(getClass()).error(e.getMessage(), e);
				}
				return Status.OK_STATUS;
			}
		};
		updateClangdJob.setRule(configFile);
		updateClangdJob.setSystem(true);
		return Optional.of(updateClangdJob);
	}

	@Override
	public void dispose() {
		if (getCompileCommandsPathJob.isPresent()) {
			getCompileCommandsPathJob.get().cancel();
		}
		super.dispose();
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
		compilationDatabaseOverride.setText(options.compilationDatabaseOverride());
		enablePreferenceContent(enable);
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
				|| !options.compilationDatabaseOverride().equals(compilationDatabaseOverride.getText());
	}
}