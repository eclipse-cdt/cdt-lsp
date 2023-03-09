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

package org.eclipse.cdt.lsp.editor.ui.clangd;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.cdt.lsp.LspUtils;
import org.eclipse.cdt.lsp.editor.ui.LspEditorUiPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Detects changes (add/delete/content) of JSON Compilation Database Format
 * Specification files ({@value #CDBF_SPECIFICATION_JSON_FILE}) in the
 * {@link IWorkspace workspace} and
 * {@link CompileCommandsMonitor#refreshEditor(IEditorPart) refreshes} open
 * editors if their {@link IEditorPart#getEditorInput()} is affected.
 */
public class CompileCommandsMonitor {
	private static final String CDBF_SPECIFICATION_JSON_FILE = "compile_commands.json";

	private final IResourceChangeListener listener = new IResourceChangeListener() {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			Set<IProject> projects = collectAffectedProjects(event);

			if (!projects.isEmpty()) {
				// collect all open editors which have cpp files as input and refresh them
				Arrays.stream(PlatformUI.getWorkbench().getWorkbenchWindows()).map(IWorkbenchWindow::getPages)
						.flatMap(Arrays::stream).map(IWorkbenchPage::getEditorReferences).flatMap(Arrays::stream)
						.flatMap(ref -> Stream.ofNullable(ref.getEditor(false))).forEach(editor -> {
							IFile file = Adapters.adapt(editor.getEditorInput(), IFile.class);

							if (isCppFile(file) && projects.contains(file.getProject())) {
								refreshEditor(editor, file);
							}
						});
			}
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
					StatusManager.getManager().handle(e, LspEditorUiPlugin.PLUGIN_ID);
				}
			}
			return projects;
		}
	};

	private final IWorkspace workspace;

	public CompileCommandsMonitor(IWorkspace workspace) {
		this.workspace = workspace;
	}

	public CompileCommandsMonitor start() {
		workspace.addResourceChangeListener(listener);
		return this;
	}

	public void stop() {
		workspace.removeResourceChangeListener(listener);
	}

	private static void refreshEditor(IEditorPart editor, IFile file) {
		ITextViewer textViewer = Adapters.adapt(editor, ITextViewer.class);

		// Notify clangd about the file change --> doesn't seem to work
//		org.eclipse.lsp4e.LanguageServers.forDocument(textViewer.getDocument()).computeFirst(server -> {
//			server.getWorkspaceService()
//					.didChangeWatchedFiles(new DidChangeWatchedFilesParams(Arrays.asList(new FileEvent(
//							file.getProject().getFile(CDBF_SPECIFICATION_JSON_FILE).getLocationURI().toASCIIString(),
//							FileChangeType.Changed))));
//			return new CompletableFuture<>();
//		});

		// Refresh the editors after 5 seconds -> see https://reviews.llvm.org/D92663
		UIJob.create("Refresh Editors", monitor -> {
			if (textViewer.getDocument() == null)
				return;
			int rangeOffset = textViewer.getTopIndexStartOffset();
			int rangeLength = textViewer.getBottomIndexEndOffset() - rangeOffset;
			editor.getSite().getPage().reuseEditor((IReusableEditor) editor, editor.getEditorInput());
			textViewer.revealRange(rangeOffset, rangeLength);
		}).schedule(5000);
	}
}
