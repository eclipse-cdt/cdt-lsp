/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp;

import org.eclipse.cdt.lsp.editor.AbstractCEditorPropertyTester;
import org.eclipse.cdt.lsp.editor.DefaultCEditorPropertyTester;
import org.eclipse.cdt.lsp.server.ICLanguageServerCommandProvider;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;

public class CLanguageServerRegistry {
	private static final String EXTENSION_ID = LspPlugin.PLUGIN_ID + ".serverProvider"; //$NON-NLS-1$
	private static final String SERVER_ELEMENT = "server"; //$NON-NLS-1$
	private static final String ELEMENT_CONTENT_TESTER = "contentTester"; //$NON-NLS-1$
	private static final String CLASS = "class";
	private final IExtensionPoint extension;

	public CLanguageServerRegistry() {
		extension = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_ID);
	}

	public AbstractCEditorPropertyTester createCEditorPropertyTester() throws InvalidRegistryObjectException {
		AbstractCEditorPropertyTester result = (AbstractCEditorPropertyTester) getInstanceFromExtension(ELEMENT_CONTENT_TESTER,
				AbstractCEditorPropertyTester.class);
		if (result == null) {
			LspPlugin.logError("No C/C++ editor input tester defined");
			return new DefaultCEditorPropertyTester();
		}
		return result;
	}

	public ICLanguageServerCommandProvider createCLanguageServerCommandProvider() {
		ICLanguageServerCommandProvider result = (ICLanguageServerCommandProvider) getInstanceFromExtension(SERVER_ELEMENT,
				ICLanguageServerCommandProvider.class);

		if (result == null) {
			LspPlugin.logError("No C/C++ language server defined");
		}
		return result;
	}

	private <T> Object getInstanceFromExtension(String configurationElementName, Class<T> clazz) {
		Object result = null;
		for (IConfigurationElement config : extension.getConfigurationElements()) {
			if (configurationElementName.equals(config.getName())) {
				try {
					Object obj = config.createExecutableExtension(CLASS);
					result = Adapters.adapt(obj, clazz);
				} catch (CoreException e) {
					LspPlugin.logError(e.getMessage(), e);
				}
				if (result != null) {
					break;
				}
			}
		}
		return result;
	}

}
