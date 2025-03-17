/*******************************************************************************
 * Copyright (c) 2023, 2025 Bachmann electronic GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *     Alexander Fedorov (ArSysOp) - extract headless part
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Arrays;

import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICBuildSetting;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.internal.core.settings.model.CConfigurationDescriptionCache;
import org.eclipse.cdt.lsp.clangd.internal.config.ClangdCompilationDatabaseSetter;
import org.eclipse.cdt.lsp.clangd.internal.config.ClangdCompilationDatabaseSetterBase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

final class ClangdCompilationDatabaseSetterTest {

	private static final String RELATIVE_DIR_PATH_BUILD_DEFAULT = "build" + File.separator + "default";
	private static final String RELATIVE_DIR_PATH_BUILD_DEBUG = "build" + File.separator + "debug";
	private static final String EXPANDED_CDB_SETTING = "CompileFlags: {Add: -ferror-limit=500, CompilationDatabase: %s, Compiler: g++}\nDiagnostics:\n  ClangTidy: {Add: modernize*, Remove: modernize-use-trailing-return-type}\n";
	private static final String DEFAULT_CDB_SETTING = "CompileFlags: {CompilationDatabase: %s}";
	private static final String MODIFIED_DEFAULT_CDB_SETTING = DEFAULT_CDB_SETTING + "\n";
	private static final String INVALID_YAML_SYNTAX_CONTAINS_TAB = "CompileFlags:\n\tCompilationDatabase: %s";
	private static final String INVALID_YAML_SYNTAX_MISSING_BRACE = "CompileFlags: {CompilationDatabase: Release\r\n";
	private final ClangdCompilationDatabaseSetter clangdCompilationDatabaseSetter = new ClangdCompilationDatabaseSetter();
	private IProject project;

	private static CProjectDescriptionEvent event = mock(CProjectDescriptionEvent.class);
	private static ICProjectDescription description = mock(ICProjectDescription.class);
	private static CConfigurationDescriptionCache config = mock(CConfigurationDescriptionCache.class);
	private static ICBuildSetting setting = mock(ICBuildSetting.class);
	private Path cwdBuilder;

	@TempDir
	private static File TEMP_DIR;

	@BeforeAll
	public static void setUp() throws Exception {
		when(event.getNewCProjectDescription()).thenReturn(description);
		when(description.getDefaultSettingConfiguration()).thenReturn(config);
		when(config.getBuildSetting()).thenReturn(setting);
	}

	@BeforeEach
	public void setUp(TestInfo testInfo) throws CoreException {
		var projectName = TestUtils.getName(testInfo);
		project = TestUtils.createCProject(projectName);
		TestUtils.setLspPreferred(project, true);
		when(event.getProject()).thenReturn(project);
	}

	@AfterEach
	public void cleanUp() throws CoreException {
		TestUtils.deleteProject(project);
	}

	private static File createFile(File parent, String format, String cdbDirectoryPath) throws FileNotFoundException {
		return createFile(parent, ClangdCompilationDatabaseSetterBase.CLANGD_CONFIG_FILE_NAME, format,
				cdbDirectoryPath);
	}

	private static File createFile(File parent, String fileName, String format, String cdbDirectoryPath)
			throws FileNotFoundException {
		var file = new File(parent, fileName);
		try (var writer = new PrintWriter(file)) {
			writer.printf(format, cdbDirectoryPath);
		}
		return file;
	}

	/**
	 * Creates a .clangd file in the current project
	 * @param format
	 * @param cdbDirectoryPath
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 * @throws CoreException
	 */
	private IFile createConfigFile(String format, String cdbDirectoryPath)
			throws UnsupportedEncodingException, IOException, CoreException {
		var file = project.getFile(ClangdCompilationDatabaseSetterBase.CLANGD_CONFIG_FILE_NAME);
		try (final var data = new ByteArrayInputStream(
				String.format(format, cdbDirectoryPath).getBytes(project.getDefaultCharset()))) {
			if (!file.exists()) {
				file.create(data, false, new NullProgressMonitor());
			} else {
				file.setContents(data, IResource.KEEP_HISTORY, new NullProgressMonitor());
			}
		}
		return file;
	}

	/**
	 * Test whether a new .clangd file will be created in the given project directory with the given
	 * configuration database (cdb) directory path (build/default) when the file does not exist.
	 *
	 * @throws IOException
	 * @throws CoreException
	 */
	@Test
	void testCreateClangdConfigFileInProject() throws IOException, CoreException {
		var projectDir = project.getLocation().toPortableString();
		var configFile = new File(projectDir, ClangdCompilationDatabaseSetterBase.CLANGD_CONFIG_FILE_NAME);
		var refFile = createFile(TEMP_DIR, DEFAULT_CDB_SETTING, RELATIVE_DIR_PATH_BUILD_DEFAULT);
		// The current working directory of the builder in the project is set to RELATIVE_DIR_PATH_BUILD_DEFAULT:
		cwdBuilder = new Path(project.getLocation().append(RELATIVE_DIR_PATH_BUILD_DEFAULT).toPortableString());
		when(setting.getBuilderCWD()).thenReturn(cwdBuilder);

		// GIVEN a project without .clangd project configuration file:
		assertTrue(configFile.length() == 0);
		// WHEN the ClangdCProjectDescriptionListenerHandler.handleEvent method gets called:
		clangdCompilationDatabaseSetter.getCProjectDescriptionListener().handleEvent(event);
		// THEN a new file has been created in the project:
		assertTrue(configFile.length() > 0);
		// AND the file content is as expected:
		assertTrue(Arrays.equals(Files.readAllBytes(configFile.toPath()), Files.readAllBytes(refFile.toPath())));

		//clean up:
		refFile.delete();
	}

	/**
	 * Test whether the new configuration database (cdb) directory path (build/debug) will be written to an existing but empty .clangd file
	 *
	 * @throws IOException
	 * @throws CoreException
	 */
	@Test
	void testEmptyClangdConfigFileInProject() throws IOException, CoreException {
		var refFile = createFile(TEMP_DIR, DEFAULT_CDB_SETTING, RELATIVE_DIR_PATH_BUILD_DEFAULT);
		// The current working directory of the builder in the project is set to RELATIVE_DIR_PATH_BUILD_DEFAULT:
		cwdBuilder = new Path(project.getLocation().append(RELATIVE_DIR_PATH_BUILD_DEFAULT).toPortableString());
		when(setting.getBuilderCWD()).thenReturn(cwdBuilder);

		// GIVEN an existing but empty .clangd configuration file in the project:
		var emptyConfigFile = createConfigFile("%s", "  ");
		// WHEN the ClangdCProjectDescriptionListenerHandler.handleEvent method gets called with a new cdb path "build/debug":
		clangdCompilationDatabaseSetter.getCProjectDescriptionListener().handleEvent(event);
		// THEN the updated file matches the expected content with the given CompilationDatabase directory "build/debug":
		assertTrue(Arrays.equals(Files.readAllBytes(emptyConfigFile.getLocation().toFile().toPath()),
				Files.readAllBytes(refFile.toPath())));

		//clean up:
		refFile.delete();
	}

	/**
	 * Test whether the new configuration database (cdb) directory path (build/debug) will be written to an existing .clangd file
	 *
	 * @throws IOException
	 * @throws CoreException
	 */
	@Test
	void testUpdateClangdConfigFileInProject() throws IOException, CoreException {
		var projectDir = project.getLocation().toPortableString();
		var configFile = new File(projectDir, ClangdCompilationDatabaseSetterBase.CLANGD_CONFIG_FILE_NAME);
		var refFileDefault = createFile(TEMP_DIR, ".clangdDefault", DEFAULT_CDB_SETTING,
				RELATIVE_DIR_PATH_BUILD_DEFAULT);
		// Use MODIFIED_DEFAULT_CDB_SETTING here, because the org.yaml.snakeyaml.Yaml.dump appends a '\n' at the last line:
		var refFileDebug = createFile(TEMP_DIR, ".clangdDebug", MODIFIED_DEFAULT_CDB_SETTING,
				RELATIVE_DIR_PATH_BUILD_DEBUG);
		// The current working directory of the builder in the project is set to RELATIVE_DIR_PATH_BUILD_DEFAULT:
		cwdBuilder = new Path(project.getLocation().append(RELATIVE_DIR_PATH_BUILD_DEFAULT).toPortableString());
		when(setting.getBuilderCWD()).thenReturn(cwdBuilder);

		// GIVEN a project without .clangd project configuration file:
		assertTrue(configFile.length() == 0);
		// AND the ClangdCProjectDescriptionListenerHandler.handleEvent method gets called:
		clangdCompilationDatabaseSetter.getCProjectDescriptionListener().handleEvent(event);
		// THEN a new file has been created in the project:
		assertTrue(configFile.length() > 0);
		// THEN the created file matches the expected content:
		assertTrue(Arrays.equals(Files.readAllBytes(configFile.toPath()), Files.readAllBytes(refFileDefault.toPath())));

		// WHEN the CWD in the build configuration changes to build/debug:
		cwdBuilder = new Path(project.getLocation().append(RELATIVE_DIR_PATH_BUILD_DEBUG).toPortableString());
		when(setting.getBuilderCWD()).thenReturn(cwdBuilder);
		// AND the handleEvent gets called again:
		clangdCompilationDatabaseSetter.getCProjectDescriptionListener().handleEvent(event);
		// THEN the updated file matches the expected content:
		assertTrue(Arrays.equals(Files.readAllBytes(configFile.toPath()), Files.readAllBytes(refFileDebug.toPath())));

		//clean up:
		refFileDefault.delete();
		refFileDebug.delete();
	}

	/**
	 * Test whether the new configuration database (cdb) directory path (build/debug) will be written to an existing expanded .clangd file
	 *
	 * @throws IOException
	 * @throws CoreException
	 */
	@Test
	void testUpdateExpandedClangdConfigFileInProject() throws IOException, CoreException {
		var refFile = createFile(TEMP_DIR, EXPANDED_CDB_SETTING, RELATIVE_DIR_PATH_BUILD_DEBUG);

		// GIVEN an existing expanded .clangd configuration file in the project pointing to "build/default":
		var configFile = createConfigFile(EXPANDED_CDB_SETTING, RELATIVE_DIR_PATH_BUILD_DEFAULT);
		// WHEN the ClangdCProjectDescriptionListenerHandler.handleEvent method gets called and the builder CWD points to "build/debug":
		cwdBuilder = new Path(project.getLocation().append(RELATIVE_DIR_PATH_BUILD_DEBUG).toPortableString());
		when(setting.getBuilderCWD()).thenReturn(cwdBuilder);
		clangdCompilationDatabaseSetter.getCProjectDescriptionListener().handleEvent(event);
		// THEN the updated file matches the expected content:
		assertTrue(Arrays.equals(Files.readAllBytes(configFile.getLocation().toFile().toPath()),
				Files.readAllBytes(refFile.toPath())));

		//clean up:
		refFile.delete();
	}

	/**
	 * Test whether a ScannerException will be thrown if the file contains invalid yaml syntax (here: tab)
	 *
	 * @throws IOException
	 * @throws CoreException
	 */
	@Test
	void testInvalidYamlSyntax() throws IOException, CoreException {
		// GIVEN an existing .clangd configuration file with invalid yaml syntax (contains tab):
		var configFile = createConfigFile(INVALID_YAML_SYNTAX_CONTAINS_TAB, RELATIVE_DIR_PATH_BUILD_DEFAULT);
		String beforeSet;
		try (var inputStream = configFile.getContents()) {
			beforeSet = new String(inputStream.readAllBytes());
		}
		// WHEN the ClangdConfigurationFileHandlerBase.setCompilationDatabasePath method gets called with a new cdb path "build/debug":
		clangdCompilationDatabaseSetter.setCompilationDatabase(project, RELATIVE_DIR_PATH_BUILD_DEBUG);
		// THEN the file has not been changed, because the user shall fix the errors first:
		try (var inputStream = configFile.getContents()) {
			var afterSet = new String(inputStream.readAllBytes());
			assertEquals(beforeSet, afterSet);
		}
	}

	/**
	 * Test whether a ParserException will be thrown if the file contains invalid yaml syntax (here: missing })
	 *
	 * @throws IOException
	 * @throws CoreException
	 */
	@Test
	void testInvalidYamlSyntax2() throws IOException, CoreException {
		// GIVEN an existing .clangd configuration file with invalid yaml syntax (missing }):
		var configFile = createConfigFile(INVALID_YAML_SYNTAX_MISSING_BRACE, RELATIVE_DIR_PATH_BUILD_DEFAULT);
		String beforeSet;
		try (var inputStream = configFile.getContents()) {
			beforeSet = new String(inputStream.readAllBytes());
		}
		// WHEN the ClangdConfigurationFileHandlerBase.setCompilationDatabasePath method gets called with a new cdb path "build/debug":
		clangdCompilationDatabaseSetter.setCompilationDatabase(project, RELATIVE_DIR_PATH_BUILD_DEBUG);
		// THEN the file has not been changed, because the user shall fix the errors first:
		try (var inputStream = configFile.getContents()) {
			var afterSet = new String(inputStream.readAllBytes());
			assertEquals(beforeSet, afterSet);
		}
	}

}
