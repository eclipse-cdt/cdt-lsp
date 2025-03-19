/*******************************************************************************
 * Copyright (c) 2025 Bachmann electronic GmbH and others.
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

package org.eclipse.cdt.lsp.clangd.internal.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.lsp.clangd.ClangdCProjectDescriptionListener;
import org.eclipse.cdt.lsp.clangd.ClangdCompilationDatabaseProvider;
import org.eclipse.cdt.lsp.clangd.ClangdCompilationDatabaseSettings;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ServiceCaller;

/**
 * The setter listens to C project description changes and post-build resource changes.
 */
public class ClangdCompilationDatabaseSetter extends ClangdCompilationDatabaseSetterBase {

	private final ServiceCaller<ClangdCompilationDatabaseSettings> settings = new ServiceCaller<>(getClass(),
			ClangdCompilationDatabaseSettings.class);

	private final ServiceCaller<ClangdCompilationDatabaseProvider> clangdCompilationDatabaseProvider = new ServiceCaller<>(
			getClass(), ClangdCompilationDatabaseProvider.class);

	private final ServiceCaller<ClangdCProjectDescriptionListener> clangdCProjectDescriptionListener = new ServiceCaller<>(
			getClass(), ClangdCProjectDescriptionListener.class);

	private final IResourceChangeListener postBuildListener = new IResourceChangeListener() {
		private static final String COMPILE_COMMANDS_JSON = "compile_commands.json"; //$NON-NLS-1$

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getDelta() != null) {
				for (var project : collectAffectedProjects(event)) {
					if (isSetCompilationDatabaseEnabled(project)) {
						clangdCompilationDatabaseProvider.call(provider -> {
							provider.getCompilationDatabasePath(event, project)
									.ifPresent(path -> setCompilationDatabase(project, path));
						});
					}
				}
			}
		}

		private Set<IProject> collectAffectedProjects(IResourceChangeEvent event) {
			Map<IProject, Boolean> projectsMap = new HashMap<>();
			try {
				event.getDelta().accept(delta -> {
					if (delta.getResource() instanceof IProject project && project.hasNature(CProjectNature.C_NATURE_ID)
							&& project.isAccessible()) {
						projectsMap.put(project, true);
					} else if (delta.getResource() instanceof IFile file && file.getProject() != null
							&& file.getProject().hasNature(CProjectNature.C_NATURE_ID)
							&& file.getProject().isAccessible()) {
						if (COMPILE_COMMANDS_JSON.contentEquals(file.getName())) {
							projectsMap.put(file.getProject(), false);
						} else {
							// do NOT remove if the compile_commands.json has changed,
							// do NOT remove if the map don't contain the project (default == false):
							if (projectsMap.getOrDefault(file.getProject(), false)) {
								// remove, because we want to detect settings changes only:
								projectsMap.remove(file.getProject());
							}
						}
					}
					return true;
				});
			} catch (CoreException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}
			return projectsMap.keySet();
		}

	};

	private final ICProjectDescriptionListener descriptionListener = new ICProjectDescriptionListener() {

		@Override
		public void handleEvent(CProjectDescriptionEvent event) {
			var project = event.getProject();
			if (project != null && isSetCompilationDatabaseEnabled(project)) {
				if (!clangdCProjectDescriptionListener.call(c -> c.handleEvent(event))) {
					// no OSGi service for deprecated ClangdCProjectDescriptionListener provided, lets use the new one:
					clangdCompilationDatabaseProvider.call(provider -> {
						provider.getCompilationDatabasePath(event)
								.ifPresent(path -> setCompilationDatabase(project, path));
					});
				}
			}
		}

	};

	public IResourceChangeListener getResourceChangeListener() {
		return postBuildListener;
	}

	public ICProjectDescriptionListener getCProjectDescriptionListener() {
		return descriptionListener;
	}

	public ClangdCompilationDatabaseSetter start(IWorkspace workspace) {
		workspace.addResourceChangeListener(postBuildListener, IResourceChangeEvent.POST_BUILD);
		CCorePlugin.getDefault().getProjectDescriptionManager().addCProjectDescriptionListener(descriptionListener,
				CProjectDescriptionEvent.APPLIED);
		return this;
	}

	public void stop(IWorkspace workspace) {
		workspace.removeResourceChangeListener(postBuildListener);
		CCorePlugin.getDefault().getProjectDescriptionManager().removeCProjectDescriptionListener(descriptionListener);
	}

	private boolean isSetCompilationDatabaseEnabled(IProject project) {
		boolean[] enabled = new boolean[1];
		settings.call(s -> {
			enabled[0] = s.enableSetCompilationDatabasePath(project);
		});
		return enabled[0];
	}

}
