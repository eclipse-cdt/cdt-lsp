/*******************************************************************************
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.editor;

import org.eclipse.cdt.lsp.config.ConfigurationPreferencesDefaults;
import org.eclipse.cdt.lsp.editor.EditorConfiguration;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.lsp4e.LanguageServerPlugin;

public final class EditorPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		ServiceCaller.callOnce(getClass(), EditorConfiguration.class, new ConfigurationPreferencesDefaults<>());
		IPreferenceStore store = LanguageServerPlugin.getDefault().getPreferenceStore();
		// increase timeout from 5 to 30 seconds. Fetching formatting regions from the language server for large files (>20k lines of code) can take more than 5 sec.:
		store.setValue("org.eclipse.cdt.lsp.server.timeout.willSaveWaitUntil", 30); //$NON-NLS-1$
	}

}
