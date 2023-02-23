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

import java.net.URI;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.editor.ui.LspEditorUiPlugin;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

@SuppressWarnings("restriction")
public class LspEditorPreferencesTester extends PropertyTester {
	private static final String FILE_SCHEME = "file"; //$NON-NLS-1$
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof URI) {
			// called from the language server enabler:
			var uri = (URI) receiver;
			var fileHandle = getFileHandle(uri);
			if(fileHandle != null && fileHandle instanceof IResource) {
				var resource = (IResource) fileHandle;
				if (resource != null && resource.getProject() != null)
					return preferLspEditor(resource.getProject());
			}
			// when resource == null it's an external file: Check if the file is already opened, if not check the active editor:
			return isFileOpenedInLspEditor(uri);
		} else if (receiver instanceof IEditorInput) {
			// called to determine the default editor for the file:
			var editorInput = (IEditorInput) receiver;
			IResource resource = editorInput.getAdapter(IResource.class);
			if(resource != null && resource.getProject() != null) {
				return preferLspEditor(resource.getProject());
			}
			// When resource == null it's an external file: Check if the file is already opened, if not check the active editor:
			return isFileOpenedInLspEditor(editorInput);
		} else if (receiver instanceof CEditor) {
			// TODO: remove this dirty hack when LSPE4 has solved the issue #393
			return false;
		}
		return true; // we assume it's a C/C++ based editor. TODO: change to false when SPE4 has solved the issue #393 and last else-if has been removed
	}

	protected boolean preferLspEditor(IProject project) {
		// check project properties:
		PreferenceMetadata<Boolean> option = LspEditorPreferences.getPreferenceMetadata();
		return Platform.getPreferencesService().getBoolean(LspEditorUiPlugin.PLUGIN_ID, option.identifer(),
				option.defaultValue(), new IScopeContext[] { new ProjectScope(project) });
	}
	
	private boolean isLspEditorActive() {
		var activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null && activeWorkbenchWindow.getActivePage() != null) {
			var activeEditor = activeWorkbenchWindow.getActivePage().getActiveEditor();
			if (activeEditor != null) {
				return LspPlugin.LSP_C_EDITOR_ID.equals(activeEditor.getEditorSite().getId());
			}
		}
		return false;
	}
	
	private IFile getFileHandle(URI uri) {
		if (uri == null) {
			return null;
		}
		if (FILE_SCHEME.equals(uri.getScheme())) {
			IFile[] files = LspEditorUiPlugin.getDefault().getWorkspace().getRoot().findFilesForLocationURI(uri);
			if (files.length > 0) {
				return files[0];
			}
			return null;
		} else {
			return Adapters.adapt(uri.toString(), IFile.class, true);
		}
	}
	
	private boolean isFileOpenedInLspEditor(URI uri) {
		if (uri == null) {
			return false;
		}
		var activeEditors = getActiveEditors();
		if (activeEditors != null) {
			for (IEditorReference editor : activeEditors) {
				IEditorInput editorInput = null;
				URI editorUnputURI = null;
				try {
					editorInput = editor.getEditorInput();
				} catch (PartInitException e) {
					LspEditorUiPlugin.logError(e.getMessage(), e);
					continue;
				}
				
				if (editorInput instanceof IURIEditorInput) {
					editorUnputURI = ((IURIEditorInput) editorInput).getURI();
				} else if (editorInput instanceof FileEditorInput) {
					editorUnputURI = ((FileEditorInput) editorInput).getFile().getLocationURI();
				}

				if (uri.equals(editorUnputURI)) {
					return LspPlugin.LSP_C_EDITOR_ID.equals(editor.getEditor(true).getEditorSite().getId());
				}
			}
		}
		// the file has not been opened yet:
		return isLspEditorActive();
	}
	
	private boolean isFileOpenedInLspEditor(IEditorInput editorInput) {
	if (editorInput == null) {
		return false;
	}
	var activeEditors = getActiveEditors();
	if (activeEditors != null) {
		for (IEditorReference editor : activeEditors) {
			IEditorInput editorInputFromEditor = null;
			try {
				editorInputFromEditor = editor.getEditorInput();
			} catch (PartInitException e) {
				LspEditorUiPlugin.logError(e.getMessage(), e);
				continue;
			}
			if (editorInput.equals(editorInputFromEditor)) {
				return LspPlugin.LSP_C_EDITOR_ID.equals(editor.getEditor(true).getEditorSite().getId());
			}
		}
	}
	// the file has not been opened yet:
	return isLspEditorActive();
}
	
	private IEditorReference[] getActiveEditors() {
		var activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null && activeWorkbenchWindow.getActivePage() != null) {
			return activeWorkbenchWindow.getActivePage().getEditorReferences();
		}
		return null;
	}

}
