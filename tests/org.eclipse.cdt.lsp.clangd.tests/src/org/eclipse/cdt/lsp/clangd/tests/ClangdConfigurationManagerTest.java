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
 *     Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *     Alexander Fedorov (ArSysOp) - extract headless part
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd.tests;

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
import org.eclipse.cdt.lsp.clangd.ClangdCProjectDescriptionListener;
import org.eclipse.cdt.lsp.clangd.MacroResolver;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.PlatformUI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

final class ClangdConfigurationManagerTest {

	private static final String RELATIVE_DIR_PATH_BUILD_DEFAULT = "build/default";
	private static final String RELATIVE_DIR_PATH_BUILD_DEBUG = "build/debug";
	private static final String EXPANDED_CDB_SETTING = "CompileFlags: {Add: -ferror-limit=500, CompilationDatabase: %s, Compiler: g++}\nDiagnostics:\n  ClangTidy: {Add: modernize*, Remove: modernize-use-trailing-return-type}\n";
	private static final String DEFAULT_CDB_SETTING = "CompileFlags: {CompilationDatabase: %s}";
	private static final String MODIFIED_DEFAULT_CDB_SETTING = DEFAULT_CDB_SETTING + "\n";
	private static final String INVALID_YAML_SYNTAX_CONTAINS_TAB = "CompileFlags:\n\tCompilationDatabase: %s";
	private final ClangdCProjectDescriptionListener clangdConfigurationManager = PlatformUI.getWorkbench()
			.getService(ClangdCProjectDescriptionListener.class);
	private final MacroResolver macroResolver = new MockMacroResolver();
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
		cwdBuilder = new Path(project.getLocation().append(RELATIVE_DIR_PATH_BUILD_DEFAULT).toPortableString());
		when(setting.getBuilderCWD()).thenReturn(cwdBuilder);
	}

	@AfterEach
	public void cleanUp() throws CoreException {
		TestUtils.deleteProject(project);
	}

	private static File createFile(File parent, String format, String cdbDirectoryPath) throws FileNotFoundException {
		var file = new File(parent, ClangdCProjectDescriptionListener.CLANGD_CONFIG_FILE_NAME);
		try (var writer = new PrintWriter(file)) {
			writer.printf(format, cdbDirectoryPath);
		}
		return file;
	}

	private IFile createFile(String format, String cdbDirectoryPath)
			throws UnsupportedEncodingException, IOException, CoreException {
		var file = project.getFile(ClangdCProjectDescriptionListener.CLANGD_CONFIG_FILE_NAME);
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
		var configFile = new File(projectDir, ClangdCProjectDescriptionListener.CLANGD_CONFIG_FILE_NAME);
		var refFile = createFile(TEMP_DIR, DEFAULT_CDB_SETTING, RELATIVE_DIR_PATH_BUILD_DEFAULT);
		// The current working directory of the builder in the project is set to RELATIVE_DIR_PATH_BUILD_DEFAULT:
		//cwdBuilder = new Path(project.getLocation().append(RELATIVE_DIR_PATH_BUILD_DEFAULT).toPortableString());

		// GIVEN a project without .clangd project configuration file:
		assertTrue(configFile.length() == 0);
		// WHEN the ClangdConfigurationManager.setCompilationDatabase method gets called:
		clangdConfigurationManager.handleEvent(event, macroResolver);
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
		//cwdBuilder = new Path(project.getLocation().append(RELATIVE_DIR_PATH_BUILD_DEFAULT).toPortableString());

		// GIVEN an existing but empty .clangd configuration file in the project:
		var emptyConfigFile = createFile("%s", "  ");
		// WHEN the ClangdConfigurationManager.setCompilationDatabase method gets called with a new cdb path "build/debug":
		//clangdConfigurationManager.handleEvent(event, macroResolver);
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
		var configFile = new File(projectDir, ClangdCProjectDescriptionListener.CLANGD_CONFIG_FILE_NAME);
		var cwdDefault = createFile(TEMP_DIR, MODIFIED_DEFAULT_CDB_SETTING, RELATIVE_DIR_PATH_BUILD_DEFAULT);
		var cwdDebug = createFile(TEMP_DIR, MODIFIED_DEFAULT_CDB_SETTING, RELATIVE_DIR_PATH_BUILD_DEBUG);
		// The current working directory of the builder in the project is set to RELATIVE_DIR_PATH_BUILD_DEFAULT:
		//cwdBuilder = new Path(project.getLocation().append(RELATIVE_DIR_PATH_BUILD_DEFAULT).toPortableString());

		// GIVEN a project without .clangd project configuration file:
		assertTrue(configFile.length() == 0);
		// AND the ClangdConfigurationManager.handleEvent method gets called:
		clangdConfigurationManager.handleEvent(event, macroResolver);
		// THEN a new file has been created in the project:
		assertTrue(configFile.length() > 0);
		// THEN the file matches the expected content:
		//assertTrue(Arrays.equals(Files.readAllBytes(configFile.toPath()), Files.readAllBytes(cwdDefault.toPath())));
		// WHEN the active build configuration changes and the CWD is set to RELATIVE_DIR_PATH_BUILD_DEBUG:
		//cwdBuilder = new Path(project.getLocation().append(RELATIVE_DIR_PATH_BUILD_DEBUG).toPortableString());
		// AND the handleEvent gets called again:
		clangdConfigurationManager.handleEvent(event, macroResolver);
		// THEN the updated file matches the expected content:
		assertTrue(Arrays.equals(Files.readAllBytes(configFile.toPath()), Files.readAllBytes(cwdDebug.toPath())));

		//clean up:
		cwdDefault.delete();
		cwdDebug.delete();
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
		var configFile = createFile(EXPANDED_CDB_SETTING, RELATIVE_DIR_PATH_BUILD_DEFAULT);
		// WHEN the ClangdConfigurationManager.handleEvent method gets called and the builder CWD points to "build/debug":
		//cwdBuilder = new Path(project.getLocation().append(RELATIVE_DIR_PATH_BUILD_DEBUG).toPortableString());
		clangdConfigurationManager.handleEvent(event, macroResolver);
		// THEN the updated file matches the expected content:
		assertTrue(Arrays.equals(Files.readAllBytes(configFile.getLocation().toFile().toPath()),
				Files.readAllBytes(refFile.toPath())));

		//clean up:
		refFile.delete();
	}

	/**
	 * Test whether a ScannerExcpetion will be thrown if the file contains invalid yaml syntax (here: tab)
	 *
	 * @throws IOException
	 * @throws CoreException
	 */
	//	@Test
	//	void testInvalidYamlSyntax() throws IOException, CoreException {
	//		// GIVEN an existing .clangd configuration file with invalid yaml syntax (contains tab):
	//		createFile(INVALID_YAML_SYNTAX_CONTAINS_TAB, RELATIVE_DIR_PATH_BUILD_DEFAULT);
	//		// WHEN the ClangdConfigurationManager.setCompilationDatabase method gets called with a new cdb path "build/debug":
	//		// THEN a ScannerExcpetion is expected:
	//		assertThrows(ScannerException.class, () -> {
	//			clangdConfigurationManager.handleEvent(event, macroResolver);//RELATIVE_DIR_PATH_BUILD_DEBUG);
	//		});
	//	}

}
