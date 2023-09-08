/*******************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.editor;

import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

public final class EditorPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		ServiceCaller.callOnce(getClass(), EditorConfiguration.class, this::initializeDefaults);
	}

	private void initializeDefaults(EditorConfiguration configuration) {
		EditorMetadata metadata = configuration.metadata();
		String qualifier = configuration.qualifier();
		initializeBoolean(metadata.preferLspEditor(), qualifier);
		initializeBoolean(metadata.formatOnSave(), qualifier);
		initializeBoolean(metadata.formatAllLines(), qualifier);
		initializeBoolean(metadata.formatEditedLines(), qualifier);
	}

	private void initializeBoolean(PreferenceMetadata<Boolean> preference, String qualifier) {
		DefaultScope.INSTANCE.getNode(qualifier).putBoolean(preference.identifer(), preference.defaultValue());
	}
}
