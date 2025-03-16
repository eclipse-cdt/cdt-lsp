/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd.internal.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

public abstract class ClangdConfigurationFileHandlerBase {
	public static final String CLANGD_CONFIG_FILE_NAME = ".clangd"; //$NON-NLS-1$
	private static final String COMPILE_FLAGS = "CompileFlags"; //$NON-NLS-1$
	private static final String COMPILATTION_DATABASE = "CompilationDatabase"; //$NON-NLS-1$
	protected static final String SET_COMPILATION_DB = COMPILE_FLAGS + ": {" + COMPILATTION_DATABASE + ": %s}"; //$NON-NLS-1$ //$NON-NLS-2$
	protected static final String EMPTY = ""; //$NON-NLS-1$

	/**
	 * Set the <code>CompilationDatabase</code> entry in the .clangd file in the given project root.
	 * The file will be created, if it's not existing.
	 * A ScannerException will be thrown if the configuration file contains invalid yaml syntax.
	 *
	 * @param project to write the .clangd file
	 * @param databasePath project relative path to .clangd file
	 * @throws IOException
	 * @throws ScannerException
	 * @throws CoreException
	 */
	@SuppressWarnings("unchecked")
	public void setCompilationDatabase(IProject project, String databasePath) {
		var configFile = project.getFile(CLANGD_CONFIG_FILE_NAME);
		try {
			if (createClangdConfigFile(configFile, project.getDefaultCharset(), databasePath, false)) {
				return;
			}
			Map<String, Object> data = null;
			Yaml yaml = new Yaml();
			try (var inputStream = configFile.getContents()) {
				//throws ScannerException and ParserException:
				try {
					data = yaml.load(inputStream);
				} catch (Exception e) {
					Platform.getLog(getClass()).error(e.getMessage(), e);
					// return, since the file syntax is corrupted. The user has to fix it first:
					return;
				}
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
		} catch (CoreException e) {
			Platform.getLog(getClass()).log(e.getStatus());
		} catch (IOException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}
	}

	private boolean createClangdConfigFile(IFile configFile, String charset, String databasePath,
			boolean overwriteContent) {
		if (!configFile.exists() || overwriteContent) {
			try (final var data = new ByteArrayInputStream(
					String.format(SET_COMPILATION_DB, databasePath).getBytes(charset))) {
				if (overwriteContent) {
					configFile.setContents(data, IResource.KEEP_HISTORY, new NullProgressMonitor());
				} else {
					configFile.create(data, false, new NullProgressMonitor());
				}
				return true;
			} catch (CoreException e) {
				Platform.getLog(getClass()).log(e.getStatus());
			} catch (IOException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}
		}
		return false;
	}
}