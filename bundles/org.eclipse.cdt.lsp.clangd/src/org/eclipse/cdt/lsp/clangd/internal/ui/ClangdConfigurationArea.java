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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.cdt.lsp.ui.ConfigurationArea;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public final class ClangdConfigurationArea extends ConfigurationArea<ClangdOptions> {

	private final Text path;
	private final Button tidy;
	private final Combo completion;
	private final Button index;
	private final Button pretty;
	private final Text driver;
	private final Text additional;
	private final Button logToConsole;
	private final Button validateOptions;
	private final Group group;
	private ControlEnableState enableState;
	private final Button setCompilationDatabase;
	private ControlEnableState enableSetDatabaseState;

	private final Map<PreferenceMetadata<String>, Text> texts;
	private final Map<PreferenceMetadata<String>, Combo> combos;

	private final static String[] completionOptions = { "detailed", "bundled", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private final static String[] completionsKeys = { LspEditorUiMessages.LspEditorPreferencePage_completion_detailed,
			LspEditorUiMessages.LspEditorPreferencePage_completion_bundled,
			LspEditorUiMessages.LspEditorPreferencePage_completion_default };
	private final Map<String, String> completions;

	public ClangdConfigurationArea(Composite parent, boolean isProjectScope) {
		super(3);
		this.texts = new HashMap<>();
		this.combos = new HashMap<>();
		this.completions = new HashMap<>();
		for (int i = 0; i < completionsKeys.length; i++) {
			completions.put(completionsKeys[i], completionOptions[i]);
		}
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(columns).create());
		this.group = createGroup(composite, LspEditorUiMessages.LspEditorPreferencePage_clangd_options_label, 3);
		this.path = createFileSelector(ClangdMetadata.Predefined.clangdPath, group, this::selectClangdExecutable);
		this.tidy = createButton(ClangdMetadata.Predefined.useTidy, group, SWT.CHECK, 0);
		this.index = createButton(ClangdMetadata.Predefined.useBackgroundIndex, group, SWT.CHECK, 0);
		this.completion = createCombo(ClangdMetadata.Predefined.completionStyle, group, completionsKeys);
		this.pretty = createButton(ClangdMetadata.Predefined.prettyPrint, group, SWT.CHECK, 0);
		this.driver = createText(ClangdMetadata.Predefined.queryDriver, group, false);
		this.additional = createText(ClangdMetadata.Predefined.additionalOptions, group, true);
		if (!isProjectScope) {
			this.logToConsole = createButton(ClangdMetadata.Predefined.logToConsole, group, SWT.CHECK, 0);
			this.validateOptions = createButton(ClangdMetadata.Predefined.validateClangdOptions, group, SWT.CHECK, 0);
		} else {
			this.logToConsole = null;
			this.validateOptions = null;
		}
		this.setCompilationDatabase = createButton(ClangdMetadata.Predefined.setCompilationDatabase, composite,
				SWT.CHECK, 0);
	}

	void enablePreferenceContent(boolean enable) {
		enableClangdOptionsGroup(enable);
	}

	private void enableClangdOptionsGroup(boolean enable) {
		if (enableState != null) {
			enableState.restore();
		}
		if (enable) {
			enableState = null;
		} else {
			enableState = ControlEnableState.disable(group);
		}
		if (enableSetDatabaseState != null) {
			enableSetDatabaseState.restore();
		}
		if (enable) {
			enableSetDatabaseState = null;
		} else {
			enableSetDatabaseState = ControlEnableState.disable(setCompilationDatabase);
		}
	}

	private Text createFileSelector(PreferenceMetadata<String> meta, Composite composite,
			Consumer<SelectionEvent> selector) {
		Label label = new Label(composite, SWT.NONE);
		label.setText(meta.name());
		label.setLayoutData(GridDataFactory.fillDefaults().create());
		Text text = new Text(composite, SWT.BORDER);
		text.setToolTipText(meta.description());
		text.setData(meta);
		texts.put(meta, text);
		text.addKeyListener(KeyListener.keyReleasedAdapter(this::changed));
		text.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(columns - 2, 1).create());
		Button button = new Button(composite, SWT.NONE);
		button.setText(LspEditorUiMessages.LspEditorPreferencePage_browse_button);
		button.setLayoutData(new GridData());
		button.addSelectionListener(SelectionListener.widgetSelectedAdapter(selector));
		return text;
	}

	private Text createText(PreferenceMetadata<String> meta, Composite composite, boolean multiLine) {
		Label label = new Label(composite, SWT.NONE);
		label.setText(meta.name());
		label.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).create());
		Text text = new Text(composite, multiLine ? SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL : SWT.BORDER);
		text.setToolTipText(meta.description());
		text.setData(meta);
		text.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(columns - 1, 1)
				.hint(SWT.DEFAULT, multiLine ? 3 * text.getLineHeight() : SWT.DEFAULT).create());
		texts.put(meta, text);
		text.addKeyListener(KeyListener.keyReleasedAdapter(this::changed));
		return text;
	}

	private Combo createCombo(PreferenceMetadata<String> meta, Composite parent, String[] items) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(meta.name());
		label.setToolTipText(meta.description());
		label.setLayoutData(GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).create());

		Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		combo.setItems(items);
		combo.setData(meta);
		combos.put(meta, combo);

		return combo;
	}

	private void selectClangdExecutable(SelectionEvent e) {
		String selected = selectFile(path.getText());
		if (selected != null) {
			path.setText(selected);
			changed(e);
		}

	}

	private String selectFile(String path) {
		FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
		dialog.setText(LspEditorUiMessages.LspEditorPreferencePage_select_clangd_executable);
		File file = new File(path);
		if (file.isFile()) {
			dialog.setFilterPath(file.getParent());
		}
		return dialog.open();
	}

	@Override
	public void load(ClangdOptions options, boolean enable) {
		path.setText(options.clangdPath());
		tidy.setSelection(options.useTidy());
		index.setSelection(options.useBackgroundIndex());
		for (int i = 0; i < completionOptions.length; i++) {
			if (completionOptions[i].equals(options.completionStyle())) {
				completion.select(i);
			}
		}
		pretty.setSelection(options.prettyPrint());
		driver.setText(options.queryDriver());
		additional.setText(options.additionalOptions().stream().collect(Collectors.joining(System.lineSeparator())));
		enablePreferenceContent(enable);
		if (logToConsole != null) {
			logToConsole.setSelection(options.logToConsole());
		}
		if (validateOptions != null) {
			validateOptions.setSelection(options.validateClangdOptions());
		}
		setCompilationDatabase.setSelection(options.setCompilationDatabase());
	}

	@Override
	public void store(IEclipsePreferences prefs) {
		OsgiPreferenceMetadataStore store = new OsgiPreferenceMetadataStore(prefs);
		buttons.entrySet().forEach(e -> store.save(e.getValue().getSelection(), e.getKey()));
		texts.entrySet().forEach(e -> store.save(e.getValue().getText(), e.getKey()));
		combos.entrySet().forEach(e -> store.save(completions.get(e.getValue().getText()), e.getKey()));
	}

	@Override
	public List<String> getPreferenceKeys() {
		var list = new ArrayList<String>(9);
		list.add(ClangdMetadata.Predefined.additionalOptions.identifer());
		list.add(ClangdMetadata.Predefined.clangdPath.identifer());
		list.add(ClangdMetadata.Predefined.completionStyle.identifer());
		list.add(ClangdMetadata.Predefined.logToConsole.identifer());
		list.add(ClangdMetadata.Predefined.prettyPrint.identifer());
		list.add(ClangdMetadata.Predefined.queryDriver.identifer());
		list.add(ClangdMetadata.Predefined.useBackgroundIndex.identifer());
		list.add(ClangdMetadata.Predefined.useTidy.identifer());
		list.add(ClangdMetadata.Predefined.validateClangdOptions.identifer());
		list.add(ClangdMetadata.Predefined.setCompilationDatabase.identifer());
		return list;
	}

	@Override
	public void dispose() {
		super.dispose();
		texts.clear();
		combos.clear();
	}

	public boolean optionsChanged(ClangdOptions options) {
		return !options.clangdPath().equals(path.getText()) || options.useTidy() != tidy.getSelection()
				|| options.useBackgroundIndex() != index.getSelection()
				|| !options.completionStyle().equals(completions.get(completion.getText()))
				|| options.prettyPrint() != pretty.getSelection() || !options.queryDriver().equals(driver.getText())
				|| !options.additionalOptions().stream().collect(Collectors.joining(System.lineSeparator()))
						.equals(additional.getText())
				|| (logToConsole != null && options.logToConsole() != logToConsole.getSelection())
				|| (validateOptions != null && options.validateClangdOptions() != validateOptions.getSelection());
	}

}
