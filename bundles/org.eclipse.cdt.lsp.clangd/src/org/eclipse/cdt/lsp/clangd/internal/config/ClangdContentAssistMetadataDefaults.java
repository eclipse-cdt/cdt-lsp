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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.lsp.clangd.ClangdContentAssistMetadata;
import org.eclipse.cdt.lsp.config.ConfigurationMetadataBase;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.osgi.service.component.annotations.Component;

@Component(property = { "service.ranking:Integer=0" })
public final class ClangdContentAssistMetadataDefaults extends ConfigurationMetadataBase
		implements ClangdContentAssistMetadata {

	@Override
	protected List<PreferenceMetadata<?>> definePreferences() {
		List<PreferenceMetadata<?>> defined = new ArrayList<>();
		defined.add(fillFunctionArguments);
		return defined;
	}

}
