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

package org.eclipse.cdt.lsp.editor.ui.test.preference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.editor.ui.test.TestUtils;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.cdt.lsp.server.enable.HasLanguageServerPropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

public class LspEditorPreferencesTesterTest {
	private static final String FILE_CONTENT = "// sample file content";
	private static final String MAIN_CPP = "main.cpp";
	private static final String HEADER_HPP = "header.hpp";
	private static final String MAIN_C = "main.c";
	private static final String HEADER_H = "header.h";
	private static final String EXTERNAL_HEADER_HPP = "ExternalHeader.hpp";
	private IProject project;
	
	@TempDir
	private static File TEMP_DIR;
	
	@BeforeEach
	public void setUp(TestInfo testInfo) throws CoreException {
		project = TestUtils.createCProject(TestUtils.getName(testInfo));
	}
	
	@AfterEach
	public void cleanUp() throws CoreException {
		TestUtils.deleteProject(project);
	}
	
	/**
	 * Tests whether the LS enable returns true on a resource whose project has "Prefer C/C++ Editor (LSP)" enabled.
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void testLsEnableByUriTest_WITH_LsEditorPreferred() throws CoreException, UnsupportedEncodingException {
		final String fileName = "main.cpp";
		//GIVEN is a project with ENABLED "Prefer C/C++ Editor (LSP)" in the preferences:
		TestUtils.setLspPreferred(project, true);
		//AND a file exits in the given project:
		TestUtils.createFile(project, fileName, FILE_CONTENT);
		//AND a ICLanguageServerProvider which uses LspEditorPreferencesTester as enabledWhen tester:
		ICLanguageServerProvider cLanguageServerProvider = LspPlugin.getDefault().getCLanguageServerProvider();
		//WHEN the LspEditorPreferencesTester gets called by the property tester in the enabledWhen element of the serverProvider extension point,
		//THEN the LspEditorPreferencesTester.test returns TRUE for the given project:
		assertTrue(cLanguageServerProvider.isEnabledFor(project));		
	}
	
	/**
	 * Tests whether LS enable returns false on a resource whose project has "Prefer C/C++ Editor (LSP)" disabled.
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void testLsEnableByUriTest_WITHOUT_LsEditorPreferred() throws CoreException, UnsupportedEncodingException {
		final String fileName = "main.cpp";
		//GIVEN is a project with DISABLED "Prefer C/C++ Editor (LSP)" in the preferences:
		TestUtils.setLspPreferred(project, false);
		//AND a file exits in the given project:
		TestUtils.createFile(project, fileName, FILE_CONTENT);
		//AND a ICLanguageServerProvider which uses LspEditorPreferencesTester as enabledWhen tester:
		ICLanguageServerProvider cLanguageServerProvider = LspPlugin.getDefault().getCLanguageServerProvider();
		//WHEN the LspEditorPreferencesTester gets called by the property tester in the enabledWhen element of the serverProvider extension point,
		//THEN the LspEditorPreferencesTester.test returns FALSE for the given project:
		assertTrue(!cLanguageServerProvider.isEnabledFor(project));		
	}
	
	/**
	 * Tests whether the C/C++ Editor is used for a C++ source file to open whose project has "Prefer C/C++ Editor (LSP)" disabled.
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void testEditorUsedToOpenCppFile_WITHOUT_LsEditorPreferred() throws CoreException, UnsupportedEncodingException {
		//GIVEN is a project with DISABLED "Prefer C/C++ Editor (LSP)" in the preferences:
		TestUtils.setLspPreferred(project, false);
		//AND a file exits in the given project:
		var file = TestUtils.createFile(project, MAIN_CPP, FILE_CONTENT);
		//WHEN this file will be opened:
		var editorPart = TestUtils.openInEditor(file);
		//THEN it will be opened in the C/C++ Editor:
		assertEquals(LspPlugin.C_EDITOR_ID, editorPart.getEditorSite().getId());
		TestUtils.closeEditor(editorPart, false);
	}
	
	/**
	 * Tests whether the C/C++ Editor (LSP) is used for a C++ source file to open whose project has "Prefer C/C++ Editor (LSP)" enabled.
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void testEditorUsedToOpenCppFile_WITH_LsEditorPreferred() throws CoreException, UnsupportedEncodingException {
		//GIVEN is a project with ENABLED "Prefer C/C++ Editor (LSP)" in the preferences:
		TestUtils.setLspPreferred(project, true);
		//AND a file exits in the given project:
		var file = TestUtils.createFile(project, MAIN_CPP, FILE_CONTENT);
		//WHEN this file will be opened:
		var editorPart = TestUtils.openInEditor(file);
		//THEN it will be opened in the C/C++ Editor (LSP):
		assertEquals(LspPlugin.LSP_C_EDITOR_ID, editorPart.getEditorSite().getId());
		TestUtils.closeEditor(editorPart, false);
	}
	
	/**
	 * Tests whether the C/C++ Editor is used for a C++ header file to open whose project has "Prefer C/C++ Editor (LSP)" disabled.
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void testEditorUsedToOpenCppHeaderFile_WITHOUT_LsEditorPreferred() throws CoreException, UnsupportedEncodingException {
		//GIVEN is a project with DISABLED "Prefer C/C++ Editor (LSP)" in the preferences:
		TestUtils.setLspPreferred(project, false);
		//AND a file exits in the given project:
		var file = TestUtils.createFile(project, HEADER_HPP, FILE_CONTENT);
		//WHEN this file will be opened:
		var editorPart = TestUtils.openInEditor(file);
		//THEN it will be opened in the C/C++ Editor:
		assertEquals(LspPlugin.C_EDITOR_ID, editorPart.getEditorSite().getId());
		TestUtils.closeEditor(editorPart, false);
	}
	
	/**
	 * Tests whether the C/C++ Editor (LSP) is used for a C++ header file to open whose project has "Prefer C/C++ Editor (LSP)" enabled.
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void testEditorUsedToOpenCppHeaderFile_WITH_LsEditorPreferred() throws CoreException, UnsupportedEncodingException {
		//GIVEN is a project with ENABLED "Prefer C/C++ Editor (LSP)" in the preferences:
		TestUtils.setLspPreferred(project, true);
		//AND a file exits in the given project:
		var file = TestUtils.createFile(project, HEADER_HPP, FILE_CONTENT);
		//WHEN this file will be opened:
		var editorPart = TestUtils.openInEditor(file);
		//THEN it will be opened in the C/C++ Editor (LSP):
		assertEquals(LspPlugin.LSP_C_EDITOR_ID, editorPart.getEditorSite().getId());
		TestUtils.closeEditor(editorPart, false);
	}
	
	/**
	 * Tests whether the C/C++ Editor is used for a C source file to open whose project has "Prefer C/C++ Editor (LSP)" disabled.
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void testEditorUsedToOpenCFile_WITHOUT_LsEditorPreferred() throws CoreException, UnsupportedEncodingException {
		//GIVEN is a project with DISABLED "Prefer C/C++ Editor (LSP)" in the preferences:
		TestUtils.setLspPreferred(project, false);
		//AND a file exits in the given project:
		var file = TestUtils.createFile(project, MAIN_C, FILE_CONTENT);
		//WHEN this file will be opened:
		var editorPart = TestUtils.openInEditor(file);
		//THEN it will be opened in the C/C++ Editor:
		assertEquals(LspPlugin.C_EDITOR_ID, editorPart.getEditorSite().getId());
		TestUtils.closeEditor(editorPart, false);
	}
	
	/**
	 * Tests whether the C/C++ Editor (LSP) is used for a C source file to open whose project has "Prefer C/C++ Editor (LSP)" enabled.
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void testEditorUsedToOpenCFile_WITH_LsEditorPreferred() throws CoreException, UnsupportedEncodingException {
		//GIVEN is a project with ENABLED "Prefer C/C++ Editor (LSP)" in the preferences:
		TestUtils.setLspPreferred(project, true);
		//AND a file exits in the given project:
		var file = TestUtils.createFile(project, MAIN_C, FILE_CONTENT);
		//WHEN this file will be opened:
		var editorPart = TestUtils.openInEditor(file);
		//THEN it will be opened in the C/C++ Editor (LSP):
		assertEquals(LspPlugin.LSP_C_EDITOR_ID, editorPart.getEditorSite().getId());
		TestUtils.closeEditor(editorPart, false);
	}
	
	/**
	 * Tests whether the C/C++ Editor is used for a C header file to open whose project has "Prefer C/C++ Editor (LSP)" disabled.
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void testEditorUsedToOpenCHeaderFile_WITHOUT_LsEditorPreferred() throws CoreException, UnsupportedEncodingException {
		//GIVEN is a project with DISABLED "Prefer C/C++ Editor (LSP)" in the preferences:
		TestUtils.setLspPreferred(project, false);
		//AND a file exits in the given project:
		var file = TestUtils.createFile(project, HEADER_HPP, FILE_CONTENT);
		//WHEN this file will be opened:
		var editorPart = TestUtils.openInEditor(file);
		//THEN it will be opened in the C/C++ Editor:
		assertEquals(LspPlugin.C_EDITOR_ID, editorPart.getEditorSite().getId());
		TestUtils.closeEditor(editorPart, false);
	}
	
	/**
	 * Tests whether the C/C++ Editor (LSP) is used for a C header file to open whose project has "Prefer C/C++ Editor (LSP)" enabled.
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void testEditorUsedToOpenCHeaderFile_WITH_LsEditorPreferred() throws CoreException, UnsupportedEncodingException {
		//GIVEN is a project with ENABLED "Prefer C/C++ Editor (LSP)" in the preferences:
		TestUtils.setLspPreferred(project, true);
		//AND a file exits in the given project:
		var file = TestUtils.createFile(project, HEADER_H, FILE_CONTENT);
		//WHEN this file will be opened:
		var editorPart = TestUtils.openInEditor(file);
		//THEN it will be opened in the C/C++ Editor (LSP):
		assertEquals(LspPlugin.LSP_C_EDITOR_ID, editorPart.getEditorSite().getId());
		TestUtils.closeEditor(editorPart, false);
	}
	
	//**********************************************************************************************************************************
	// The following test
	//**********************************************************************************************************************************
	
	/**
	 * Tests whether the LS is NOT enabled when switching from a tab with a project file with LS enabled 
	 * to a project file without LS enabled.
	 * @throws UnsupportedEncodingException 
	 */
	@Test
	public void testLsNotEnabledByTabSwitchFromLspToNonLspProjectFile() throws CoreException, UnsupportedEncodingException {
		//GIVEN is a project with ENABLED "Prefer C/C++ Editor (LSP)" in the preferences:
		TestUtils.setLspPreferred(project, true);
		//AND a file exists in the given project:
		var lspProjectFile = TestUtils.createFile(project, MAIN_C, FILE_CONTENT);
		//AND this file will be opened:
		var lspEditorPart = TestUtils.openInEditor(lspProjectFile);
		//AND a second project with DISABLED "Prefer C/C++ Editor (LSP)" in the preferences:
		var nonLspProject = TestUtils.createCProject("NonLspProject");
		TestUtils.setLspPreferred(nonLspProject, false);
		//WHEN a file exits in the given second project with DISABLED "Prefer C/C++ Editor (LSP)":
		var nonLspProjectFile = TestUtils.createFile(nonLspProject, MAIN_C, FILE_CONTENT);
		//THEN the LS won't be enabled for the non LSP project file:
		assertTrue(!new HasLanguageServerPropertyTester().test(nonLspProjectFile.getLocationURI(), null, null, null));
		//AND those file has been be opened:
		var nonLspEditorPart = TestUtils.openInEditor(nonLspProjectFile);
		//THEN the file from the project with enabled "Prefer C/C++ Editor (LSP)" has been opened in the C/C++ Editor (LSP):
		assertEquals(LspPlugin.LSP_C_EDITOR_ID, lspEditorPart.getEditorSite().getId());
		//THEN the file from the project with disabled "Prefer C/C++ Editor (LSP)" has been opened in the C/C++ Editor:
		assertEquals(LspPlugin.C_EDITOR_ID, nonLspEditorPart.getEditorSite().getId());
		
		//clean-up:
		TestUtils.closeEditor(lspEditorPart, false);
		TestUtils.closeEditor(nonLspEditorPart, false);
		TestUtils.deleteProject(nonLspProject);
	}
	
	/**
	 * Tests whether the LS is enabled for an external header file when a LSP based editor is in focus 
	 * and an external header file shall be opened. (hyperlink test)
	 * @throws UnsupportedEncodingException, IOException
	 */
	@Test
	public void testLsEnableByHyperlinkFromLspFileToExternalFile() throws CoreException, IOException {
		//GIVEN is a project with ENABLED "Prefer C/C++ Editor (LSP)" in the preferences:
		TestUtils.setLspPreferred(project, true);
		//AND a file exists in the given project:
		var lspProjectFile = TestUtils.createFile(project, MAIN_C, FILE_CONTENT);
		//AND this file has been opened:
		var lspEditorPart = TestUtils.openInEditor(lspProjectFile);
		//WHEN an external header file shall be opened in the C/C++ Editor:
		File externalFile = new File(TEMP_DIR, EXTERNAL_HEADER_HPP);
		externalFile.createNewFile();
		//THEN the LS will be enabled for the external file, because we assume that is opened via hyperlink from the opened main.c:
		assertTrue(new HasLanguageServerPropertyTester().test(externalFile.toURI(), null, null, null));
	
		//clean-up:
		TestUtils.closeEditor(lspEditorPart, false);
		externalFile.delete();
	}

}
