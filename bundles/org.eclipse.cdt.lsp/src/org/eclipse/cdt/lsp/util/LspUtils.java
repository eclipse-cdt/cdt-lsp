/*******************************************************************************
 * Copyright (c) 2023, 2024, 2025 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/
package org.eclipse.cdt.lsp.util;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;

public class LspUtils {

	/**
	 * Checks if given ContentType id matches the content types for C/C++ files.
	 *
	 * @param id ContentType id
	 * @return {@code true} if C/C++ content type
	 */
	public static boolean isCContentType(String id) {
		return (id.startsWith("org.eclipse.cdt.core.c") && (id.endsWith("Source") || id.endsWith("Header"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public static boolean isFileOpenedInLspEditor(URI uri) {
		if (uri == null) {
			return false;
		}
		var editors = getEditors();
		if (!editors.isEmpty()) {
			for (IEditorReference editor : editors) {
				IEditorInput editorInput = null;
				URI editorUnputURI = null;
				try {
					editorInput = editor.getEditorInput();
				} catch (PartInitException e) {
					Platform.getLog(LspUtils.class).error(e.getMessage(), e);
					continue;
				}

				if (editorInput instanceof IURIEditorInput) {
					editorUnputURI = ((IURIEditorInput) editorInput).getURI();
				} else if (editorInput instanceof FileEditorInput) {
					editorUnputURI = ((FileEditorInput) editorInput).getFile().getLocationURI();
				}

				if (uri.equals(editorUnputURI)) {
					// should return false when an external header file with same URI is opened in a LSP editor
					// and non LSP editor and tab switching from a non LSP editor to the tab with the file in the non LSP editor:
					return LspPlugin.LSP_C_EDITOR_ID.equals(editor.getId()) && isLspEditorActive();
				}
			}
			// the file has not been opened yet -> goto definition/declaration case
			return isLspEditorActive();
		}
		return false;
	}

	public static Map<Integer, URI> getFilesInLspBasedEditor() {
		var uris = new HashMap<Integer, URI>();
		for (IEditorReference editor : getEditors()) {
			if (LspPlugin.LSP_C_EDITOR_ID.equals(editor.getId())) {
				IEditorInput editorInput = null;
				try {
					editorInput = editor.getEditorInput();
				} catch (PartInitException e) {
					Platform.getLog(LspUtils.class).error(e.getMessage(), e);
					continue;
				}
				int hash = Optional.ofNullable(editor.getPart(true)).map(p -> p.hashCode()).orElse(0);
				if (hash != 0 && checkForCContentType(editorInput)) {
					if (editorInput instanceof IURIEditorInput uriEditorInput) {
						uris.put(hash, uriEditorInput.getURI());
					} else if (editorInput instanceof FileEditorInput fileEditorInput) {
						uris.put(hash, fileEditorInput.getFile().getLocationURI());
					}
				}
			}
		}
		return uris;
	}

	public static boolean isFileOpenedInLspEditor(IEditorInput editorInput, IContentType contentType) {
		if (editorInput == null) {
			return false;
		}
		var editors = getEditors();
		if (!editors.isEmpty()) {
			for (IEditorReference editor : editors) {
				IEditorInput editorInputFromEditor = null;
				try {
					editorInputFromEditor = editor.getEditorInput();
				} catch (PartInitException e) {
					Platform.getLog(LspUtils.class).error(e.getMessage(), e);
					continue;
				}
				if (editorInput.equals(editorInputFromEditor)) {
					return LspPlugin.LSP_C_EDITOR_ID.equals(editor.getId());
				}
			}
			// the file has not been opened yet:
			return isLspEditorActive();
		}
		// check defaults:
		var desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(editorInput.getName(), contentType);
		return desc != null ? LspPlugin.LSP_C_EDITOR_ID.equals(desc.getId()) : false;
	}

	public static List<IEditorReference> getEditors() {
		List<IEditorReference> editorsList = new ArrayList<>();
		for (var window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
			for (var page : window.getPages()) {
				for (var editor : page.getEditorReferences()) {
					editorsList.add(editor);
				}
			}
		}
		return editorsList;
	}

	private static boolean isLspEditorActive() {
		var activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null && activeWorkbenchWindow.getActivePage() != null) {
			var activeEditor = activeWorkbenchWindow.getActivePage().getActiveEditor();
			if (activeEditor != null) {
				return LspPlugin.LSP_C_EDITOR_ID.equals(activeEditor.getEditorSite().getId());
			}
		}
		return false;
	}

	public static boolean checkForCContentType(IEditorInput editorInput) {
		if (editorInput instanceof IFileEditorInput fileEditorInput) {
			try {
				return Optional.ofNullable(fileEditorInput.getFile().getContentDescription())
						.map(cd -> cd.getContentType()).map(ct -> ct.getId()).map(LspUtils::isCContentType)
						.orElse(false);
			} catch (CoreException e) {
				// do nothing
			}
		} else if (editorInput instanceof FileStoreEditorInput fileStore) {
				var contentType = Platform.getContentTypeManager().findContentTypeFor(fileStore.getName());
				if (contentType != null) {
					return isCContentType(contentType.getId());
				}
		}
		return false;
	}

	//FIXME: AF: consider removing, since it doesn't recognize containers, use UriResource instead
	public static Optional<IProject> getProject(URI uri) {
		return getFile(uri).map(file -> file.getProject());
	}

	public static Optional<IFile> getFile(URI uri) {
		List<IFile> found = new ArrayList<>();
		ServiceCaller.callOnce(LspUtils.class, IWorkspace.class, //
				w -> Arrays.stream(w.getRoot().findFilesForLocationURI(uri)).forEach(found::add));
		return found.stream().findFirst();
	}

	/**
	 * Get active language servers
	 * @return List of LanguageServerWrapper
	 */
	public static List<LanguageServerWrapper> getLanguageServers() {
		return getLanguageServers(true);
	}

	/**
	 * Get language servers
	 * @param onlyActiveLS
	 * @return List of LanguageServerWrapper
	 */
	public static List<LanguageServerWrapper> getLanguageServers(boolean onlyActiveLS) {
		return LanguageServiceAccessor.getStartedWrappers(null, onlyActiveLS).stream()
				.filter(w -> "org.eclipse.cdt.lsp.server".equals(w.serverDefinition.id)).toList(); //$NON-NLS-1$
	}

}
