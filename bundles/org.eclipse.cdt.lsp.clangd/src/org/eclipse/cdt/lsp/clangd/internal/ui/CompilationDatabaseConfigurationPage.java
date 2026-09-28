/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/
package org.eclipse.cdt.lsp.clangd.internal.ui;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.cdt.lsp.ui.ConfigurationArea;
import org.eclipse.cdt.lsp.ui.ConfigurationPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;

/**
 * Dedicated page for project/workspace compilation database settings.
 */
public final class CompilationDatabaseConfigurationPage extends ConfigurationPage<ClangdConfiguration, ClangdOptions> {
	private static final String PREFERENCE_PAGE_ID = "org.eclipse.cdt.lsp.clangd.editor.compilationDatabasePropertyPage"; //$NON-NLS-1$

	@Override
	protected ClangdConfiguration getConfiguration(IWorkbench workbench) {
		return workbench.getService(ClangdConfiguration.class);
	}

	@Override
	protected ClangdOptions configurationDefaults() {
		return configuration.defaults();
	}

	@Override
	protected ClangdOptions configurationOptions(IAdaptable element) {
		return configuration.options(element);
	}

	@Override
	protected ConfigurationArea<ClangdOptions> getConfigurationArea(Composite composite, boolean isProjectScope) {
		IProject project = isProjectScope ? getElement().getAdapter(IProject.class) : null;
		return new CompilationDatabaseArea(composite, isProjectScope, project);
	}

	@Override
	protected String getPreferenceId() {
		return PREFERENCE_PAGE_ID;
	}

	@Override
	protected boolean hasProjectSpecificOptions() {
		return projectScope()//
				.map(p -> p.getNode(configuration.qualifier()))//
				.filter(CompilationDatabaseConfigurationPage::hasCompilationDatabaseProjectSpecificOptions)//
				.isPresent();
	}

	private static boolean hasCompilationDatabaseProjectSpecificOptions(IEclipsePreferences preferences) {
		return preferences.get(ClangdMetadata.Predefined.setCompilationDatabase.identifer(), null) != null
				|| preferences.get(ClangdMetadata.Predefined.compilationDatabaseOverride.identifer(), null) != null;
	}
}