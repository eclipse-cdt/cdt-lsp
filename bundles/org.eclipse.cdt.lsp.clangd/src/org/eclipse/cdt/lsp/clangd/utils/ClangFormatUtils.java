/*******************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

public final class ClangFormatUtils {
	public static final String format_file = ".clang-format"; //$NON-NLS-1$

	/**
	 * Checks if a .clang-format file exists in the workspace root directory. Creates it if not.
	 * This ensures that the user has a default formatting similar to the CDT default K&R formatter
	 *
	 * The reason why we store the .clang-format file in the workspace root is:
	 * clangd searches parent directories for .clang-format files.
	 * Putting a .clang-format file in the root directory ensures that workspace wide preferences
	 * will be used for formatting via clangd. It's a smart way (according to the convention over configuration paradigm)
	 * to tell clangd which .clang-format file should be used.
	 *
	 */
	public void checkWorkspaceClangFormatFile(IWorkspace workspace) {
		var path = workspace.getRoot().getLocation().append(format_file).toOSString();
		var formatFile = new File(path);
		if (!formatFile.exists()) {
			try (final var source = getClass().getResourceAsStream(".clang-format-ws");) { //$NON-NLS-1$
				Files.copy(source, formatFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Checks if the formatFile exists. Creates it if not.
	 * @param formatFile
	 */
	public void checkProjectClangFormatFile(IFile formatFile) {
		if (!formatFile.exists()) {
			createFileFromResource(formatFile, ".clang-format-project"); //$NON-NLS-1$
		}
	}

	private void createFileFromResource(IFile formatFile, String sourceFileName) {
		try (final var source = getClass().getResourceAsStream(sourceFileName);) {
			formatFile.create(source, true, new NullProgressMonitor());
		} catch (CoreException | IOException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}
	}

}
