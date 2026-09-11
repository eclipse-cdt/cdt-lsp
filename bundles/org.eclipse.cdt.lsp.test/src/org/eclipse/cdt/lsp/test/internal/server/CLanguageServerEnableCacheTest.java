/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.test.internal.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.cdt.lsp.internal.server.CLanguageServerEnableCache;
import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.cdt.lsp.test.TestUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

class CLanguageServerEnableCacheTest {
	private static final String HEADER_HPP = "MyProjectHeader.h";
	private static final String EXTERNAL_HEADER_HPP = "ExternalHeader.hpp";
	private static final String NO_C_FILE = "MyFile.hdr";
	private IProject project;
	private File externalHeaderFile;
	private File externalNoCFile;
	private IFile projectHeaderFile;
	private IFile projectNoCFile;
	private IEditorPart editor;
	private IEditorPart editor2;
	private CLanguageServerEnableCache cache = CLanguageServerEnableCache.getInstance();

	@TempDir
	private static File TEMP_DIR;

	@BeforeEach
	public void setUp(TestInfo testInfo) throws CoreException, IOException {
		project = TestUtils.createCProject(TestUtils.getName(testInfo));
		projectHeaderFile = TestUtils.createFile(project, HEADER_HPP, "");
		projectNoCFile = TestUtils.createFile(project, NO_C_FILE, "");
		externalHeaderFile = new File(TEMP_DIR, EXTERNAL_HEADER_HPP);
		externalHeaderFile.createNewFile();
		externalNoCFile = new File(TEMP_DIR, NO_C_FILE);
		externalNoCFile.createNewFile();
	}

	@AfterEach
	public void cleanUp() throws CoreException {
		if (editor != null) {
			TestUtils.closeEditor(editor, false);
		}
		if (editor2 != null) {
			TestUtils.closeEditor(editor2, false);
		}

		TestUtils.deleteProject(project);
		if (externalHeaderFile != null) {
			externalHeaderFile.delete();
		}
		if (externalNoCFile != null) {
			externalNoCFile.delete();
		}
	}

	private void testC_File_URIopenedInEditor(URI uri) throws CoreException, IOException {
		// GIVEN is an opened header file in the LSP based C/C++ editor:
		editor = TestUtils.openInEditor(uri, LspPlugin.LSP_C_EDITOR_ID);
		// WHEN accessing the cached enable value:
		var cachedUri = cache.get(uri);
		// THEN the cache for the given URI is present:
		assertTrue(cachedUri.isPresent());
		// AND the cache returns TRUE for the given file URI:
		assertTrue(cachedUri.get().booleanValue());
		// WHEN when the file gets closed:
		TestUtils.closeEditor(editor, false);
		// THEN the cached URI has been removed:
		assertFalse(cache.get(uri).isPresent());
	}

	private void testC_File_URIopenedIn2EditorsIn2Windows(URI uri) throws CoreException, IOException {
		// GIVEN two opened header files in the LSP based C/C++ editor in two workbench windows:
		editor = TestUtils.openInEditor(uri, LspPlugin.LSP_C_EDITOR_ID);
		editor2 = TestUtils.openInEditorInNewWindow(uri, LspPlugin.LSP_C_EDITOR_ID);
		// WHEN accessing the cached enable value:
		var cachedUri = cache.get(uri);
		// THEN the cache for the given URI is present:
		assertTrue(cachedUri.isPresent());
		// AND the cache returns TRUE for the given file URI:
		assertTrue(cachedUri.get().booleanValue());
		// WHEN when the file gets closed in the first window:
		TestUtils.closeEditor(editor, false);
		// THEN the cached URI has NOT been removed:
		assertTrue(cache.get(uri).isPresent());
		assertTrue(cache.get(uri).get().booleanValue());
		// WHEN the file gets closed in the second window:
		TestUtils.closeEditor(editor2, false);
		// THEN the cached URI has been removed:
		assertFalse(cache.get(uri).isPresent());
	}

	private void test_File_URIopenedInEditor(URI uri) throws CoreException, IOException {
		// GIVEN is an opened file in the LSP based C/C++ editor:
		editor = TestUtils.openInEditor(uri, LspPlugin.LSP_C_EDITOR_ID);
		// WHEN accessing the cached enable value:
		var cachedUri = cache.get(uri);
		// THEN the cache for the given URI is present:
		assertTrue(cachedUri.isPresent());
		// AND the cache returns FALSE for the given file URI:
		assertFalse(cachedUri.get().booleanValue());
		// WHEN when the file gets closed:
		TestUtils.closeEditor(editor, false);
		// THEN the cached URI has NOT been removed:
		assertTrue(cache.get(uri).isPresent());
	}

	@Test
	@DisplayName("Cached header file URI shall be removed after file gets closed in editor")
	public void testCache1() throws CoreException, IOException {
		testC_File_URIopenedInEditor(projectHeaderFile.getFullPath().toFile().toURI());
	}

	@Test
	@DisplayName("Cached header file URI shall be removed after ALL opened editors gets closed")
	public void testCache2() throws CoreException, IOException {
		testC_File_URIopenedIn2EditorsIn2Windows(projectHeaderFile.getFullPath().toFile().toURI());
	}

	@Test
	@DisplayName("Cached EXTERNAL header file URI shall be removed after file gets closed in editor")
	public void testCache3() throws CoreException, IOException {
		testC_File_URIopenedInEditor(externalHeaderFile.toURI());
	}

	@Test
	@DisplayName("Cached EXTERNAL header file URI shall be removed after ALL opened editors gets closed")
	public void testCache4() throws CoreException, IOException {
		testC_File_URIopenedIn2EditorsIn2Windows(externalHeaderFile.toURI());
	}

	@Test
	@DisplayName("Cached non C file URI shall return false and should not be removed after closing")
	public void testCache5() throws CoreException, IOException {
		test_File_URIopenedInEditor(projectNoCFile.getFullPath().toFile().toURI());
	}

	@Test
	@DisplayName("Cached EXTERNAL non C file URI shall return false and should not be removed after closing")
	public void testCache6() throws CoreException, IOException {
		test_File_URIopenedInEditor(externalNoCFile.toURI());
	}

}
