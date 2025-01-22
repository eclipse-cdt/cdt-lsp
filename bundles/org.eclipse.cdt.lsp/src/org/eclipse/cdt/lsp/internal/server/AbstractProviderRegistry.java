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

import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class AbstractProviderRegistry {
	private static final String CLASS = "class"; //$NON-NLS-1$

	protected static <T> Object getInstanceFromExtension(IConfigurationElement configurationElement, Class<T> clazz) {
		Object result = null;
		try {
			Object obj = configurationElement.createExecutableExtension(CLASS);
			result = Adapters.adapt(obj, clazz);
		} catch (CoreException e) {
			Platform.getLog(LogProviderRegistry.class).log(e.getStatus());
		}
		return result;
	}
}
