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

import java.util.Optional;

import org.eclipse.cdt.lsp.editor.DefaultCEditorTest;
import org.eclipse.cdt.lsp.editor.ICEditorTest;
import org.eclipse.cdt.lsp.server.EnableExpression;
import org.eclipse.cdt.lsp.server.ICLanguageServerCommandProvider;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

public class CLanguageServerRegistry {
	private static final String EXTENSION_ID = LspPlugin.PLUGIN_ID + ".serverProvider"; //$NON-NLS-1$
	private static final String SERVER_ELEMENT = "server"; //$NON-NLS-1$
	private static final String ELEMENT_CONTENT_TESTER = "contentTester"; //$NON-NLS-1$
	private static final String CLASS = "class"; //$NON-NLS-1$
	private static final String ENABLED_WHEN_ATTRIBUTE = "enabledWhen"; //$NON-NLS-1$
	private final IExtensionPoint cExtensionPoint;

	public CLanguageServerRegistry() {
		cExtensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_ID);
	}

	public ICEditorTest createCEditorTest() throws InvalidRegistryObjectException {
		ICEditorTest propertyTester = (ICEditorTest) getInstanceFromExtension(ELEMENT_CONTENT_TESTER,ICEditorTest.class);
		if (propertyTester == null) {
			LspPlugin.logWarning("No C/C++ editor input tester defined");
			return new DefaultCEditorTest();
		}
		return propertyTester;
	}

	public ICLanguageServerCommandProvider createCLanguageServerCommandProvider() {
		ICLanguageServerCommandProvider provider = (ICLanguageServerCommandProvider) getInstanceFromExtension(SERVER_ELEMENT,
				ICLanguageServerCommandProvider.class);

		if (provider == null) {
			LspPlugin.logWarning("No C/C++ language server defined");
		}
				
		return provider;
	}
	
	public EnableExpression getEnablementExpression() {
		EnableExpression enableExpression = null;
		for (IConfigurationElement configurationElement : cExtensionPoint.getConfigurationElements()) {
			if (SERVER_ELEMENT.equals(configurationElement.getName())) {
				if (configurationElement.getChildren(ENABLED_WHEN_ATTRIBUTE) != null) {
					IConfigurationElement[] enabledWhenElements = configurationElement.getChildren(ENABLED_WHEN_ATTRIBUTE);
					if (enabledWhenElements.length == 1) {
						IConfigurationElement enabledWhen = enabledWhenElements[0];
						IConfigurationElement[] enabledWhenChildren = enabledWhen.getChildren();
						if (enabledWhenChildren.length == 1) {
							try {
								enableExpression = new EnableExpression(this::getEvaluationContext, ExpressionConverter.getDefault().perform(enabledWhenChildren[0]));
							} catch (CoreException e) {
								LspPlugin.logWarning(e.getMessage(), e);
							}
						}
					}
				}
			}
		}
		return enableExpression;
	}
	
	private IEvaluationContext getEvaluationContext() {
		return Optional.ofNullable(PlatformUI.getWorkbench().getService(IHandlerService.class)).map(IHandlerService::getCurrentState).orElse(null);
	}

	private <T> Object getInstanceFromExtension(String configurationElementName, Class<T> clazz) {
		Object result = null;
		for (IConfigurationElement configurationElement : cExtensionPoint.getConfigurationElements()) {
			if (configurationElementName.equals(configurationElement.getName())) {
				try {
					Object obj = configurationElement.createExecutableExtension(CLASS);
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
