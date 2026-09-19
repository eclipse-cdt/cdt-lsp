/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

public record ClangdCompilationDatabaseStatus(Source source, String configuredDirectory, String compileCommandsPath,
		String buildConfiguration, boolean automaticManagementEnabled, boolean compileCommandsExists, String message) {

	public enum Source {
		AUTOMATIC,
		MANUAL,
		NONE
	}

	boolean hasConfiguredDirectory() {
		return configuredDirectory != null && !configuredDirectory.isBlank();
	}
}
