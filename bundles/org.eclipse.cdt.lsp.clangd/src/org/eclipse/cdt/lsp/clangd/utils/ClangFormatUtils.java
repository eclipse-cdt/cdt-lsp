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

package org.eclipse.cdt.lsp.clangd.utils;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.plugin.ClangdPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public final class ClangFormatUtils {
	public static final String format_file = ".clang-format"; //$NON-NLS-1$

	/**
	 * Checks if the formatFile exists. Creates it if not.
	 * @param formatFile
	 */
	public Optional<IFile> getClangFormatFile(IProject project) {
		var formatFile = project.getFile(format_file);
		var status = createFileFromResource(formatFile);
		return status.isOK() ? Optional.of(formatFile) : Optional.empty();
	}

	public void createClangFormatFile(IProject project) {
		var formatFile = project.getFile(format_file);
		WorkspaceJob job = new WorkspaceJob("Create " + format_file + " file") { //$NON-NLS-1$ //$NON-NLS-2$
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				return createFileFromResource(formatFile);
			}
		};
		job.setSystem(true);
		job.setRule(formatFile.getWorkspace().getRuleFactory().createRule(formatFile));
		job.schedule();
	}

	private IStatus createFileFromResource(IFile formatFile) {
		if (!formatFile.exists()) {
			try (final var source = getClass().getResourceAsStream(".clang-format-project");) { //$NON-NLS-1$
				formatFile.create(source, true, new NullProgressMonitor());
			} catch (IOException | CoreException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
				return new Status(IStatus.ERROR, ClangdPlugin.PLUGIN_ID, "Cannot create " + format_file, e); //$NON-NLS-1$
			}
		}
		return Status.OK_STATUS;
	}

}
