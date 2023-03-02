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

import java.util.List;
import java.util.ArrayList;

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.server.ICompileCommandsDirLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

public class CompileCommandsDirLocatorRegistry {
	private static final String EXTENSION_ID = LspPlugin.PLUGIN_ID + ".compileCommands"; //$NON-NLS-1$
	private static final String LOCATOR_ELEMENT = "locator"; //$NON-NLS-1$
	private final IExtensionPoint cExtensionPoint;
	private final ClassCreator cClassCreator;
	
	public CompileCommandsDirLocatorRegistry() {
		this.cExtensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_ID);
		this.cClassCreator = new ClassCreator();
	}
		
	public List<ICompileCommandsDirLocator> createCompileCommandsDirLocators() {
		List<ICompileCommandsDirLocator> locators = new ArrayList<>();
		var configuratonElements = cExtensionPoint.getConfigurationElements();
		for (IConfigurationElement configurationElement : configuratonElements) {
			if (LOCATOR_ELEMENT.equals(configurationElement.getName())) {
				 cClassCreator.getInstanceFromExtension(configurationElement, ICompileCommandsDirLocator.class).ifPresent(locator -> locators.add(locator));
			}
		}
		return locators;
	}
}
