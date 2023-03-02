/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.editor.ui.properties;

import java.net.URI;
import java.util.Optional;

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.editor.ui.LspEditorUiPlugin;
import org.eclipse.cdt.lsp.server.ICompileCommandsDirLocator;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class PropertiesCompileCommandsDirLocator implements ICompileCommandsDirLocator {

	@Override
	public Optional<IPath> getCompileCommandsDir(URI uri) {
		var project =  getProject(uri);
		if (project == null) {
			return Optional.empty();
		}
		IEclipsePreferences node = new ProjectScope(project).getNode(LspEditorUiPlugin.PLUGIN_ID);
		if (node == null) {
			return Optional.empty();
		}
		var relativePath = node.get(LspEditorPropertiesPage.COMPILE_COMMANDS_DIR, "build/default");
		return Optional.of(project.getLocation().append(relativePath));
	}
	
	private IProject getProject(URI uri) {
		var scheme = uri.getScheme();
		if ("file".equals(scheme)) {
			IContainer[] container = LspPlugin.getDefault().getWorkspace().getRoot().findContainersForLocationURI(uri);
			if (container.length > 0) {
				return container[0].getProject();
			}
		}
		return null;
	}

}
