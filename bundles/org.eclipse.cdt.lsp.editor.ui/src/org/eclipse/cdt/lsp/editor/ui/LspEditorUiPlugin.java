/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
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

package org.eclipse.cdt.lsp.editor.ui;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class LspEditorUiPlugin extends AbstractUIPlugin {
	private IPreferenceStore preferenceStore;
	private IWorkspace workspace;

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.lsp.editor.ui"; //$NON-NLS-1$

	// The shared instance
	private static LspEditorUiPlugin plugin;
	
	/**
	 * The constructor
	 */
	public LspEditorUiPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		ServiceTracker<IWorkspace, IWorkspace> workspaceTracker = new ServiceTracker<>(context, IWorkspace.class, null);
		workspaceTracker.open();
		workspace = workspaceTracker.getService();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static LspEditorUiPlugin getDefault() {
		return plugin;
	}
	
	public IPreferenceStore getLspEditorPreferences() {
		if (preferenceStore == null) {
			preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, LspEditorUiPlugin.PLUGIN_ID);
		}
		return preferenceStore;
	}
	
	public static void logError(String message, Throwable throwable) {
		getDefault().getLog().error(message, throwable);
	}
	
	public IWorkspace getWorkspace() {
		return workspace;
	}

}
