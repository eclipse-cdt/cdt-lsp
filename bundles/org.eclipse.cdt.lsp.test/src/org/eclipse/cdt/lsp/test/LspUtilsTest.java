package org.eclipse.cdt.lsp.test;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.lsp.LspUtils;
import org.junit.jupiter.api.Test;

class LspUtilsTest {

	@Test
	void testIsCContentType_EmptyId() {
		assertTrue(!LspUtils.isCContentType(""));
	}
	
	@Test
	void testIsCContentType_CppContentTypeFromTM4E() {
		assertTrue(LspUtils.isCContentType("lng.cpp"));
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
