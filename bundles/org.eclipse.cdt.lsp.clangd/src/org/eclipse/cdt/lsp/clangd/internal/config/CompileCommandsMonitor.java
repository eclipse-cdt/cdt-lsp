/*******************************************************************************
 * Copyright (c) 2023 COSEDA Technologies GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Dominic Scharfe (COSEDA Technologies GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd.internal.config;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.cdt.lsp.clangd.plugin.ClangdPlugin;
import org.eclipse.cdt.lsp.util.LspUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Detects changes (add/delete/content) of JSON Compilation Database Format
 * Specification files ({@value #CDBF_SPECIFICATION_JSON_FILE}) in the
 * {@link IWorkspace workspace} and {@link #restartLanguageServers() restarts
 * the language servers} if any cpp file from the affected projects is  open
 * in an editor.
 */
public class CompileCommandsMonitor {
	private static final String CDBF_SPECIFICATION_JSON_FILE = "compile_commands.json"; //$NON-NLS-1$

	private static final long DEBOUNCE_DELAY = 2000; // ms

	private final IWorkspace workspace;

	/**
	 * Utility class for postponing the execution of a {@link Runnable} to avoid
	 * unnecessary or frequent invocation.
	 */
	private static final class Debouncer {
		private long debounceDelay;
		private ScheduledExecutorService scheduler;
		private ScheduledFuture<?> debounceTimer;

		public Debouncer(long debounceDelay) {
			this.debounceDelay = debounceDelay;
		}

		public void run(Runnable runnable) {
			if (debounceTimer != null && !debounceTimer.isDone()) {
				debounceTimer.cancel(true);
			}

			debounceTimer = scheduler.schedule(runnable, debounceDelay, TimeUnit.MILLISECONDS);
		}

		public void start() {
			scheduler = Executors.newScheduledThreadPool(1);
		}

		public void stop() {
			scheduler.shutdown();
		}
	}

	private final Debouncer debouncer;

	private final IResourceChangeListener listener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			Set<IProject> affectedProjects = collectAffectedProjects(event);

			if (!affectedProjects.isEmpty()) {
				if (getEditors().map(editor -> Adapters.adapt(editor.getEditorInput(), IFile.class))
						.anyMatch(file -> isCppFile(file) && affectedProjects.contains(file.getProject()))) {
					debouncer.run(() -> restartLanguageServers());
				}
			}
		}

		/**
		 * Returns the editors in workbench without restoring them
		 */
		private Stream<IEditorPart> getEditors() {
			return Arrays.stream(PlatformUI.getWorkbench().getWorkbenchWindows()).map(IWorkbenchWindow::getPages)
					.flatMap(Arrays::stream).map(IWorkbenchPage::getEditorReferences).flatMap(Arrays::stream)
					.flatMap(ref -> Stream.ofNullable(ref.getEditor(false)));
		}

		private boolean isCppFile(IResource resource) {
			if (resource instanceof IFile) {
				var contentTypes = Platform.getContentTypeManager().findContentTypesFor(((IFile) resource).getName());
				return Arrays.stream(contentTypes).anyMatch(contentType -> {
					return LspUtils.isCContentType(contentType.getId());
				});
			}
			return false;
		}

		/**
		 * Collects all projects where where compile_commands.json files were
		 * added/removed/changed
		 */
		private Set<IProject> collectAffectedProjects(IResourceChangeEvent event) {
			Set<IProject> projects = new HashSet<>();

			if (event.getDelta() != null && event.getType() == IResourceChangeEvent.POST_CHANGE) {
				try {
					event.getDelta().accept(delta -> {
						if ((delta.getKind() == IResourceDelta.ADDED || delta.getKind() == IResourceDelta.REMOVED
								|| (delta.getFlags() & IResourceDelta.CONTENT) != 0)
								&& CDBF_SPECIFICATION_JSON_FILE.equals(delta.getResource().getName())) {
							projects.add(delta.getResource().getProject());
						}

						return true;
					});
				} catch (CoreException e) {
					StatusManager.getManager().handle(e, ClangdPlugin.PLUGIN_ID);
				}
			}
			return projects;
		}
	};

	public CompileCommandsMonitor(IWorkspace workspace) {
		this.workspace = workspace;
		this.debouncer = new Debouncer(DEBOUNCE_DELAY);
	}

	protected void restartLanguageServers() {
		LspUtils.getLanguageServers().forEach(w -> {
			try {
				w.restart();
			} catch (IOException e) {
				StatusManager.getManager().handle(
						new Status(IStatus.ERROR, ClangdPlugin.PLUGIN_ID, "Could not restart language servers"),
						StatusManager.LOG);
			}
		});
	}

	public CompileCommandsMonitor start() {
		workspace.addResourceChangeListener(listener);
		debouncer.start();
		return this;
	}

	public void stop() {
		workspace.removeResourceChangeListener(listener);
		debouncer.stop();
	}
}
