/*******************************************************************************
 * Copyright (c) 2023, 2024 Bachmann electronic GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 * Alexander Fedorov (ArSysOp) - use Platform for logging
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd.plugin;

import org.eclipse.cdt.lsp.clangd.internal.config.CProjectChangeMonitor;
import org.eclipse.cdt.lsp.clangd.internal.config.ClangFormatMonitor;
import org.eclipse.cdt.lsp.clangd.internal.config.ClangdConfigFileMonitor;
import org.eclipse.cdt.lsp.clangd.internal.config.CompileCommandsMonitor;
import org.eclipse.cdt.lsp.clangd.utils.ClangFormatUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class ClangdPlugin extends AbstractUIPlugin {
	private IWorkspace workspace;
	private CompileCommandsMonitor compileCommandsMonitor;
	private CProjectChangeMonitor cProjectChangeMonitor;
	private ClangdConfigFileMonitor configFileMonitor;
	private ClangFormatMonitor formatMonitor;

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.lsp.clangd"; //$NON-NLS-1$

	// The shared instance
	private static ClangdPlugin plugin;

	/**
	 * The constructor
	 */
	public ClangdPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		ServiceTracker<IWorkspace, IWorkspace> workspaceTracker = new ServiceTracker<>(context, IWorkspace.class, null);
		workspaceTracker.open();
		workspace = workspaceTracker.getService();
		compileCommandsMonitor = new CompileCommandsMonitor(workspace).start();
		cProjectChangeMonitor = new CProjectChangeMonitor().start();
		configFileMonitor = new ClangdConfigFileMonitor(workspace).start();
		formatMonitor = new ClangFormatMonitor(new ClangFormatUtils()).start();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		compileCommandsMonitor.stop();
		cProjectChangeMonitor.stop();
		configFileMonitor.stop();
		formatMonitor.stop();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ClangdPlugin getDefault() {
		return plugin;
	}

	public IWorkspace getWorkspace() {
		return workspace;
	}

}
