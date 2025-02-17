/*******************************************************************************
 * Copyright (c) 2025 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.examples.preferences;

import java.util.List;

import org.eclipse.cdt.lsp.config.ConfigurationMetadataBase;
import org.eclipse.cdt.lsp.editor.EditorMetadata;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.osgi.service.component.annotations.Component;

/**
 * Demonstrates how to change default of showTryLspBanner
 * <p>
 * NOTE: Because this example code also includes HidePreferLsp the effect of
 * this change is not apparent because hiding prefer LSP overrides the banner.
 * Change {@link HidePreferLsp#showPreferLsp(boolean)} to return true (or remove
 * the HidePreferLsp Service-Component entry in MANIFEST.MF)
 */
@Component(property = { "service.ranking:Integer=100" })
public class HideTryNewExperienceBanner extends ConfigurationMetadataBase implements EditorMetadata {

	@Override
	protected List<PreferenceMetadata<?>> definePreferences() {
		return overrideOne(Predefined.defaults, overrideBoolean(Predefined.showTryLspBanner, false));
	}
}
