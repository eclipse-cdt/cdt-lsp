/*******************************************************************************
 * Copyright (c) 2023, 2025 ArSysOp.
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.cdt.lsp.clangd.internal.ui.LspEditorUiMessages;
import org.eclipse.cdt.lsp.config.ConfigurationMetadata;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

/**
 * The metadata for options to configure clangd
 *
 * @see ClangdOptions
 * @since 2.0
 */
public interface ClangdMetadata extends ConfigurationMetadata {

	/**
	 * The predefined metadata for the "Clangd path" option.
	 *
	 * @see ClangdOptions#clangdPath()
	 *
	 * @since 3.0
	 */
	PreferenceMetadata<String> clangdPath = new PreferenceMetadata<>(String.class, //
			"clangd_path", //$NON-NLS-1$
			Optional.ofNullable(PathUtil.findProgramLocation("clangd", null)) //$NON-NLS-1$
					.map(IPath::toOSString).orElse("clangd"), //$NON-NLS-1$
			LspEditorUiMessages.LspEditorPreferencePage_path, //
			LspEditorUiMessages.LspEditorPreferencePage_path_description);

	/**
	 * The predefined metadata for the "Enable clang-tidy" option.
	 *
	 * @see ClangdOptions#useTidy()
	 *
	 * @since 3.0
	 */
	PreferenceMetadata<Boolean> useTidy = new PreferenceMetadata<>(Boolean.class, //
			"use_tidy", //$NON-NLS-1$
			true, //
			LspEditorUiMessages.LspEditorPreferencePage_enable_tidy, //
			LspEditorUiMessages.LspEditorPreferencePage_enable_tidy);

	/**
	 * The predefined metadata for the "Background index" option.
	 *
	 * @see ClangdOptions#useBackgroundIndex()
	 *
	 * @since 3.0
	 */
	PreferenceMetadata<Boolean> useBackgroundIndex = new PreferenceMetadata<>(Boolean.class, //
			"background_index", //$NON-NLS-1$
			true, //
			LspEditorUiMessages.LspEditorPreferencePage_background_index, //
			LspEditorUiMessages.LspEditorPreferencePage_background_index);

	/**
	 * The predefined metadata for the "Completion style" option.
	 *
	 * @see ClangdOptions#completionStyle()
	 *
	 * @since 3.0
	 */
	PreferenceMetadata<String> completionStyle = new PreferenceMetadata<>(String.class, //
			"completion_style", //$NON-NLS-1$
			"detailed", //$NON-NLS-1$
			LspEditorUiMessages.LspEditorPreferencePage_completion, //
			LspEditorUiMessages.LspEditorPreferencePage_completion_description);

	/**
	 * The predefined metadata for the "Pretty print" option.
	 *
	 * @see ClangdOptions#prettyPrint()
	 *
	 * @since 3.0
	 */
	PreferenceMetadata<Boolean> prettyPrint = new PreferenceMetadata<>(Boolean.class, //
			"pretty_print", //$NON-NLS-1$
			true, //
			LspEditorUiMessages.LspEditorPreferencePage_pretty_print, //
			LspEditorUiMessages.LspEditorPreferencePage_pretty_print);

	/**
	 * The predefined metadata for the "Query driver" option.
	 *
	 * @see ClangdOptions#queryDriver()
	 *
	 * @since 3.0
	 */
	PreferenceMetadata<String> queryDriver = new PreferenceMetadata<>(String.class, //
			"query_driver", //$NON-NLS-1$
			Optional.ofNullable(PathUtil.findProgramLocation("gcc", null)) //$NON-NLS-1$
					.map(p -> p.removeLastSegments(1).append(IPath.SEPARATOR + "*"))// //$NON-NLS-1$
					.map(IPath::toString).orElse(""), //$NON-NLS-1$
			LspEditorUiMessages.LspEditorPreferencePage_drivers, //
			LspEditorUiMessages.LspEditorPreferencePage_drivers_description);

	/**
	 * The predefined metadata for the additional options, must not return <code>null</code>.
	 *
	 * @see ClangdOptions#additionalOptions
	 *
	 * @since 3.0
	 */
	PreferenceMetadata<String> additionalOptions = new PreferenceMetadata<>(String.class, //
			"additional_options", //$NON-NLS-1$
			List.of("").stream().collect(Collectors.joining(System.lineSeparator())), //$NON-NLS-1$
			LspEditorUiMessages.LspEditorPreferencePage_additional, //
			LspEditorUiMessages.LspEditorPreferencePage_additional_description);

	/**
	 * The predefined metadata for the "Log to Console" option.
	 *
	 * @see ClangdOptions#logToConsole()
	 *
	 * @since 3.0
	 */
	PreferenceMetadata<Boolean> logToConsole = new PreferenceMetadata<>(Boolean.class, //
			"log_to_console", //$NON-NLS-1$
			false, //
			LspEditorUiMessages.LspEditorPreferencePage_Log_to_Console, //
			LspEditorUiMessages.LspEditorPreferencePage_Log_to_Console_description);

	/**
	 * The predefined metadata for the "Validate clangd options" option.
	 *
	 * @see ClangdOptions#validateClangdOptions()
	 *
	 * @since 3.0
	 */
	PreferenceMetadata<Boolean> validateClangdOptions = new PreferenceMetadata<>(Boolean.class, //
			"validate_clangd_options", //$NON-NLS-1$
			true, //
			LspEditorUiMessages.LspEditorPreferencePage_Validate_clangd_options, //
			LspEditorUiMessages.LspEditorPreferencePage_Validate_clangd_options_description);

	/**
	 * Returns the metadata for the "Fill function arguments and show guessed arguments" option.
	 *
	 * @see ClangdOptions#fillFunctionArguments()
	 *
	 * @since 3.0
	 */
	PreferenceMetadata<Boolean> fillFunctionArguments = new PreferenceMetadata<>(Boolean.class, //
			"fill_function_arguments", //$NON-NLS-1$
			true, //
			LspEditorUiMessages.ContentAssistConfigurationPage_fill_function_arguments,
			LspEditorUiMessages.ContentAssistConfigurationPage_fill_function_arguments_description);

	/**
	 * Returns the default {@link List} of {@link PreferenceMetadata}
	 *
	 * @since 3.0
	 */
	List<PreferenceMetadata<?>> defaults = List.of(//
			clangdPath, //
			useTidy, //
			useBackgroundIndex, //
			completionStyle, //
			prettyPrint, //
			queryDriver, //
			additionalOptions, //
			logToConsole, //
			validateClangdOptions, //
			fillFunctionArguments//
	);

}
