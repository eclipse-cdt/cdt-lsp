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
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.test;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.jupiter.api.TestInfo;

public class TestUtils {

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

	public static String getName(TestInfo testInfo) {
		String displayName = testInfo.getDisplayName();
		String replaceFirst = displayName.replaceFirst("\\(.*\\)", "");
		return replaceFirst;
	}

	public static IEditorPart openInEditor(URI uri, String editorID) throws PartInitException {
		IEditorPart part = IDE.openEditor(getWorkbenchPage(), uri, editorID, true);
		part.setFocus();
		return part;
	}

	public static IEditorPart openInEditor(IFile file) throws PartInitException {
		IEditorPart part = IDE.openEditor(getWorkbenchPage(), file);
		part.setFocus();
		return part;
	}

	public static boolean closeEditor(IEditorPart editor, boolean save) {
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = workbenchWindow.getActivePage();
		return page.closeEditor(editor, save);
	}

	public static IFile createFile(IProject p, String name, String content)
			throws CoreException, UnsupportedEncodingException {
		IFile testFile = p.getFile(name);
		testFile.create(new ByteArrayInputStream(content.getBytes(testFile.getCharset())), true, null);
		return testFile;
	}

	private static IWorkbenchPage getWorkbenchPage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

}
