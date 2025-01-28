/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;

/**
 * Monitor changes in the <code>.clang-format</code> files in the workspace and triggers a check via <code>clangd --check</code>
 * to add error markers to the modified <code>.clang-format</code> file.
 */
public class ClangFormatFileMonitor {
	private static final String CLANG_FORMAT_FILE = ".clang-format"; //$NON-NLS-1$
	public static final String CLANG_FORMAT_CHECK_FILE = "clang-format-check"; //$NON-NLS-1$
	private final ConcurrentLinkedQueue<IFile> pendingFiles = new ConcurrentLinkedQueue<>();
	private final IWorkspace workspace;
	private final ClangFormatValidator validator = new ClangFormatValidator();

	private final ServiceCaller<ClangdConfiguration> configuration = new ServiceCaller<>(getClass(),
			ClangdConfiguration.class);

	private final IResourceChangeListener listener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getDelta() != null && event.getType() == IResourceChangeEvent.POST_CHANGE) {
				try {
					event.getDelta().accept(delta -> {
						if ((delta.getKind() == IResourceDelta.ADDED
								|| (delta.getFlags() & IResourceDelta.CONTENT) != 0)
								&& CLANG_FORMAT_FILE.equals(delta.getResource().getName())) {
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

	public ClangFormatFileMonitor(IWorkspace workspace) {
		this.workspace = workspace;
	}

	private final WorkspaceJob checkJob = new WorkspaceJob("Check .clang-format file") { //$NON-NLS-1$

		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
			var clangdPath = getClangdPath();
			if (clangdPath.isEmpty()) {
				String msg = "Cannot determine clangd path for .clang-format file validation"; //$NON-NLS-1$
				Platform.getLog(getClass()).error(msg);
				return Status.error(msg);
			}
			while (pendingFiles.peek() != null) {
				IFile emptyFile = null;
				try {
					var clangFormatFile = pendingFiles.poll();
					emptyFile = createEmptyFile(clangFormatFile);
					if (emptyFile == null) {
						Platform.getLog(getClass()).error("Cannot create empty file"); //$NON-NLS-1$
						continue;
					}
					validator.validateFile(getCommandLine(emptyFile, clangdPath), clangFormatFile);
				} catch (IOException e) {
					Platform.getLog(getClass()).error(e.getMessage(), e);
					return Status.error(e.getMessage());
				} finally {
					deleteEmptyFile(emptyFile);
				}
			}
			return Status.OK_STATUS;
		}

	};

	private IFile createEmptyFile(IFile clangFormatFile) {
		var parent = clangFormatFile.getParent();
		if (parent instanceof IContainer folder) {
			try {
				var file = folder.getFile(new Path(CLANG_FORMAT_CHECK_FILE));
				if (!file.exists()) {
					file.create(new byte[0], true, false, null);
				}
				return file;
			} catch (CoreException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}
		}
		return null;
	}

	private void deleteEmptyFile(IFile emptyFile) {
		try {
			if (emptyFile != null && emptyFile.exists()) {
				emptyFile.delete(true, null);
			}
		} catch (CoreException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}
	}

	private List<String> getCommandLine(IFile emptyFile, String clangdPath) {
		var list = new ArrayList<String>(2);
		list.add(clangdPath);
		list.add("--check=" + emptyFile.getLocation().toOSString()); //$NON-NLS-1$
		return list;
	}

	private String getClangdPath() {
		List<String> result = new ArrayList<>();
		configuration.call(c -> result.addAll(
				c.commands(null).stream().map(ClangFormatFileMonitor::resolveVariables).collect(Collectors.toList())));
		return result.isEmpty() ? "" : result.get(0); //$NON-NLS-1$
	}

	private static String resolveVariables(String cmd) {
		try {
			return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(cmd);
		} catch (CoreException e) {
			return cmd;
		}
	}

	public ClangFormatFileMonitor start() {
		workspace.addResourceChangeListener(listener);
		return this;
	}

	public void stop() {
		workspace.removeResourceChangeListener(listener);
	}
}
