package org.eclipse.cdt.lsp.internal.clangd;

import org.eclipse.cdt.lsp.editor.Configuration;
import org.eclipse.cdt.lsp.editor.LanguageServerEnable;
import org.eclipse.cdt.lsp.server.ICLanguageServerEnable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.ServiceCaller;
import org.osgi.service.component.annotations.Component;

@Component(property = { "service.ranking:Integer=0" })
public class ClangdEnable implements ICLanguageServerEnable {

	private final ServiceCaller<Configuration> editorConfiguration = new ServiceCaller<>(getClass(),
			Configuration.class);

	@Override
	public boolean isEnabledFor(IProject project) {
		boolean[] enabled = new boolean[1];
		editorConfiguration.call(c -> enabled[0] = ((LanguageServerEnable) c.options(project)).isEnabledFor(project));
		return enabled[0];
	}

}
