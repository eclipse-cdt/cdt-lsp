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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.lsp.clangd.ClangdCProjectDescriptionListener;
import org.eclipse.cdt.lsp.clangd.ClangdCompilationDatabaseSettings;
import org.eclipse.cdt.lsp.clangd.ClangdResourceChangeListener;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
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

	private final ServiceCaller<ClangdResourceChangeListener> clangdResourceChangeListener = new ServiceCaller<>(
			getClass(), ClangdResourceChangeListener.class);

	private final ServiceCaller<ClangdCProjectDescriptionListener> clangdListener = new ServiceCaller<>(getClass(),
			ClangdCProjectDescriptionListener.class);

	private final IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {
		private List<IProject> projects = new ArrayList<>();

		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			if (event.getDelta() != null) {
				projects.clear();
				getProjects(event.getDelta());
				for (IProject project : projects) {
					if (isEnabled(project)) {
						clangdResourceChangeListener.call(c -> {
							try {
								c.handleEvent(project.getActiveBuildConfig());
							} catch (CoreException e) {
								Platform.getLog(getClass()).error(e.getMessage(), e);
							}
						});
					}
				}
			}
		}

		private void getProjects(IResourceDelta delta) {
			if (delta.getResource() instanceof IProject project) {
				projects.add(project);
			}
			for (var child : delta.getAffectedChildren()) {
				getProjects(child);
			}
		}

	};

	private final ICProjectDescriptionListener listener = new ICProjectDescriptionListener() {

		@Override
		public void handleEvent(CProjectDescriptionEvent event) {
			var project = event.getProject();
			if (project != null && isEnabled(project)) {
				clangdListener.call(c -> c.handleEvent(event));
			}
		}

	};

	public CProjectChangeMonitor start(IWorkspace workspace) {
		workspace.addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.POST_BUILD);
		CCorePlugin.getDefault().getProjectDescriptionManager().addCProjectDescriptionListener(listener,
				CProjectDescriptionEvent.APPLIED);
		return this;
	}

	public void stop(IWorkspace workspace) {
		workspace.removeResourceChangeListener(resourceChangeListener);
		CCorePlugin.getDefault().getProjectDescriptionManager().removeCProjectDescriptionListener(listener);
	}

	private boolean isEnabled(IProject project) {
		boolean[] enabled = new boolean[1];
		settings.call(s -> {
			enabled[0] = s.enableSetCompilationDatabasePath(project);
		});
		return enabled[0];
	}

}
