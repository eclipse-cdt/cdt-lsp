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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.cdt.lsp.clangd.ClangdConfigurationVisibility;
import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.OsgiPreferenceMetadataStore;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public final class ClangdConfigurationArea {

	private final int columns = 3;
	private final Button prefer;
	private final Text path;
	private final Button tidy;
	private final Text completion;
	private final Button index;
	private final Button pretty;
	private final Text driver;
	private final Text additional;
	private final Group group;
	private ControlEnableState enableState;
	private ClangdConfigurationVisibility visibility;

	private final Map<PreferenceMetadata<Boolean>, Button> buttons;
	private final Map<PreferenceMetadata<String>, Text> texts;
	private final List<Consumer<TypedEvent>> listeners;

	public ClangdConfigurationArea(Composite parent, ClangdMetadata metadata, boolean isProjectScope) {
		this.visibility = PlatformUI.getWorkbench().getService(ClangdConfigurationVisibility.class);
		this.buttons = new HashMap<>();
		this.texts = new HashMap<>();
		this.listeners = new ArrayList<>();
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(columns).create());
		if (visibility.showPreferClangd(isProjectScope)) {
			final SelectionAdapter listener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					enableClangdOptionsGroup(prefer.getSelection());
				}
			};

			this.prefer = createCheckbox(metadata.preferClangd(), composite);
			this.prefer.addSelectionListener(listener);
		} else {
			this.prefer = null;
		}
		this.group = createGroup(composite, LspEditorUiMessages.LspEditorPreferencePage_clangd_options_label);
		this.group.setVisible(visibility.showClangdOptions(isProjectScope));
		this.path = createFileSelector(metadata.clangdPath(), group, this::selectClangdExecutable);
		this.tidy = createCheckbox(metadata.useTidy(), group);
		this.index = createCheckbox(metadata.useBackgroundIndex(), group);
		this.completion = createText(metadata.completionStyle(), group, false);
		this.pretty = createCheckbox(metadata.prettyPrint(), group);
		this.driver = createText(metadata.queryDriver(), group, false);
		this.additional = createText(metadata.additionalOptions(), group, true);
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
	}

	private Group createGroup(Composite parent, String label) {
		Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setText(label);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return group;
	}

	private Button createCheckbox(PreferenceMetadata<Boolean> meta, Composite composite) {
		Button button = new Button(composite, SWT.CHECK);
		button.setLayoutData(GridDataFactory.fillDefaults().span(columns, 1).create());
		button.setData(meta);
		button.setText(meta.name());
		button.setToolTipText(meta.description());
		buttons.put(meta, button);
		return button;
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

	private void selectClangdExecutable(SelectionEvent e) {
		String selected = selectFile(path.getText());
		if (selected != null) {
			path.setText(selected);
			changed(e);
		}

	}

	private String selectFile(String path) {
		FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
		File file = new File(path);
		if (file.isFile()) {
			dialog.setFilterPath(file.toString());
		}
		return dialog.open();
	}

	void addChangeListener(Consumer<TypedEvent> listener) {
		listeners.add(listener);
	}

	void removeChangeListener(Consumer<TypedEvent> listener) {
		listeners.add(listener);
	}

	void changed(TypedEvent event) {
		listeners.forEach(c -> c.accept(event));
	}

	void load(ClangdOptions options) {
		if (prefer != null) {
			prefer.setSelection(options.preferClangd());
			enableClangdOptionsGroup(prefer.getSelection());
		}
		path.setText(options.clangdPath());
		tidy.setSelection(options.useTidy());
		index.setSelection(options.useBackgroundIndex());
		completion.setText(options.completionStyle());
		pretty.setSelection(options.prettyPrint());
		driver.setText(options.queryDriver());
		additional.setText(options.additionalOptions().stream().collect(Collectors.joining(System.lineSeparator())));
	}

	void store(IEclipsePreferences prefs) {
		OsgiPreferenceMetadataStore store = new OsgiPreferenceMetadataStore(prefs);
		buttons.entrySet().forEach(e -> store.save(e.getValue().getSelection(), e.getKey()));
		texts.entrySet().forEach(e -> store.save(e.getValue().getText(), e.getKey()));
	}

	void dispose() {
		listeners.clear();
		buttons.clear();
		texts.clear();
	}

	public boolean optionsChanged(ClangdOptions options) {
		if (!group.isVisible() || (prefer != null && !prefer.getSelection())) {
			return false;
		}
		return !options.clangdPath().equals(path.getText()) || options.useTidy() != tidy.getSelection()
				|| options.useBackgroundIndex() != index.getSelection()
				|| !options.completionStyle().equals(completion.getText())
				|| options.prettyPrint() != pretty.getSelection() || !options.queryDriver().equals(driver.getText())
				|| !options.additionalOptions().stream().collect(Collectors.joining(System.lineSeparator()))
						.equals(additional.getText());
	}

}
