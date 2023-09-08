package org.eclipse.cdt.lsp.examples.preferences;

import org.eclipse.cdt.lsp.editor.ConfigurationVisibility;
import org.osgi.service.component.annotations.Component;

@Component(property = { "service.ranking:Integer=100" })
public class HidePreferLsp implements ConfigurationVisibility {

	@Override
	public boolean showPreferLsp(boolean isProjectScope) {
		return false;
	}

}
