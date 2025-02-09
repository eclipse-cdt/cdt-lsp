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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.lsp.internal.server.CLanguageServerEnableCache;
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

class CLanguageServerEnableCacheTest {
	private static final String EXTERNAL_HEADER_HPP = "ExternalHeader.hpp";
	private IProject project;
	private File externalFile;
	private IEditorPart editor;
	private IEditorPart editor2;
	private CLanguageServerEnableCache cache = CLanguageServerEnableCache.getInstance();

	@TempDir
	private static File TEMP_DIR;

	@BeforeEach
	public void setUp(TestInfo testInfo) throws CoreException, IOException {
		project = TestUtils.createCProject(TestUtils.getName(testInfo));
		externalFile = new File(TEMP_DIR, EXTERNAL_HEADER_HPP);
		externalFile.createNewFile();
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
		if (editor2 != null) {
			TestUtils.closeEditor(editor2, false);
		}
	}

	@Test
	public void testCacheRemovedAfterFileGetsClosed() throws CoreException, IOException {
		// GIVEN is an opened file in the LSP based C/C++ editor:
		var uri = externalFile.toURI();
		editor = TestUtils.openInEditor(uri, LspPlugin.LSP_C_EDITOR_ID);
		// WHEN accessing the cached enable value:
		var cachedUri = cache.get(uri);
		// THEN the cache for the given URI is present:
		assertTrue(cachedUri != null);
		// AND the cache returns TRUE for the given file URI:
		assertTrue(cachedUri.booleanValue());
		// WHEN when the file gets closed:
		TestUtils.closeEditor(editor, false);
		// THEN the cached URI has been removed:
		assertNull(cache.get(uri));
	}

	@Test
	public void testCacheNotRemovedUntilAllOpenedEditorsHasBeenClosed() throws CoreException, IOException {
		// GIVEN two opened files in the LSP based C/C++ editor in two workbench windows:
		var uri = externalFile.toURI();
		editor = TestUtils.openInEditor(uri, LspPlugin.LSP_C_EDITOR_ID);
		editor2 = TestUtils.openInEditorInNewWindow(uri, LspPlugin.LSP_C_EDITOR_ID);
		// WHEN accessing the cached enable value:
		var cachedUri = cache.get(uri);
		// THEN the cache for the given URI is present:
		assertTrue(cachedUri != null);
		// AND the cache returns TRUE for the given file URI:
		assertTrue(cachedUri.booleanValue());
		// WHEN when the file gets closed in the first window:
		TestUtils.closeEditor(editor, false);
		// THEN the cached URI has NOT been removed:
		assertNotNull(cache.get(uri));
		assertTrue(cache.get(uri).booleanValue());
		// WHEN the file gets closed in the second window:
		TestUtils.closeEditor(editor2, false);
		// THEN the cached URI has been removed:
		assertNull(cache.get(uri));
	}

}
