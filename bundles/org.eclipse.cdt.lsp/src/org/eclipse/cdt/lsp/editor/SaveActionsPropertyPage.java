package org.eclipse.cdt.lsp.editor;

import org.eclipse.swt.widgets.Composite;

public class SaveActionsPropertyPage extends EditorPropertyPage {
	private final String id = "org.eclipse.cdt.lsp.editor.SaveActionsPreferencePage"; //$NON-NLS-1$

	@Override
	protected ConfigurationArea getConfigurationArea(Composite composite, boolean isProjectScope) {
		return new SaveActionsConfigurationArea(composite, configuration.metadata(), isProjectScope);
	}

	@Override
	protected String getPreferenceId() {
		return id;
	}

}
