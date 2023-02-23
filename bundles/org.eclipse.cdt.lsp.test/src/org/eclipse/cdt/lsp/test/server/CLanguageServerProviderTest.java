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

package org.eclipse.cdt.lsp.test.server;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.Test;


public class CLanguageServerProviderTest {
	
	/**
	 * Tests whether the ICLanguageServerProvider with the largest priority is used.
	 */
	@Test
	public void testLsProvidersPriority() throws CoreException {
		//WHEN a language server provider gets created from the serverProvider extension point:
		ICLanguageServerProvider cLanguageServerProvider = LspPlugin.getDefault().getCLanguageServerProvider();
		//THEN its the mocked one, because its priority is larger than the CdtLanguageServerProvider's priority
		assertTrue(cLanguageServerProvider instanceof MockCLanguageServerProvider);		
	}
}
