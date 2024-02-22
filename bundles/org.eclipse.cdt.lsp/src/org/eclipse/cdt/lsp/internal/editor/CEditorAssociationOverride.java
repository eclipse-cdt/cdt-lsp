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

package org.eclipse.cdt.lsp.internal.editor;

import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.cdt.lsp.util.LspUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
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
	public IEditorDescriptor[] overrideEditors(IEditorInput editorInput, IContentType contentType,
			IEditorDescriptor[] editorDescriptors) {
		if (isNoCElement(contentType)) {
			return editorDescriptors;
		}
		if (isEnabledFor(editorInput)) {
			return editorFilter(LspPlugin.C_EDITOR_ID, editorDescriptors); // remove CDT C-Editor
		}
		return editorFilter(LspPlugin.LSP_C_EDITOR_ID, editorDescriptors); // remove LSP based C-Editor
	}

	@Override
	public IEditorDescriptor[] overrideEditors(String fileName, IContentType contentType,
			IEditorDescriptor[] editorDescriptors) {
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(fileName);
		if (resource != null && resource instanceof IFile) {
			return overrideEditors(new FileEditorInput((IFile) resource), contentType, editorDescriptors);
		}
		return editorDescriptors;
	}

	@Override
	public IEditorDescriptor overrideDefaultEditor(IEditorInput editorInput, IContentType contentType,
			IEditorDescriptor editorDescriptor) {
		IEditorDescriptor descriptor = getEditorDescriptor(editorInput, contentType);
		return descriptor != null ? descriptor : editorDescriptor;
	}

	@Override
	public IEditorDescriptor overrideDefaultEditor(String fileName, IContentType contentType,
			IEditorDescriptor editorDescriptor) {
		IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(fileName);
		if (resource != null && resource instanceof IFile) {
			return overrideDefaultEditor(new FileEditorInput((IFile) resource), contentType, editorDescriptor);
		}
		return editorDescriptor;
	}

	private boolean isEnabledFor(IEditorInput editorInput) {
		if (cLanguageServerProvider == null)
			return false;
		IResource resource = editorInput.getAdapter(IResource.class);
		if (resource != null) {
			boolean enabled = cLanguageServerProvider.isEnabledFor(resource.getProject());
			if (enabled) {
				deleteCodanMarkers(resource);
			}
			return enabled;
		}
		// When resource == null it's an external file: Check if the file is already opened, if not check the active editor:
		return LspUtils.isFileOpenedInLspEditor(editorInput);
	}

	private void deleteCodanMarkers(IResource resource) {
		var wsJob = new WorkspaceJob("Remove codan markers") { //$NON-NLS-1$
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				try {
					resource.deleteMarkers(IProblemReporter.GENERIC_CODE_ANALYSIS_MARKER_TYPE, true,
							IResource.DEPTH_INFINITE);
				} catch (CoreException e) {
					Platform.getLog(CEditorAssociationOverride.class).log(e.getStatus());
				}
				return Status.OK_STATUS;
			}
		};
		wsJob.setRule(resource);
		wsJob.setSystem(true);
		wsJob.schedule();
	}

	private boolean isNoCElement(IContentType contentType) {
		if (contentType == null) {
			return true;
		}
		return !LspUtils.isCContentType(contentType.getId());
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

		if (isEnabledFor(editorInput)) {
			return getEditorDescriptorById(editorInput.getName(), LspPlugin.LSP_C_EDITOR_ID, contentType); // return LSP based C/C++ Editor
		}
		// TODO: return null; when either https://github.com/eclipse-cdt/cdt/pull/310 or
		// https://github.com/eclipse/tm4e/pull/500 has been merged.
		return getEditorDescriptorById(editorInput.getName(), LspPlugin.C_EDITOR_ID, contentType); // return C/C++ Editor
	}

	private IEditorDescriptor getEditorDescriptorById(String fileName, String editorId, IContentType contentType) {
		IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();
		for (IEditorDescriptor descriptor : registry.getEditors(fileName, contentType)) {
			if (editorId.equals(descriptor.getId())) {
				return descriptor;
			}
		}
		return null;
	}
}
