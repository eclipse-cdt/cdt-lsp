package org.eclipse.cdt.lsp.internal.ui;

import org.eclipse.cdt.lsp.editor.EditorMetadata;
import org.eclipse.cdt.lsp.editor.EditorOptions;
import org.eclipse.cdt.lsp.ui.ConfigurationArea;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.OsgiPreferenceMetadataStore;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class SaveActionsConfigurationArea extends ConfigurationArea {

	private final Button format;
	private final Button formatAll;
	private final Button formatEdited;

	public SaveActionsConfigurationArea(Composite parent, EditorMetadata metadata, boolean isProjectScope) {
		super(1);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(columns).create());

		this.format = createButton(metadata.formatOnSave(), composite, SWT.CHECK, 0);
		this.formatAll = createButton(metadata.formatAllLines(), composite, SWT.RADIO, 15);
		this.formatEdited = createButton(metadata.formatEditedLines(), composite, SWT.RADIO, 15);

		final SelectionAdapter formatListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				var selection = format.getSelection();
				formatAll.setEnabled(selection);
				formatEdited.setEnabled(selection);
			}
		};
		this.format.addSelectionListener(formatListener);
	}

	@Override
	public void load(Object options, boolean enable) {
		if (options instanceof EditorOptions editorOptions) {
			format.setSelection(editorOptions.formatOnSave());
			formatAll.setSelection(editorOptions.formatAllLines());
			formatEdited.setSelection(editorOptions.formatEditedLines());
			format.setEnabled(enable);
			formatAll.setEnabled(enable && format.getSelection());
			formatEdited.setEnabled(enable && format.getSelection());
		}
	}

	@Override
	public void store(IEclipsePreferences prefs) {
		OsgiPreferenceMetadataStore store = new OsgiPreferenceMetadataStore(prefs);
		buttons.entrySet().forEach(e -> store.save(e.getValue().getSelection(), e.getKey()));
	}

}
