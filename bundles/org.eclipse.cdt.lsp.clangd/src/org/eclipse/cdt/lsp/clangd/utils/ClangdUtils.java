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

public class ClangdUtils {
	public static final String format_file = ".clang-format"; //$NON-NLS-1$

	/**
	 * Checks if a .clang-format file exists in the workspace root directory. Creates it if not.
	 * This ensures that the user has a default formatting similar to the CDT default K&R formatter
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
	 * Checks if the configFile exists. Creates it if not.
	 * @param configFile
	 */
	public void checkProjectClangFormatFile(IFile configFile) {
		if (!configFile.exists()) {
			try (final var source = getClass().getResourceAsStream(".clang-format-project");) { //$NON-NLS-1$
				configFile.create(source, true, new NullProgressMonitor());
			} catch (CoreException e) {
				Platform.getLog(getClass()).log(e.getStatus());
			} catch (IOException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}
		}
	}

}
