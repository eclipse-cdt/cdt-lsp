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

import org.eclipse.cdt.lsp.clangd.ClangFormatFile;
import org.eclipse.cdt.lsp.clangd.plugin.ClangdPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.ui.progress.WorkbenchJob;
import org.osgi.service.component.annotations.Component;

@Component(property = { "service.ranking:Integer=0" })
public final class ClangFormatFileHandler implements ClangFormatFile {
	public static final String format_file = ".clang-format"; //$NON-NLS-1$

	/**
	 * Opens the .clang-format file in the given project. Creates a file with default values, if not yet existing prior to the opening.
	 * @param formatFile
	 */
	@Override
	public void openClangFormatFile(IProject project) {
		runClangFormatFileCreatorJob(project, true);
	}

	/**
	 * Creates a new .clang-format file with default settings in the project root directory if not yet existing.
	 * @param project
	 */
	@Override
	public void createClangFormatFile(IProject project) {
		runClangFormatFileCreatorJob(project, false);
	}

	private void runClangFormatFileCreatorJob(IProject project, boolean openFile) {
		var formatFile = project.getFile(format_file);
		WorkbenchJob job = new WorkbenchJob("Create " + format_file + " file") { //$NON-NLS-1$ //$NON-NLS-2$
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				return createFileFromResource(formatFile);
			}

			@Override
			public void performDone(IJobChangeEvent event) {
				if (openFile) {
					LSPEclipseUtils.open(formatFile.getLocationURI().toString(), null);
				}
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
				return new Status(IStatus.ERROR, ClangdPlugin.PLUGIN_ID, "Cannot create " + format_file + " file", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return Status.OK_STATUS;
	}

}
