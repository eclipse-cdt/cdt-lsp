/*******************************************************************************
 * Copyright (c) 2023 ArSysOp.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.clangd;

import org.eclipse.core.runtime.preferences.PreferenceMetadata;

/**
 * The metadata for options to configure clangd
 *
 * @see ClangdOptions
 * @since 2.0
 */
public interface ClangdMetadata {

	/**
	 * Returns the metadata for the "Clangd path" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Clangd path" option
	 *
	 * @see ClangdOptions#clangdPath()
	 */
	PreferenceMetadata<String> clangdPath();

	/**
	 * Returns the metadata for the "Enable clang-tidy" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Enable clang-tidy" option
	 *
	 * @see ClangdOptions#useTidy()
	 */
	PreferenceMetadata<Boolean> useTidy();

	/**
	 * Returns the metadata for the "Background index" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Background index" option
	 *
	 * @see ClangdOptions#useBackgroundIndex()
	 */
	PreferenceMetadata<Boolean> useBackgroundIndex();

	/**
	 * Returns the metadata for the "Completion style" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Completion style" option
	 *
	 * @see ClangdOptions#completionStyle()
	 */
	PreferenceMetadata<String> completionStyle();

	/**
	 * Returns the metadata for the "Pretty print" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Pretty print" option
	 *
	 * @see ClangdOptions#prettyPrint()
	 */
	PreferenceMetadata<Boolean> prettyPrint();

	/**
	 * Returns the metadata for the "Query driver" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Query driver" option
	 *
	 * @see ClangdOptions#queryDriver()
	 */
	PreferenceMetadata<String> queryDriver();

	/**
	 * Returns the metadata for the additional options, must not return <code>null</code>.
	 *
	 * @return the metadata for the additional options
	 *
	 * @see ClangdOptions#additionalOptions
	 */
	PreferenceMetadata<String> additionalOptions();

	/**
	 * Returns the metadata for the "Log to Console" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Log to Console" option
	 *
	 * @see ClangdOptions#logToConsole()
	 *
	 * @since 2.2
	 */
	default PreferenceMetadata<Boolean> logToConsole() {
		return new PreferenceMetadata<>(Boolean.class, //
				"log_to_console", //$NON-NLS-1$
				false, //
				"Log to Console", // //$NON-NLS-1$
				"Log to Console"); //$NON-NLS-1$
	}

	/**
	 * Returns the metadata for the "Validate clangd options" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Validate clangd options" option
	 *
	 * @see ClangdOptions#validateClangdOptions()
	 *
	 * @since 2.2
	 */
	default PreferenceMetadata<Boolean> validateClangdOptions() {
		return new PreferenceMetadata<>(Boolean.class, //
				"validate_clangd_options", //$NON-NLS-1$
				true, //
				"Validate clangd options", // //$NON-NLS-1$
				"Validate clangd options"); //$NON-NLS-1$
	}

}
