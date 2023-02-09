/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.server;

import org.eclipse.cdt.lsp.CLanguageServerRegistry;
import org.eclipse.core.expressions.PropertyTester;

public class HasLanguageServerPropertyTester extends PropertyTester {
	private final EnableExpression cEnableExpression;

	public HasLanguageServerPropertyTester() {
		cEnableExpression = new CLanguageServerRegistry().getEnablementExpression();
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (cEnableExpression != null) {
			return cEnableExpression.evaluate(receiver);
		}
		return true;
	}

}
