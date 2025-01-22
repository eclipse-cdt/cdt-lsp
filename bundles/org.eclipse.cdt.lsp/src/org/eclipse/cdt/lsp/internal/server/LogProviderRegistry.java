/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.server;

import java.util.Optional;

import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.cdt.lsp.server.ILogProvider;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public final class LogProviderRegistry extends AbstractProviderRegistry {
	private static final String EXTENSION_ID = LspPlugin.PLUGIN_ID + ".logProvider"; //$NON-NLS-1$
	private static final String LOGGER_ELEMENT = "logger"; //$NON-NLS-1$

	public static Optional<ILogProvider> createLogProvider() {
		for (IConfigurationElement configurationElement : Platform.getExtensionRegistry()
				.getExtensionPoint(EXTENSION_ID).getConfigurationElements()) {
			if (LOGGER_ELEMENT.equals(configurationElement.getName())) {
				return Optional
						.ofNullable((ILogProvider) getInstanceFromExtension(configurationElement, ILogProvider.class));
			}
		}
		return Optional.empty();
	}
}
