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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.lsp.PreferredOptions;
import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.core.runtime.preferences.IScopeContext;

final class ClangdPreferredOptions extends PreferredOptions implements ClangdOptions {

	ClangdPreferredOptions(ClangdMetadata metadata, String qualifier, IScopeContext[] scopes) {
		super(metadata, qualifier, scopes);
	}

	@Override
	public String clangdPath() {
		return stringValue(ClangdMetadata.Predefined.clangdPath);
	}

	@Override
	public boolean useTidy() {
		return booleanValue(ClangdMetadata.Predefined.useTidy);
	}

	@Override
	public boolean useBackgroundIndex() {
		return booleanValue(ClangdMetadata.Predefined.useBackgroundIndex);
	}

	@Override
	public String completionStyle() {
		return stringValue(ClangdMetadata.Predefined.completionStyle);
	}

	@Override
	public boolean prettyPrint() {
		return booleanValue(ClangdMetadata.Predefined.prettyPrint);
	}

	@Override
	public String queryDriver() {
		return stringValue(ClangdMetadata.Predefined.queryDriver);
	}

	@Override
	public List<String> additionalOptions() {
		var options = stringValue(ClangdMetadata.Predefined.additionalOptions);
		if (options.isBlank()) {
			return new ArrayList<>();
		}
		return Arrays.asList(options.split("\\R")); //$NON-NLS-1$
	}

	@Override
	public boolean logToConsole() {
		return booleanValue(ClangdMetadata.Predefined.logToConsole);
	}

	@Override
	public boolean validateClangdOptions() {
		return booleanValue(ClangdMetadata.Predefined.validateClangdOptions);
	}

	@Override
	public boolean fillFunctionArguments() {
		return booleanValue(ClangdMetadata.Predefined.fillFunctionArguments);
	}

}
