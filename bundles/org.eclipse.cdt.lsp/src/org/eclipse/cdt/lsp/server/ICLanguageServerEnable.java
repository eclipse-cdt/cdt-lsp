package org.eclipse.cdt.lsp.server;

import org.eclipse.core.resources.IProject;

public interface ICLanguageServerEnable {

	/**
	 * Check whether the LSP based C/C++ Editor and the language server shall be used for the given project.
	 *
	 * @param project
	 * @return true if LSP based C/C++ Editor and language server shall be enabled for the given project, otherwise false.
	 */
	public boolean isEnabledFor(IProject project);

}
