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

package org.eclipse.cdt.lsp.internal.clangd;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.yaml.snakeyaml.Yaml;

public class ClangdConfigurationManager {
	public static final String CLANGD_CONFIG_FILE_NAME = ".clangd";
	private static final String COMPILE_FLAGS = "CompileFlags";
	private static final String COMPILATTION_DATABASE = "CompilationDatabase";
	private static final String SET_COMPILATION_DB = COMPILE_FLAGS + ": {" + COMPILATTION_DATABASE + ": %s}";

	/**
	 * Set the <code>CompilationDatabase</code> entry in the .clangd file in the given project root.
	 * The file will be created, if it's not existing.
	 * A ScannerException will be thrown if the configuration file contains invalid yaml syntax.
	 *
	 * @param configFileDirectory
	 * @param databasePath
	 * @throws IOException, ScannerException
	 * @throws CoreException
	 */
	@SuppressWarnings("unchecked")
	public static void setCompilationDatabase(IProject project, String databasePath) throws IOException, CoreException {
		var configFile = project.getFile(CLANGD_CONFIG_FILE_NAME);
		if (createClangdConfigFile(configFile, project.getDefaultCharset(), databasePath, false)) {
			return;
		}
		Map<String, Object> data = null;
		Yaml yaml = new Yaml();
		try (var inputStream = configFile.getContents()) {
			//throws ScannerException
			data = yaml.load(inputStream);
		}
		if (data == null) {
			//empty file: (re)create .clangd file:
			createClangdConfigFile(configFile, project.getDefaultCharset(), databasePath, true);
			return;
		}
		Map<String, Object> map = (Map<String, Object>) data.get(COMPILE_FLAGS);
		if (map != null) {
			var cdb = map.get(COMPILATTION_DATABASE);
			if (cdb != null && cdb instanceof String) {
				if (cdb.equals(databasePath)) {
					return;
				}
			}
			map.put(COMPILATTION_DATABASE, databasePath);
			data.put(COMPILE_FLAGS, map);
			try (var yamlWriter = new PrintWriter(configFile.getLocation().toFile())) {
				yaml.dump(data, yamlWriter);
			}
		}

	}

	private static boolean createClangdConfigFile(IFile configFile, String charset, String databasePath,
			boolean overwriteContent) throws IOException, CoreException {
		if (!configFile.exists() || overwriteContent) {
			try (final var data = new ByteArrayInputStream(
					String.format(SET_COMPILATION_DB, databasePath).getBytes(charset))) {
				if (overwriteContent) {
					configFile.setContents(data, IResource.KEEP_HISTORY, new NullProgressMonitor());
				} else {
					configFile.create(data, false, new NullProgressMonitor());
				}
			}
			return true;
		}
		return false;
	}
}
