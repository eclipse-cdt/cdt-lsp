/*******************************************************************************
 * Copyright (c) 2023 ArSysOp.
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
import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

public final class ClangdPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		ServiceCaller.callOnce(getClass(), ClangdConfiguration.class, this::initializeDefaults);
	}

	private void initializeDefaults(ClangdConfiguration configuration) {
		ClangdMetadata metadata = (ClangdMetadata) configuration.metadata();
		String qualifier = configuration.qualifier();
		initializeString(metadata.clangdPath(), qualifier);
		initializeBoolean(metadata.useTidy(), qualifier);
		initializeBoolean(metadata.useBackgroundIndex(), qualifier);
		initializeString(metadata.completionStyle(), qualifier);
		initializeBoolean(metadata.prettyPrint(), qualifier);
		initializeString(metadata.queryDriver(), qualifier);
		initializeString(metadata.additionalOptions(), qualifier);
	}

	private void initializeBoolean(PreferenceMetadata<Boolean> preference, String qualifier) {
		DefaultScope.INSTANCE.getNode(qualifier).putBoolean(preference.identifer(), preference.defaultValue());
	}

	private void initializeString(PreferenceMetadata<String> preference, String qualifier) {
		DefaultScope.INSTANCE.getNode(qualifier).put(preference.identifer(), preference.defaultValue());
	}
}
