/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.test.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.lsp.util.LspUtils;
import org.junit.jupiter.api.Test;

class LspUtilsTest {

	@Test
	void testIsCContentType_EmptyId() {
		assertTrue(!LspUtils.isCContentType(""));
	}

	@Test
	void testIsCContentType_CONTENT_TYPE_CSOURCE() {
		assertTrue(LspUtils.isCContentType(CCorePlugin.CONTENT_TYPE_CSOURCE));
	}

	@Test
	void testIsCContentType_CONTENT_TYPE_CHEADER() {
		assertTrue(LspUtils.isCContentType(CCorePlugin.CONTENT_TYPE_CHEADER));
	}

	@Test
	void testIsCContentType_CONTENT_TYPE_CXXSOURCE() {
		assertTrue(LspUtils.isCContentType(CCorePlugin.CONTENT_TYPE_CXXSOURCE));
	}

	@Test
	void testIsCContentType_CONTENT_TYPE_CXXHEADER() {
		assertTrue(LspUtils.isCContentType(CCorePlugin.CONTENT_TYPE_CXXHEADER));
	}

}
