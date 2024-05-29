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

package org.eclipse.cdt.lsp.test.internal.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.lsp.internal.server.HasLanguageServerPropertyTester;
import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.cdt.lsp.test.TestUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

class HasLanguageServerPropertyTesterTest {
	private static final String EXTERNAL_HEADER_HPP = "ExternalHeader.hpp";
	private IProject project;
	private File externalFile;
	private IEditorPart editor;

	@TempDir
	private static File TEMP_DIR;

	@BeforeEach
	public void setUp(TestInfo testInfo) throws CoreException {
		project = TestUtils.createCProject(TestUtils.getName(testInfo));
	}

	@AfterEach
	public void cleanUp() throws CoreException {
		TestUtils.deleteProject(project);
		if (externalFile != null) {
			externalFile.delete();
		}
		if (editor != null) {
			TestUtils.closeEditor(editor, false);
		}
	}

	/**
	 * Tests whether LS enable test returns false for an external file when no (LSP) editor is opened.
	 * @throws IOException
	 */
	@Test
	public void testLsNotEnabledForExternalFile_NoEditorOpen() throws CoreException, IOException {
		//GIVEN is an external file which is not opened:
		externalFile = new File(TEMP_DIR, EXTERNAL_HEADER_HPP);
		//WHEN the file is not opened,
		//THEN the hasLanguageServerPropertyTester.test returns FALSE for the given file URI:
		assertFalse(new HasLanguageServerPropertyTester().test(externalFile.toURI(), null, null, null));
	}

	/**
	 * Tests whether LS enable test returns false for an external file which is opened in the C/C++ Editor.
	 */
	@Test
	public void testLsNotEnabledForExternalFile_OpenedInCEditor() throws CoreException, IOException {
		//GIVEN is an existing external file:
		externalFile = new File(TEMP_DIR, EXTERNAL_HEADER_HPP);
		externalFile.createNewFile();
		//WHEN it's opened in the C/C++ Editor:
		editor = TestUtils.openInEditor(externalFile.toURI(), LspPlugin.C_EDITOR_ID);
		//THEN the hasLanguageServerPropertyTester.test returns FALSE for the given file URI:
		assertFalse(new HasLanguageServerPropertyTester().test(externalFile.toURI(), null, null, null));
	}

	/**
	 * Tests whether LS enable test returns true for an external file which is opened in the C/C++ Editor (LSP).
	 */
	@Test
	public void testLsEnableForExternalFile_OpenedInLspCEditor() throws CoreException, IOException {
		//GIVEN is an existing external file:
		externalFile = new File(TEMP_DIR, EXTERNAL_HEADER_HPP);
		externalFile.createNewFile();
		//WHEN it's opened in the LSP based C/C++ Editor:
		editor = TestUtils.openInEditor(externalFile.toURI(), LspPlugin.LSP_C_EDITOR_ID);
		//THEN the hasLanguageServerPropertyTester.test returns TRUE for the given file URI:
		assertTrue(new HasLanguageServerPropertyTester().test(externalFile.toURI(), null, null, null));
	}

}
