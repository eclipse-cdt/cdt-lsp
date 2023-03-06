package org.eclipse.cdt.lsp.editor.ui.test.preference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.editor.ui.test.TestUtils;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;


public class LspEditorPreferencesTesterTest {
	private static final String FILE_CONTENT = "// sample file content";
	private static final String MAIN_CPP = "main.cpp";
	private static final String HEADER_HPP = "header.hpp";
	private static final String MAIN_C = "main.c";
	private static final String HEADER_H = "header.h";
	//private static final String EXTERNAL_HEADER_HPP = "ExternalHeader.hpp";
	private IProject project;
	
	// @TempDir -> does not work with org.junit.jupiter.api. Needs junit-jupiter-api and junit-jupiter-params.
	// These packages are not accessible on the CI build server because we build with Eclipse 2022-06
	// Path tempDir = Files.createTempFile("ExternalHeader", ".hpp", null);
	
	private File createTempHppHeaderfile() throws IOException {
		return Files.createTempFile("ExternalHeader", ".hpp").toFile();
	}
	
	@BeforeEach
	public void setUp(TestInfo testInfo) throws CoreException {
		project = TestUtils.createCProject(getName(testInfo));
	}
	
	private String getName(TestInfo testInfo) {
		String displayName = testInfo.getDisplayName();
		String replaceFirst = displayName.replaceFirst("\\(.*\\)", "");
		return replaceFirst;
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
		//THEN the LspEditorPreferencesTester.test returns TRUE for the given project file URI:
		assertTrue(cLanguageServerProvider.isEnabledFor(project.getFile(fileName).getLocationURI()));		
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
		//THEN the LspEditorPreferencesTester.test returns FALSE for the given project file URI:
		assertTrue(!cLanguageServerProvider.isEnabledFor(project.getFile(fileName).getLocationURI()));		
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
	
	/**
	 * Tests whether LS enable returns false for an external file which is not opened.
	 * @throws IOException 
	 */
	@Test
	public void testLsEnableByExternalUriTest_NoEditorOpen() throws CoreException, IOException {
		//GIVEN is an external file which does not exists in the given project and is not opened:
		File externalFile = createTempHppHeaderfile();
		//AND a ICLanguageServerProvider which uses LspEditorPreferencesTester as enabledWhen tester:
		ICLanguageServerProvider cLanguageServerProvider = LspPlugin.getDefault().getCLanguageServerProvider();
		//WHEN the LspEditorPreferencesTester gets called by the property tester in the enabledWhen element of the serverProvider extension point,
		//THEN the LspEditorPreferencesTester.test returns FALSE for the given file URI:
		assertTrue(!cLanguageServerProvider.isEnabledFor(externalFile.toURI()));		
		//ensure clean up
		externalFile.delete();
	}
	
	/**
	 * Tests whether LS enable returns true for an external file which is opened in the C/C++ Editor (LSP).
	 */	
	@Test
	public void testLsEnableByExternalUriTest_OpenedInLspCEditor() throws CoreException, IOException {
		//GIVEN is an existing external file:
		File externalFile = createTempHppHeaderfile();
		externalFile.createNewFile();
		//AND it's opened in the LSP based C/C++ Editor:
		var editor = TestUtils.openInEditor(externalFile.toURI(), LspPlugin.LSP_C_EDITOR_ID);
		//AND a ICLanguageServerProvider which uses LspEditorPreferencesTester as enabledWhen tester:
		ICLanguageServerProvider cLanguageServerProvider = LspPlugin.getDefault().getCLanguageServerProvider();
		//WHEN the LspEditorPreferencesTester gets called by the property tester in the enabledWhen element of the serverProvider extension point,
		//THEN the LspEditorPreferencesTester.test returns TRUE for the given file URI:
		assertTrue(cLanguageServerProvider.isEnabledFor(externalFile.toURI()));	
		TestUtils.closeEditor(editor, false);
		//ensure clean up
		externalFile.delete();
	}
	
	/**
	 * Tests whether LS enable returns false for an external file which is opened in the C/C++ Editor.
	 */	
	@Test
	public void testLsEnableByExternalUriTest_OpenedInCEditor() throws CoreException, IOException {
		//GIVEN is an existing external file:
		File externalFile = createTempHppHeaderfile();
		externalFile.createNewFile();
		//AND it's opened in the C/C++ Editor:
		var editor = TestUtils.openInEditor(externalFile.toURI(), LspPlugin.C_EDITOR_ID);
		//AND a ICLanguageServerProvider which uses LspEditorPreferencesTester as enabledWhen tester:
		ICLanguageServerProvider cLanguageServerProvider = LspPlugin.getDefault().getCLanguageServerProvider();
		//WHEN the LspEditorPreferencesTester gets called by the property tester in the enabledWhen element of the serverProvider extension point,
		//THEN the LspEditorPreferencesTester.test returns FALSE for the given file URI:
		assertTrue(!cLanguageServerProvider.isEnabledFor(externalFile.toURI()));	
		TestUtils.closeEditor(editor, false);
		//ensure clean up
		externalFile.delete();
	}

}
