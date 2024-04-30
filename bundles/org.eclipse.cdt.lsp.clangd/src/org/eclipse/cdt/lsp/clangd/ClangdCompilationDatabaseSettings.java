package org.eclipse.cdt.lsp.clangd;

import org.eclipse.core.resources.IProject;

public interface ClangdCompilationDatabaseSettings {

	/**
	 * Enabler for {@link org.eclipse.cdt.lsp.clangd.internal.config.ClangdConfigurationFileManager#setCompilationDatabase(IProject, String)}.
	 * Can be overriden for customization.
	 * @param project
	 * @return true if the database path should be written to .clangd file in the project root.
	 */
	boolean enableSetCompilationDatabasePath(IProject project);

}