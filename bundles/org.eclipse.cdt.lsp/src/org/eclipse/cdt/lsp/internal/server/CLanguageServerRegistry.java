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
 * Alexander Fedorov (ArSysOp) - use Platform for logging
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.server;

import java.util.HashMap;
import java.util.Optional;

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.server.DefaultCLanguageServerProvider;
import org.eclipse.cdt.lsp.server.EnableExpression;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.core.expressions.ExpressionConverter;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;

public class CLanguageServerRegistry {
	private static final String EXTENSION_ID = LspPlugin.PLUGIN_ID + ".serverProvider"; //$NON-NLS-1$
	private static final String SERVER_ELEMENT = "server"; //$NON-NLS-1$
	private static final String CLASS = "class"; //$NON-NLS-1$
	private static final String PRIORITY = "priority"; //$NON-NLS-1$
	private static final String ENABLED_WHEN_ATTRIBUTE = "enabledWhen"; //$NON-NLS-1$
	private final IExtensionPoint cExtensionPoint;
	private ICLanguageServerProvider prioritizedProvider = null;
	private Priority highestPrio = Priority.low;

	private enum Priority {
		low, normal, high
	}

	public CLanguageServerRegistry() {
		cExtensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_ID);
	}

	public ICLanguageServerProvider createCLanguageServerProvider() {
		prioritizedProvider = null;
		highestPrio = Priority.low;
		HashMap<Priority, ICLanguageServerProvider> providers = new HashMap<Priority, ICLanguageServerProvider>();
		for (IConfigurationElement configurationElement : cExtensionPoint.getConfigurationElements()) {
			if (SERVER_ELEMENT.equals(configurationElement.getName())) {
				ICLanguageServerProvider provider = (ICLanguageServerProvider) getInstanceFromExtension(
						configurationElement, ICLanguageServerProvider.class);
				if (provider != null) {
					// set enable expression:
					EnableExpression enableExpression = null;
					if (configurationElement.getChildren(ENABLED_WHEN_ATTRIBUTE) != null) {
						IConfigurationElement[] enabledWhenElements = configurationElement
								.getChildren(ENABLED_WHEN_ATTRIBUTE);
						if (enabledWhenElements.length == 1) {
							IConfigurationElement enabledWhen = enabledWhenElements[0];
							IConfigurationElement[] enabledWhenChildren = enabledWhen.getChildren();
							if (enabledWhenChildren.length == 1) {
								try {
									enableExpression = new EnableExpression(this::getEvaluationContext,
											ExpressionConverter.getDefault().perform(enabledWhenChildren[0]));
								} catch (CoreException e) {
									Platform.getLog(getClass()).warn("Failed to create enable expression for "
											+ configurationElement.getNamespaceIdentifier(), e);
								}
							}
						}
					}
					provider.setEnableExpression(enableExpression);
					// save priority attribute:
					providers.put(Priority.valueOf(configurationElement.getAttribute(PRIORITY)), provider);
				}
			}
		}
		if (providers.isEmpty()) {
			Platform.getLog(getClass()).warn("No C/C++ language server defined");
			prioritizedProvider = new DefaultCLanguageServerProvider();
		} else {
			// get provider with highest priority:
			providers.forEach((key, value) -> {
				if (key.compareTo(highestPrio) >= 0) {
					highestPrio = key;
					prioritizedProvider = value;
				}
			});
		}
		return prioritizedProvider;
	}

	private IEvaluationContext getEvaluationContext() {
		return Optional.ofNullable(PlatformUI.getWorkbench().getService(IHandlerService.class))
				.map(IHandlerService::getCurrentState).orElse(null);
	}

	private <T> Object getInstanceFromExtension(IConfigurationElement configurationElement, Class<T> clazz) {
		Object result = null;
		try {
			Object obj = configurationElement.createExecutableExtension(CLASS);
			result = Adapters.adapt(obj, clazz);
		} catch (CoreException e) {
			Platform.getLog(getClass()).log(e.getStatus());
		}
		return result;
	}

}
