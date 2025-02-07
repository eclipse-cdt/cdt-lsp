/*******************************************************************************
 * Copyright (c) 2023, 2024, 2025 Bachmann electronic GmbH and others.
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

package org.eclipse.cdt.lsp.plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.cdt.lsp.internal.server.CLanguageServerEnableCache;
import org.eclipse.cdt.lsp.internal.server.CLanguageServerRegistry;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class LspPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.lsp"; //$NON-NLS-1$
	public static final String LSP_C_EDITOR_ID = "org.eclipse.cdt.lsp.CEditor"; //$NON-NLS-1$
	public static final String C_EDITOR_ID = "org.eclipse.cdt.ui.editor.CEditor"; //$NON-NLS-1$

	// The shared instance
	private static LspPlugin plugin;

	private ICLanguageServerProvider cLanguageServerProvider;

	// Disable warnings, see https://github.com/eclipse-cdt/cdt-lsp/issues/88 and https://github.com/eclipse-cdt/cdt-lsp/issues/101.
	// We keep this reference to avoid the logger being garbage collected.
	private static final Logger logger = Logger.getLogger("org.eclipse.tm4e.core.internal.oniguruma.OnigRegExp"); //$NON-NLS-1$

	/**
	 * The constructor
	 */
	public LspPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		cLanguageServerProvider = new CLanguageServerRegistry().createCLanguageServerProvider();

		// Disable warnings, see https://github.com/eclipse-cdt/cdt-lsp/issues/88 and https://github.com/eclipse-cdt/cdt-lsp/issues/101
		logger.setLevel(Level.SEVERE);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		CLanguageServerEnableCache.stop();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static LspPlugin getDefault() {
		return plugin;
	}

	public ICLanguageServerProvider getCLanguageServerProvider() {
		return cLanguageServerProvider;
	}

}
