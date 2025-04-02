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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

public abstract class ClangdCompilationDatabaseSetterBase {
	public static final String CLANGD_CONFIG_FILE_NAME = ".clangd"; //$NON-NLS-1$
	private static final String COMPILE_FLAGS = "CompileFlags"; //$NON-NLS-1$
	private static final String COMPILATTION_DATABASE = "CompilationDatabase"; //$NON-NLS-1$
	protected static final String SET_COMPILATION_DB = COMPILE_FLAGS + ": {" + COMPILATTION_DATABASE + ": %s}"; //$NON-NLS-1$ //$NON-NLS-2$
	protected static final String EMPTY = ""; //$NON-NLS-1$

	/**
	 * Set the <code>CompilationDatabase</code> entry in the .clangd file in the given project root.
	 * The file will be created, if it's not existing.
	 *
	 * @param project to write the .clangd file
	 * @param databasePath project relative path to .clangd file
	 */
	public void setCompilationDatabase(IProject project, String databasePath) {
		var configFile = project.getFile(CLANGD_CONFIG_FILE_NAME);
		try {
			if (createClangdConfigFile(configFile, project.getDefaultCharset(), databasePath, false)) {
				return;
			}
			var content = configFile.readString();
			var result = content.replaceAll(
					"(?<=CompilationDatabase:)[\\s]*[\\w\\.\\/\\-\\:\\\\]*[\\s]*(?=\\}?\\s*$|\\R)", //$NON-NLS-1$
					" " + databasePath); //$NON-NLS-1$
			writeConfigFile(configFile, result);
		} catch (CoreException e) {
			Platform.getLog(getClass()).log(e.getStatus());
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

	private void writeConfigFile(IFile configFile, String fileData) {
		try {
			configFile.setContents(fileData.getBytes(), IResource.KEEP_HISTORY, new NullProgressMonitor());
		} catch (CoreException e) {
			Platform.getLog(getClass()).log(e.getStatus());
		}
	}
}