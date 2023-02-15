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

package org.eclipse.cdt.lsp.editor.ui.preference;

import java.util.Optional;

import org.eclipse.cdt.lsp.editor.ui.Activator;
import org.eclipse.cdt.lsp.workspace.ResolveDocumentFile;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;

public class LspEditorPreferencesTester extends PropertyTester {
	private static final String LSP_CEDITOR_ID = "org.eclipse.cdt.lsp.CEditor";
	private final ResolveDocumentFile file;
	
	public LspEditorPreferencesTester() {
		file = new ResolveDocumentFile();
	}
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IDocument) {
			Optional<IProject> project = file.apply((IDocument) receiver).map(IFile::getProject);
			if (project.isPresent()) {
				return preferLspEditor(project.get());
			}
			// check the starting point, this is probably a link to another (header)file outside the project. Use LSP C-Editor when active:
			return isLspEditorActive();
		} else if (receiver instanceof IEditorInput) {
			IResource resource = ((IEditorInput) receiver).getAdapter(IResource.class);
			if(resource != null && resource.getProject() != null) {
				return preferLspEditor(resource.getProject());
			}
			// check the starting point, this is probably a link to another (header)file outside the project. Use LSP C-Editor when active:
			return isLspEditorActive();
		} 
		return false;
	}

	protected boolean preferLspEditor(IProject project) {
		PreferenceMetadata<Boolean> option = LspEditorPreferences.getPreferenceMetadata();
		return Platform.getPreferencesService().getBoolean(Activator.PLUGIN_ID, option.identifer(),
				option.defaultValue(), new IScopeContext[] { new ProjectScope(project) });
	}
	
	private boolean isLspEditorActive() {
		var activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null && activeWorkbenchWindow.getActivePage() != null) {
			var activeEditor = activeWorkbenchWindow.getActivePage().getActiveEditor();
			if (activeEditor != null) {
				return LSP_CEDITOR_ID.equals(activeEditor.getEditorSite().getId());
			}
		}
		return false;
	}

}
