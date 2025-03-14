/*******************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd.internal.config;

import java.io.IOException;
import java.net.URI;

import org.eclipse.cdt.lsp.clangd.ClangFormatFile;
import org.eclipse.cdt.lsp.clangd.format.ClangFormatFileMonitor;
import org.eclipse.cdt.lsp.clangd.plugin.ClangdPlugin;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.component.annotations.Component;

@Component(property = { "service.ranking:Integer=0" })
public final class ClangFormatFileHandler implements ClangFormatFile {

	/**
	 * Opens the .clang-format file in the given project. Creates a file with default values, if not yet existing prior to the opening.
	 * @param formatFile
	 */
	@Override
	public void openClangFormatFile(IProject project) {
		findOrCreateClangFormatFile(project, true);
	}

	/**
	 * Creates a new .clang-format file with default settings in the project root directory if not yet existing.
	 * @param project
	 */
	@Override
	public void createClangFormatFile(IProject project) {
		findOrCreateClangFormatFile(project, false);
	}

	private void findOrCreateClangFormatFile(IProject project, boolean openFile) {
		IFile formatFileInProject = project.getFile(ClangFormatFileMonitor.CLANG_FORMAT_FILE);
		IFileStore formatFileInParentFolder = null;

		if (!formatFileInProject.exists()) {
			formatFileInParentFolder = findClangFormatFileInParentFolders(project);
		}

		boolean createFormatFile = !formatFileInProject.exists() && formatFileInParentFolder == null;

		if (createFormatFile) {
			WorkspaceJob job = new WorkspaceJob("Create " + ClangFormatFileMonitor.CLANG_FORMAT_FILE + " file") { //$NON-NLS-1$ //$NON-NLS-2$
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) {
					var status = createFileFromResource(formatFileInProject);
					if (openFile && status.isOK()) {
						openClangFormatFile(formatFileInProject.getLocationURI());
					}
					return status;
				}
			};
			job.setSystem(true);
			job.setRule(formatFileInProject.getWorkspace().getRuleFactory().createRule(formatFileInProject));
			job.schedule();
		} else if (openFile) {
			URI formatFileUri = (formatFileInParentFolder != null) ? formatFileInParentFolder.toURI()
					: formatFileInProject.getLocationURI();
			openClangFormatFile(formatFileUri);
		}
	}

	private void openClangFormatFile(URI fileUri) {
		Display.getDefault().asyncExec(() -> {
			LSPEclipseUtils.open(fileUri.toString(), null);
		});
	}

	private IFileStore findClangFormatFileInParentFolders(IProject project) {
		IFileStore currentDirStore = EFS.getLocalFileSystem().getStore(project.getLocation());
		IFileStore clangFormatFileStore = currentDirStore.getChild(ClangFormatFileMonitor.CLANG_FORMAT_FILE);

		while (!clangFormatFileStore.fetchInfo().exists() && currentDirStore.getParent() != null
				&& currentDirStore.getParent().fetchInfo().exists()) {
			// move up one level to the parent directory and check again
			currentDirStore = currentDirStore.getParent();
			clangFormatFileStore = currentDirStore.getChild(ClangFormatFileMonitor.CLANG_FORMAT_FILE);
		}

		if (clangFormatFileStore.fetchInfo().exists()) {
			return clangFormatFileStore;
		}
		return null;
	}

	private IStatus createFileFromResource(IFile formatFile) {
		if (!formatFile.exists()) {
			try (final var source = getClass().getResourceAsStream(".clang-format-project");) { //$NON-NLS-1$
				formatFile.create(source, true, new NullProgressMonitor());
			} catch (IOException | CoreException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
				return new Status(IStatus.ERROR, ClangdPlugin.PLUGIN_ID,
						"Cannot create " + ClangFormatFileMonitor.CLANG_FORMAT_FILE + " file", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return Status.OK_STATUS;
	}

}
