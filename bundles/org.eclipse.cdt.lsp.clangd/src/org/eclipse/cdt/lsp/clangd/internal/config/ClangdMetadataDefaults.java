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

import java.util.List;

import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.config.ConfigurationMetadataBase;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.osgi.service.component.annotations.Component;

@Component(property = { "service.ranking:Integer=0" })
public final class ClangdMetadataDefaults extends ConfigurationMetadataBase implements ClangdMetadata {

	@Override
	protected List<PreferenceMetadata<?>> definePreferences() {
		return Predefined.defaults;
	}

}
