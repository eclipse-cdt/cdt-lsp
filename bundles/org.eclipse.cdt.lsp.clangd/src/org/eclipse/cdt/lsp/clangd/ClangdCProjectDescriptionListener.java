/*******************************************************************************
 * Copyright (c) 2024 Bachmann electronic GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd;

import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.core.resources.IProject;

/**
 * Vendors can implement this interface as OSGi service
 * with a service.ranking property > 0 to implement custom behavior
 * and to replace the {@link ClangdConfigurationManager}
 */
public interface ClangdCProjectDescriptionListener {
	String CLANGD_CONFIG_FILE_NAME = ".clangd"; //$NON-NLS-1$

	/**
	 * Called when the configuration of a CDT C/C++ project changes.
	 * @param event
	 * @param macroResolver
	 */
	void handleEvent(CProjectDescriptionEvent event, MacroResolver macroResolver);

	/**
	 * Set the <code>CompilationDatabase</code> entry in the <code>.clangd</code> file which is located in the <code>project</code> root.
	 * The <code>.clangd</code> file will be created, if it's not existing.
	 * The <code>CompilationDatabase</code> points to the build folder of the active build configuration
	 * (in case <code>project</code> is a managed C/C++ project).
	 *
	 * In the following example clangd uses the compile_commands.json file in the Debug folder:
	 * <pre>CompileFlags: {CompilationDatabase: Debug}</pre>
	 *
	 * @param project managed C/C++ project
	 * @param newCProjectDescription new CProject description
	 * @param macroResolver helper to resolve macros in the CWD path of the builder
	 */
	void setCompilationDatabasePath(IProject project, ICProjectDescription newCProjectDescription,
			MacroResolver macroResolver);

	/**
	 * Enabler for {@link setCompilationDatabasePath}. Can be overriden for customization.
	 * @param project
	 * @return true if the database path should be written to .clangd file in the project root.
	 */
	boolean enableSetCompilationDatabasePath(IProject project);
}