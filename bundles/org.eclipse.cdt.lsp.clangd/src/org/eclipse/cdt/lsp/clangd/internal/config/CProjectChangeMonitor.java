/*******************************************************************************
 * Copyright (c) 2024 Bachmann electronic GmbH and others.
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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.lsp.clangd.ClangdCProjectDescriptionListener;
import org.eclipse.cdt.lsp.clangd.MacroResolver;
import org.eclipse.core.runtime.ServiceCaller;

/**
 * This monitor listens to C project description changes.
 */
public class CProjectChangeMonitor {
	MacroResolver macroResolver = new MacroResolver();

	private final ServiceCaller<ClangdCProjectDescriptionListener> clangdListener = new ServiceCaller<>(getClass(),
			ClangdCProjectDescriptionListener.class);

	private final ICProjectDescriptionListener listener = new ICProjectDescriptionListener() {

		@Override
		public void handleEvent(CProjectDescriptionEvent event) {
			clangdListener.call(c -> c.handleEvent(event, macroResolver));
		}

	};

	public CProjectChangeMonitor start() {
		CCorePlugin.getDefault().getProjectDescriptionManager().addCProjectDescriptionListener(listener,
				CProjectDescriptionEvent.APPLIED);
		return this;
	}

	public void stop() {
		CCorePlugin.getDefault().getProjectDescriptionManager().removeCProjectDescriptionListener(listener);
	}

}
