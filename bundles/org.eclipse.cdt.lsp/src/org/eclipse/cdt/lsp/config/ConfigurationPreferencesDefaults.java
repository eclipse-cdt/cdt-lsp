/*******************************************************************************
 * Copyright (c) 2025 ArSysOp.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.config;

import java.util.function.Consumer;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

/**
 * Does actual default preferences initialization with provided {@link Configuration} instance
 *
 * Example usage:
 * <pre>
 * ServiceCaller.callOnce(getClass(), EditorConfiguration.class, new ConfigurationPreferencesDefaults<>())
 * </pre>
 *
 * @see AbstractPreferenceInitializer
 *
 * @since 3.0
 */
public final class ConfigurationPreferencesDefaults<C extends Configuration> implements Consumer<C> {

	@Override
	public void accept(C configuration) {
		configuration.metadata().defined().forEach(a -> initializePreferences(configuration.qualifier(), a));
	}

	private void initializePreferences(String qualifier, PreferenceMetadata<?> pm) {
		if (pm.defaultValue() instanceof Boolean value) {
			DefaultScope.INSTANCE.getNode(qualifier).putBoolean(pm.identifer(), value);
		} else if (pm.defaultValue() instanceof String value) {
			DefaultScope.INSTANCE.getNode(qualifier).put(pm.identifer(), value);
		}
	}

}
