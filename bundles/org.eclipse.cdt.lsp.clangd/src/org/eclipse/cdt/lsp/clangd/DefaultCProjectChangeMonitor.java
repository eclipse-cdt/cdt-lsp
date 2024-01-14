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

package org.eclipse.cdt.lsp.clangd;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This default monitor listens to C project description changes. Can be derived by vendors to add own listeners/behavior.
 * This can be done by using this class as superclass and add the new class as OSGi service with a service.ranking > 0.
 */
@Component(property = { "service.ranking:Integer=0" })
public class DefaultCProjectChangeMonitor implements CProjectChangeMonitor {

	@Reference
	MacroResolver macroResolver;

	@Reference
	private ClangdCProjectDescriptionListener clangdListener;

	private final ICProjectDescriptionListener listener = new ICProjectDescriptionListener() {

		@Override
		public void handleEvent(CProjectDescriptionEvent event) {
			clangdListener.handleEvent(event, macroResolver);
		}

	};

	@Override
	public CProjectChangeMonitor start() {
		CCorePlugin.getDefault().getProjectDescriptionManager().addCProjectDescriptionListener(listener,
				CProjectDescriptionEvent.APPLIED);
		return this;
	}

	@Override
	public void stop() {
		CCorePlugin.getDefault().getProjectDescriptionManager().removeCProjectDescriptionListener(listener);
	}

}
