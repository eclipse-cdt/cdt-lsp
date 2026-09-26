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

/**
 * Immutable snapshot of the effective compilation database state shown in the clangd project
 * properties page.
 *
 * @param source whether the state comes from automatic detection, a custom override, or neither
 * @param configuredDirectory effective directory used to resolve {@code compile_commands.json}
 * @param compileCommandsPath resolved absolute path to {@code compile_commands.json}
 * @param buildConfiguration active workspace build configuration name
 * @param automaticManagementEnabled whether automatic {@code .clangd} synchronization is enabled
 * @param compileCommandsExists whether the resolved {@code compile_commands.json} file exists
 * @param message user-facing status message for the current state
 */
public record ClangdCompilationDatabaseStatus(Source source, String configuredDirectory, String compileCommandsPath,
		String buildConfiguration, boolean automaticManagementEnabled, boolean compileCommandsExists, String message) {

	/**
	 * Identifies which input currently determines the effective compilation database location.
	 */
	public enum Source {
		AUTOMATIC,
		MANUAL,
		NONE
	}

	/**
	 * Returns whether a directory is currently configured for the project.
	 */
	boolean hasConfiguredDirectory() {
		return configuredDirectory != null && !configuredDirectory.isBlank();
	}
}
