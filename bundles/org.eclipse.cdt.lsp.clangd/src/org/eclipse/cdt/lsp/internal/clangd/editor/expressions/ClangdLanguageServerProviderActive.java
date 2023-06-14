/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.clangd.editor.expressions;

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.internal.clangd.ClangdLanguageServerProvider;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.core.expressions.PropertyTester;

public final class ClangdLanguageServerProviderActive extends PropertyTester {
	private final ICLanguageServerProvider provider;

	public ClangdLanguageServerProviderActive() {
		this.provider = LspPlugin.getDefault().getCLanguageServerProvider();
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		return provider instanceof ClangdLanguageServerProvider;
	}

}
