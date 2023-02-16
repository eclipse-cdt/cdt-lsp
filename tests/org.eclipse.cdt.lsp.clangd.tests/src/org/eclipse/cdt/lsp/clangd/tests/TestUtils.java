/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *     Alexander Fedorov (ArSysOp) - extract headless part
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd.tests;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.cdt.lsp.internal.clangd.editor.LspEditorUiPlugin;
import org.eclipse.cdt.lsp.internal.clangd.editor.preferences.LspEditorPreferences;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.junit.jupiter.api.TestInfo;

public final class TestUtils {

	public static void setLspPreferred(IProject project, boolean value) {
		IEclipsePreferences node = new ProjectScope(project).getNode(LspEditorUiPlugin.PLUGIN_ID);
		node.putBoolean(LspEditorPreferences.getPreferenceMetadata().identifer(), value);
	}

	public static boolean isLspPreferred(IProject project) {
		IEclipsePreferences node = new ProjectScope(project).getNode(LspEditorUiPlugin.PLUGIN_ID);
		return node.getBoolean(LspEditorPreferences.getPreferenceMetadata().identifer(), false);
	}

	public static IProject createCProject(String projectName) throws CoreException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project.exists()) {
			return project;
		}
		project.create(null);
		project.open(null);
		// configure C nature
		IProjectDescription description = project.getDescription();
		if (description != null) {
			String[] natureIds = { "org.eclipse.cdt.core.cnature" };
			description.setNatureIds(natureIds);
			project.setDescription(description, null);
		}
		return project;
	}

	public static void deleteProject(IProject project) throws CoreException {
		if (project != null) {
			project.delete(true, new NullProgressMonitor());
		}
	}

	public static IFile createFile(IProject p, String name, String content)
			throws CoreException, UnsupportedEncodingException {
		IFile testFile = p.getFile(name);
		testFile.create(new ByteArrayInputStream(content.getBytes(testFile.getCharset())), true, null);
		return testFile;
	}

	public static String getName(TestInfo testInfo) {
		String displayName = testInfo.getDisplayName();
		String replaceFirst = displayName.replaceFirst("\\(.*\\)", "");
		return replaceFirst;
	}

}
