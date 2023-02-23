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

package org.eclipse.cdt.lsp.editor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IEditorAssociationOverride;
import org.eclipse.ui.part.FileEditorInput;

public class CEditorAssociationOverride implements IEditorAssociationOverride {
	private final ICLanguageServerProvider cLanguageServerProvider;

	public CEditorAssociationOverride() {
		cLanguageServerProvider = LspPlugin.getDefault().getCLanguageServerProvider();
	}

	/**
	 * Remove not appropriate C Editor
	 */
	@Override
	public IEditorDescriptor[] overrideEditors(IEditorInput editorInput, IContentType contentType, IEditorDescriptor[] editorDescriptors) {
		if (isNoCElement(contentType)) {
			return editorDescriptors;
		}
		if (cLanguageServerProvider.isEnabledFor(editorInput)) {
			return editorFilter(LspPlugin.C_EDITOR_ID, editorDescriptors); // remove CDT C-Editor
		}
		return editorFilter(LspPlugin.LSP_C_EDITOR_ID, editorDescriptors); // remove LSP based C-Editor
	}

	@Override
	public IEditorDescriptor[] overrideEditors(String fileName, IContentType contentType, IEditorDescriptor[] editorDescriptors) {
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(fileName);
		if (resource != null && resource instanceof IFile) {
			return overrideEditors(new FileEditorInput((IFile) resource), contentType, editorDescriptors);
		}
		return editorDescriptors;
	}

	@Override
	public IEditorDescriptor overrideDefaultEditor(IEditorInput editorInput, IContentType contentType, IEditorDescriptor editorDescriptor) {
		IEditorDescriptor descriptor = getEditorDescriptor(editorInput, contentType);
		return descriptor != null ? descriptor : editorDescriptor;
	}

	@Override
	public IEditorDescriptor overrideDefaultEditor(String fileName, IContentType contentType, IEditorDescriptor editorDescriptor) {
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(fileName);
		if (resource != null && resource instanceof IFile) {
			return overrideDefaultEditor(new FileEditorInput((IFile) resource), contentType, editorDescriptor);
		}
		return editorDescriptor;
	}

	private boolean isNoCElement(IContentType contentType) {
		if (contentType == null || !(CCorePlugin.CONTENT_TYPE_CHEADER.equals(contentType.getId()) ||
				CCorePlugin.CONTENT_TYPE_CSOURCE.equals(contentType.getId()) ||
				CCorePlugin.CONTENT_TYPE_CXXHEADER.equals(contentType.getId()) ||
				CCorePlugin.CONTENT_TYPE_CXXSOURCE.equals(contentType.getId())))
			return true;
		return false;
	}

	private IEditorDescriptor[] editorFilter(String editorId, IEditorDescriptor[] editorDescriptors) {
		if (editorId != null) {
			for (int i = 0; i < editorDescriptors.length; ++i) {
				if (editorId.equals(editorDescriptors[i].getId())) {
					// Remove the editor:
					IEditorDescriptor[] filteredDesc = new IEditorDescriptor[editorDescriptors.length - 1];
					System.arraycopy(editorDescriptors, 0, filteredDesc, 0, i);
					System.arraycopy(editorDescriptors, i + 1, filteredDesc, i, editorDescriptors.length - i - 1);
					return filteredDesc;
				}
			}
		}
		return editorDescriptors;
	}

	private IEditorDescriptor getEditorDescriptor(IEditorInput editorInput, IContentType contentType) {
		if (isNoCElement(contentType))
			return null;

		if (cLanguageServerProvider.isEnabledFor(editorInput)) {
			return getLspCEditor(editorInput, contentType);
		}
		return null;
	}

	private IEditorDescriptor getLspCEditor(IEditorInput editorInput, IContentType contentType) {
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		for (IEditorDescriptor descriptor : registry.getEditors(editorInput.getName(), contentType)) {
			if (LspPlugin.LSP_C_EDITOR_ID.equals(descriptor.getId())) {
				return descriptor;
			}
		}
		return null;
	}
}
