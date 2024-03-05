/*******************************************************************************
 * Copyright (c) 2024 Bachmann electronic GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.clangd.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.eclipse.cdt.lsp.clangd.ClangdConfigurationFileManager;
import org.eclipse.cdt.lsp.clangd.internal.config.ClangdConfigFileChecker;
import org.eclipse.cdt.lsp.clangd.internal.config.ClangdConfigFileMonitor;
import org.eclipse.cdt.lsp.clangd.tests.TestUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

class ClangdConfigFileCheckerTest {
	private static final String ERROR_MSG = ".clangd problem marker has not been added to the config file!";
	private static final String VALID_YAML_SYNTAX = "\r\nCompileFlags: {CompilationDatabase: Release}\r\n";
	private static final String INVALID_YAML_SYNTAX_TAB_ONLY = "\t";
	private static final String INVALID_YAML_SYNTAX_CONTAINS_TAB = "\r\nCompileFlags:\n\tCompilationDatabase: Release";
	private static final String INVALID_YAML_SYNTAX_MISSING_BRACE = "CompileFlags: {CompilationDatabase: Release\r\n";

	private IProject project;

	@BeforeEach
	public void setUp(TestInfo testInfo) throws CoreException {
		var projectName = TestUtils.getName(testInfo);
		project = TestUtils.createCProject(projectName);
	}

	@AfterEach
	public void cleanUp() throws CoreException {
		TestUtils.deleteProject(project);
	}

	private IFile createConfigFile(String content) throws UnsupportedEncodingException, IOException, CoreException {
		var file = project.getFile(ClangdConfigurationFileManager.CLANGD_CONFIG_FILE_NAME);
		try (final var data = new ByteArrayInputStream(content.getBytes(project.getDefaultCharset()))) {
			if (!file.exists()) {
				file.create(data, false, new NullProgressMonitor());
			} else {
				file.setContents(data, IResource.KEEP_HISTORY, new NullProgressMonitor());
			}
		}
		return file;
	}

	/**
	 * Test valid yaml syntax. No marker should be added.
	 *
	 * @throws IOException
	 * @throws CoreException
	 */
	@Test
	void testValidYamlSyntax() throws IOException, CoreException {
		// GIVEN an existing .clangd configuration file with valid yaml syntax:
		var configFile = createConfigFile(VALID_YAML_SYNTAX);
		// WHEN the ClangdConfigFileChecker().checkConfigFile get called on the configFile:
		new ClangdConfigFileChecker().checkConfigFile(configFile);
		// THEN we expect that NO ClangdConfigFileChecker.CLANGD_MARKER has been added:
		var marker = configFile.findMarkers(ClangdConfigFileChecker.CLANGD_MARKER, false, IResource.DEPTH_ZERO);
		assertNotNull(marker);
		assertEquals(0, marker.length, "Expected no marker for valid yaml syntax");
	}

	/**
	 * Test whether a .clangd yaml Problem marker will be added to the .clangd file if the file contains invalid yaml syntax (here: tab only)
	 *
	 * @throws IOException
	 * @throws CoreException
	 */
	@Test
	void testInvalidYamlSyntaxTabOnly() throws IOException, CoreException {
		// GIVEN an existing .clangd configuration file with invalid yaml syntax (contains tab only):
		var configFile = createConfigFile(INVALID_YAML_SYNTAX_TAB_ONLY);
		// WHEN the ClangdConfigFileChecker().checkConfigFile get called on the configFile:
		new ClangdConfigFileChecker().checkConfigFile(configFile);
		// THEN we expect that an ClangdConfigFileChecker.CLANGD_MARKER has been added:
		var marker = configFile.findMarkers(ClangdConfigFileChecker.CLANGD_MARKER, false, IResource.DEPTH_ZERO);
		assertNotNull(marker);
		assertEquals(1, marker.length, ERROR_MSG);
	}

	/**
	 * Test whether a .clangd yaml Problem marker will be added to the .clangd file if the file contains invalid yaml syntax (here: tab)
	 *
	 * @throws IOException
	 * @throws CoreException
	 */
	@Test
	void testInvalidYamlSyntaxContainsTab() throws IOException, CoreException {
		// GIVEN an existing .clangd configuration file with invalid yaml syntax (contains tab):
		var configFile = createConfigFile(INVALID_YAML_SYNTAX_CONTAINS_TAB);
		// WHEN the ClangdConfigFileChecker().checkConfigFile get called on the configFile:
		new ClangdConfigFileChecker().checkConfigFile(configFile);
		// THEN we expect that an ClangdConfigFileChecker.CLANGD_MARKER has been added:
		var marker = configFile.findMarkers(ClangdConfigFileChecker.CLANGD_MARKER, false, IResource.DEPTH_ZERO);
		assertNotNull(marker);
		assertEquals(1, marker.length, ERROR_MSG);
	}

	/**
	 * Test whether a .clangd yaml Problem marker will be added to the .clangd file if the file contains invalid yaml syntax (here: missing closing brace)
	 * because the {@link ClangdConfigFileMonitor#checkJob} should have been run after a delay of 100ms.
	 *
	 * @throws IOException
	 * @throws CoreException
	 * @throws InterruptedException
	 */
	@Test
	void testInvalidYamlSyntaxMissingBrace() throws IOException, CoreException, InterruptedException {
		// GIVEN an existing .clangd configuration file with invalid yaml syntax (missing closing brace):
		// WHEN a new .clang file gets added to the project (should trigger ClangdConfigFileMonitor.checkJob).
		var configFile = createConfigFile(INVALID_YAML_SYNTAX_MISSING_BRACE);
		int timeoutCnt = 0;
		var marker = new IMarker[] {};
		do {
			Thread.sleep(50);
			marker = configFile.findMarkers(ClangdConfigFileChecker.CLANGD_MARKER, false, IResource.DEPTH_ZERO);
			timeoutCnt++;
		} while (marker.length == 0 && timeoutCnt < 20);
		// THEN we expect that an ClangdConfigFileChecker.CLANGD_MARKER has been added in the meantime,
		// because the ClangdConfigFileMonitor.checkJob, which calls the ClangdConfigFileChecker().checkConfigFile, should have been run after a delay of 100ms:
		assertEquals(1, marker.length, ERROR_MSG);
	}

}
