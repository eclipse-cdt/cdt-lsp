package org.eclipse.cdt.lsp.internal.ui;

import org.eclipse.cdt.lsp.editor.ConfigurationVisibility;
import org.osgi.service.component.annotations.Component;

@Component(property = { "service.ranking:Integer=0" })
public class DefaultConfigurationVisibility implements ConfigurationVisibility {

	@Override
	public boolean showPreferLsp(boolean isProjectScope) {
		return true;
	}

}
