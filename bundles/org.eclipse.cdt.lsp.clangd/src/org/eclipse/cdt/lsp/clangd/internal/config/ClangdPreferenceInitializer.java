/*******************************************************************************
 * Copyright (c) 2023, 2025 ArSysOp.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.clangd.internal.config;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.config.ConfigurationPreferencesDefaults;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public final class ClangdPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		ServiceCaller.callOnce(getClass(), ClangdConfiguration.class, new ConfigurationPreferencesDefaults<>());
	}

}
