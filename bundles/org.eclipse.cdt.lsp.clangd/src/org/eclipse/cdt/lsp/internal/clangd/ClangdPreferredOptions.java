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
package org.eclipse.cdt.lsp.internal.clangd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.eclipse.osgi.util.NLS;

final class ClangdPreferredOptions implements ClangdOptions {

	private final String qualifier;
	private final IScopeContext[] scopes;
	private final ClangdMetadata metadata;

	ClangdPreferredOptions(String qualifier, IScopeContext[] scopes, ClangdMetadata metadata) {
		this.qualifier = Objects.requireNonNull(qualifier);
		this.scopes = Objects.requireNonNull(scopes);
		this.metadata = Objects.requireNonNull(metadata);
	}

	@Override
	public boolean preferClangd() {
		return booleanValue(metadata.preferClangd());
	}

	@Override
	public String clangdPath() {
		return stringValue(metadata.clangdPath());
	}

	@Override
	public boolean useTidy() {
		return booleanValue(metadata.useTidy());
	}

	@Override
	public boolean useBackgroundIndex() {
		return booleanValue(metadata.useBackgroundIndex());
	}

	@Override
	public String completionStyle() {
		return stringValue(metadata.completionStyle());
	}

	@Override
	public boolean prettyPrint() {
		return booleanValue(metadata.prettyPrint());
	}

	@Override
	public String queryDriver() {
		return stringValue(metadata.queryDriver());
	}

	@Override
	public List<String> customOptions() {
		return Arrays.asList(stringValue(metadata.customOptions()).split("\\R")); //$NON-NLS-1$
	}

	private String stringValue(PreferenceMetadata<?> meta) {
		String actual = String.valueOf(meta.defaultValue());
		for (int i = scopes.length - 1; i >= 0; i--) {
			IScopeContext scope = scopes[i];
			String previous = actual;
			actual = scope.getNode(qualifier).get(meta.identifer(), previous);
		}
		return actual;
	}

	private boolean booleanValue(PreferenceMetadata<Boolean> meta) {
		return Optional.of(meta)//
				.map(this::stringValue)//
				.map(Boolean::valueOf)//
				.orElseGet(meta::defaultValue);
	}

	@Override
	public List<String> toList() {
		List<String> list = new ArrayList<>();
		list.add(clangdPath());
		if (useTidy()) {
			list.add("--clang-tidy"); //$NON-NLS-1$
		}
		if (useBackgroundIndex()) {
			list.add("--background-index"); //$NON-NLS-1$
		}
		list.add(NLS.bind("--completion-style={0}", completionStyle())); //$NON-NLS-1$
		if (prettyPrint()) {
			list.add("--pretty"); //$NON-NLS-1$
		}
		list.add(NLS.bind("--query-driver={0}", queryDriver())); //$NON-NLS-1$

		list.addAll(customOptions());
		return list;
	}

}
