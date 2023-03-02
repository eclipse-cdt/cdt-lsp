/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.server;

import java.util.Optional;

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

public final class ClassCreator {
	private static final String CLASS = "class"; //$NON-NLS-1$
	
	public <T> Optional<T> getInstanceFromExtension(IConfigurationElement configurationElement, Class<T> clazz) {
		try {
			Object obj = configurationElement.createExecutableExtension(CLASS);
			if (clazz.isInstance(obj)) {
				return Optional.ofNullable(clazz.cast(obj));
			}
		} catch (CoreException e) {
			LspPlugin.logError(e.getMessage(), e);
		}
		return Optional.empty();
	}
}
