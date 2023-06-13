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
import java.util.List;
import java.util.Objects;

import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.eclipse.osgi.util.NLS;

final class ClangdPreferredOptions implements ClangdOptions {

	private final IPreferencesService service;
	private final String qualifier;
	private final IScopeContext[] scopes;
	private final ClangdMetadata metadata;

	ClangdPreferredOptions(IPreferencesService service, String qualifier, IScopeContext[] scopes,
			ClangdMetadata metadata) {
		this.service = Objects.requireNonNull(service);
		this.qualifier = Objects.requireNonNull(qualifier);
		this.scopes = Objects.requireNonNull(scopes);
		this.metadata = Objects.requireNonNull(metadata);
	}

	@Override
	public boolean preferClangd() {
		PreferenceMetadata<Boolean> pref = metadata.preferClangd();
		return service.getBoolean(qualifier, pref.identifer(), pref.defaultValue(), scopes);
	}

	@Override
	public String clangdPath() {
		PreferenceMetadata<String> pref = metadata.clangdPath();
		return service.getString(qualifier, pref.identifer(), pref.defaultValue(), scopes);
	}

	@Override
	public boolean useTidy() {
		PreferenceMetadata<Boolean> pref = metadata.useTidy();
		return service.getBoolean(qualifier, pref.identifer(), pref.defaultValue(), scopes);
	}

	@Override
	public boolean useBackgroundIndex() {
		PreferenceMetadata<Boolean> pref = metadata.useBackgroundIndex();
		return service.getBoolean(qualifier, pref.identifer(), pref.defaultValue(), scopes);
	}

	@Override
	public String completionStyle() {
		PreferenceMetadata<String> pref = metadata.completionStyle();
		return service.getString(qualifier, pref.identifer(), pref.defaultValue(), scopes);
	}

	@Override
	public boolean prettyPrint() {
		PreferenceMetadata<Boolean> pref = metadata.prettyPrint();
		return service.getBoolean(qualifier, pref.identifer(), pref.defaultValue(), scopes);
	}

	@Override
	public String queryDriver() {
		PreferenceMetadata<String> pref = metadata.queryDriver();
		return service.getString(qualifier, pref.identifer(), pref.defaultValue(), scopes);
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
		return list;
	}

}
