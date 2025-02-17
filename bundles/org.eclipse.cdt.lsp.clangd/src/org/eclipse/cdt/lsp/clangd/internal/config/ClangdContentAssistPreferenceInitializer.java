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
package org.eclipse.cdt.lsp.clangd.internal.config;

import org.eclipse.cdt.lsp.clangd.ClangdContentAssistConfiguration;
import org.eclipse.cdt.lsp.config.ConfigurationPreferencesDefaults;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public final class ClangdContentAssistPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		ServiceCaller.callOnce(getClass(), ClangdContentAssistConfiguration.class,
				new ConfigurationPreferencesDefaults<>());
	}

}
