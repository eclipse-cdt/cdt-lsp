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

import org.eclipse.cdt.lsp.PreferredOptions;
import org.eclipse.cdt.lsp.clangd.ClangdContentAssistMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdContentAssistOptions;
import org.eclipse.cdt.lsp.config.ConfigurationMetadata;
import org.eclipse.core.runtime.preferences.IScopeContext;

public final class ClangdContentAssistPreferredOptions extends PreferredOptions implements ClangdContentAssistOptions {

	public ClangdContentAssistPreferredOptions(ConfigurationMetadata metadata, String qualifier,
			IScopeContext[] scopes) {
		super(metadata, qualifier, scopes);
	}

	@Override
	public boolean fillFunctionArguments() {
		return booleanValue(ClangdContentAssistMetadata.fillFunctionArguments);
	}
}
