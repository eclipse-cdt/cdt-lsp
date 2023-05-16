/*******************************************************************************
 * Copyright (c) 2023 COSEDA Technologies GmbH and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Dominic Scharfe (COSEDA Technologies GmbH) - initial implementation
 *     Alexander Fedorov (ArSysOp) - extract headless part
 *******************************************************************************/
package org.eclipse.cdt.lsp.clangd.editor.tests.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.internal.clangd.editor.expressions.LspEditorActiveTester;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class LspEditorActiveTesterTest {

	LspEditorActiveTester tut;

	IEditorPart part;
	IEditorSite site;

	@BeforeEach
	public void setup() {
		tut = new LspEditorActiveTester();

		part = mock(IEditorPart.class);
		site = mock(IEditorSite.class);

		when(part.getSite()).thenReturn(site);
	}

	/**
	 * Assert that a receiver which is not an editor part is not detected as c
	 * editor.
	 */
	@Test
	public void nonEditorPart() {
		assertFalse(tut.test(new String("not an edit part"), LspEditorActiveTester.IS_LSP_CEDITOR_PROPERTY, null, tut));
	}

	/**
	 * Assert that tester only tests for the correct property, even if the given
	 * receiver is an {@link IEditorPart}.
	 */
	@Test
	public void editorPartNonMatchingProperty() {
		assertFalse(tut.test(part, "someOtherProperty", null, tut));
	}

	/**
	 * Assert that an {@link IEditorPart} with some other id is not detected as c editor.
	 */
	@Test
	public void matchingEditorPartWithNonMatchingId() {
		when(site.getId()).thenReturn("someId");
		assertTestResult(false);
	}

	/**
	 * Assert that an {@link IEditorPart} with the c editor id is detected as c editor.
	 */
	@Test
	public void matchingEditorPartWithMatchingId() {
		when(site.getId()).thenReturn(LspPlugin.LSP_C_EDITOR_ID);
		assertTestResult(true);
	}

	/**
	 * Assert that an {@link IEditorPart} with an "inner editor" (part adapts to
	 * {@link ITextEditor.class}) is not detected as c editor when the inner editor
	 * doesn't have the c editor id.
	 */
	@Test
	public void editorPartWithInnerNonMatchingEditor() {
		ITextEditor innerEditor = mock(ITextEditor.class);
		IEditorSite innerEditorSite = mock(IEditorSite.class);
		when(innerEditor.getSite()).thenReturn(innerEditorSite);
		when(innerEditorSite.getId()).thenReturn("someId");
		when(part.getAdapter(ITextEditor.class)).thenReturn(innerEditor);
		when(site.getId()).thenReturn("someOtherId");

		assertTestResult(false);
	}

	/**
	 * Assert that an {@link IEditorPart} with an "inner editor" (part adapts to
	 * {@link ITextEditor.class}) is detected as c editor when the inner editor
	 * has the c editor id.
	 */
	@Test
	public void editorPartWithInnerMatchingEditor() {
		ITextEditor innerEditor = mock(ITextEditor.class);
		IEditorSite innerEditorSite = mock(IEditorSite.class);
		when(innerEditor.getSite()).thenReturn(innerEditorSite);
		when(innerEditorSite.getId()).thenReturn(LspPlugin.LSP_C_EDITOR_ID);
		when(part.getAdapter(ITextEditor.class)).thenReturn(innerEditor);
		when(site.getId()).thenReturn("someOtherId");

		assertTestResult(true);
	}

	/**
	 * Helper method.
	 */
	private void assertTestResult(boolean expectedResult) {
		assertEquals(expectedResult, tut.test(part, LspEditorActiveTester.IS_LSP_CEDITOR_PROPERTY, null, tut));
	}

}
