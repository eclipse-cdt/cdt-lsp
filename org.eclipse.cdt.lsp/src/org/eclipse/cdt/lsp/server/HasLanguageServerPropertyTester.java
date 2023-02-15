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

package org.eclipse.cdt.lsp.server;

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.core.expressions.PropertyTester;

public class HasLanguageServerPropertyTester extends PropertyTester {
	private final ICLanguageServerProvider cLanguageServerProvider;

	public HasLanguageServerPropertyTester() {
		cLanguageServerProvider = LspPlugin.getDefault().getCLanguageServerProvider();
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (cLanguageServerProvider != null) {
			return cLanguageServerProvider.isEnabled(receiver);
		}
		return true;
	}

}
