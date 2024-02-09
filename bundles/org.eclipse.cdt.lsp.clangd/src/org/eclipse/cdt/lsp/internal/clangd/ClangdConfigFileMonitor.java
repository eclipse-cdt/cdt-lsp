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

package org.eclipse.cdt.lsp.internal.clangd;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * Monitor changes in <code>.clangd</code> files in the workspace and triggers a yaml checker
 * to add error markers to the <code>.clangd</code> file when the edits causes yaml loader failures.
 */
public class ClangdConfigFileMonitor {
	private static final String CLANGD_CONFIG_FILE = ".clangd"; //$NON-NLS-1$
	private final ConcurrentLinkedQueue<IFile> pendingFiles = new ConcurrentLinkedQueue<>();
	private final IWorkspace workspace;
	private final ClangdConfigFileChecker checker = new ClangdConfigFileChecker();

	private final IResourceChangeListener listener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getDelta() != null && event.getType() == IResourceChangeEvent.POST_CHANGE) {
				try {
					event.getDelta().accept(delta -> {
						if ((delta.getKind() == IResourceDelta.ADDED
								|| (delta.getFlags() & IResourceDelta.CONTENT) != 0)
								&& CLANGD_CONFIG_FILE.equals(delta.getResource().getName())) {
							if (delta.getResource() instanceof IFile file) {
								pendingFiles.add(file);
								checkJob.schedule(100);
							}
						}
						return true;
					});
				} catch (CoreException e) {
					Platform.getLog(getClass()).log(e.getStatus());
				}
			}
		}
	};

	public ClangdConfigFileMonitor(IWorkspace workspace) {
		this.workspace = workspace;
	}

	private final WorkspaceJob checkJob = new WorkspaceJob("Check .clangd file") { //$NON-NLS-1$

		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
			while (pendingFiles.peek() != null) {
				checker.checkConfigFile(pendingFiles.poll());
			}
			return Status.OK_STATUS;
		}

	};

	public ClangdConfigFileMonitor start() {
		workspace.addResourceChangeListener(listener);
		return this;
	}

	public void stop() {
		workspace.removeResourceChangeListener(listener);
	}
}
