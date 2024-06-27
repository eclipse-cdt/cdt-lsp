package org.eclipse.cdt.lsp.server;

import org.eclipse.core.resources.IProject;

public interface ICLanguageServerProvider2 extends ICLanguageServerProvider {

	public void init(IProject project);

}
