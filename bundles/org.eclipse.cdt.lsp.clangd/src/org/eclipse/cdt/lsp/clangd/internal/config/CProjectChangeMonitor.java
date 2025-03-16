/*******************************************************************************
 * Copyright (c) 2024, 2025 Bachmann electronic GmbH and others.
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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.lsp.clangd.ClangdCProjectDescriptionListener;
import org.eclipse.cdt.lsp.clangd.ClangdCompilationDatabaseSettings;
import org.eclipse.cdt.lsp.clangd.ClangdPostBuildCompilationDatabaseSetter;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ServiceCaller;

/**
 * This monitor listens to C project description changes and post-build resource changes.
 */
public class CProjectChangeMonitor {

	private final ServiceCaller<ClangdCompilationDatabaseSettings> settings = new ServiceCaller<>(getClass(),
			ClangdCompilationDatabaseSettings.class);

	private final ServiceCaller<ClangdPostBuildCompilationDatabaseSetter> clangdPostBuildCompilationDatabaseSetter = new ServiceCaller<>(
			getClass(), ClangdPostBuildCompilationDatabaseSetter.class);

	private final ServiceCaller<ClangdCProjectDescriptionListener> clangdCProjectDescriptionListener = new ServiceCaller<>(
			getClass(), ClangdCProjectDescriptionListener.class);

	private final IResourceChangeListener postBuildListener = new IResourceChangeListener() {

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getDelta() != null) {
				for (var project : collectAffectedProjects(event)) {
					if (isSetCompilationDatabaseEnabled(project)) {
						clangdPostBuildCompilationDatabaseSetter.call(setter -> {
							try {
								setter.setCompilationDatabase(project.getActiveBuildConfig());
							} catch (CoreException e) {
								Platform.getLog(getClass()).error(e.getMessage(), e);
							}
						});
					}
				}
			}
		}

		private Set<IProject> collectAffectedProjects(IResourceChangeEvent event) {
			Set<IProject> projects = new HashSet<>();
			try {
				event.getDelta().accept(delta -> {
					if (delta.getResource() instanceof IProject project) {
						projects.add(project);
					}
					return true;
				});
			} catch (CoreException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}
			return projects;
		}

	};

	private final ICProjectDescriptionListener descriptionListener = new ICProjectDescriptionListener() {

		@Override
		public void handleEvent(CProjectDescriptionEvent event) {
			var project = event.getProject();
			if (project != null && isSetCompilationDatabaseEnabled(project)) {
				clangdCProjectDescriptionListener.call(c -> c.handleEvent(event));
			}
		}

	};

	public CProjectChangeMonitor start(IWorkspace workspace) {
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
