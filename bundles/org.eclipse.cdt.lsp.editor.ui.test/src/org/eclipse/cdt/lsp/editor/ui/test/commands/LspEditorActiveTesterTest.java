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
 * Dominic Scharfe (COSEDA Technologies GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.editor.ui.test.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.editor.ui.commands.LspEditorActiveTester;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.texteditor.ITextEditor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LspEditorActiveTesterTest {

	LspEditorActiveTester tut;

	@Mock
	IEditorPart editorPart;

	@Mock
	IEditorSite editorSite;

	@BeforeEach
	public void setup() {
		tut = new LspEditorActiveTester();

		when(editorPart.getSite()).thenReturn(editorSite);
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
		assertFalse(tut.test(editorPart, "someOtherProperty", null, tut));
	}

	/**
	 * Assert that an {@link IEditorPart} with some other id is not detected as c editor.
	 */
	@Test
	public void matchingEditorPartWithNonMatchingId() {
		when(editorSite.getId()).thenReturn("someId");
		assertTestResult(false);
	}

	/**
	 * Assert that an {@link IEditorPart} with the c editor id is detected as c editor.
	 */
	@Test
	public void matchingEditorPartWithMatchingId() {
		when(editorSite.getId()).thenReturn(LspPlugin.LSP_C_EDITOR_ID);
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
		when(editorPart.getAdapter(ITextEditor.class)).thenReturn(innerEditor);
		when(editorSite.getId()).thenReturn("someOtherId");

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
		when(editorPart.getAdapter(ITextEditor.class)).thenReturn(innerEditor);
		when(editorSite.getId()).thenReturn("someOtherId");

		assertTestResult(true);
	}

	/**
	 * Helper method.
	 */
	private void assertTestResult(boolean expectedResult) {
		assertEquals(expectedResult, tut.test(editorPart, LspEditorActiveTester.IS_LSP_CEDITOR_PROPERTY, null, tut));
	}

}
