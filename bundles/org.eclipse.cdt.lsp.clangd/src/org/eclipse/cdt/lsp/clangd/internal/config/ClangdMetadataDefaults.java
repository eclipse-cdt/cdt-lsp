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
package org.eclipse.cdt.lsp.clangd.internal.config;

import java.util.stream.Collectors;

import org.eclipse.cdt.lsp.clangd.ClangdMetadata2;
import org.eclipse.cdt.lsp.clangd.ClangdOptionsDefaults2;
import org.eclipse.cdt.lsp.clangd.ClangdOptionsDefaults;
import org.eclipse.cdt.lsp.clangd.internal.ui.LspEditorUiMessages;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class ClangdMetadataDefaults implements ClangdMetadata2 {

	@Reference
	private ClangdOptionsDefaults defaults;

	@Reference
	private ClangdOptionsDefaults2 defaults2;

	@Override
	public PreferenceMetadata<String> clangdPath() {
		return new PreferenceMetadata<>(String.class, //
				"clangd_path", //$NON-NLS-1$
				defaults.clangdPath(), LspEditorUiMessages.LspEditorPreferencePage_path, //
				LspEditorUiMessages.LspEditorPreferencePage_path_description);
	}

	@Override
	public PreferenceMetadata<Boolean> useTidy() {
		return new PreferenceMetadata<>(Boolean.class, //
				"use_tidy", //$NON-NLS-1$
				defaults.useTidy(), //
				LspEditorUiMessages.LspEditorPreferencePage_enable_tidy, //
				LspEditorUiMessages.LspEditorPreferencePage_enable_tidy);
	}

	@Override
	public PreferenceMetadata<Boolean> useBackgroundIndex() {
		return new PreferenceMetadata<>(Boolean.class, //
				"background_index", //$NON-NLS-1$
				defaults.useBackgroundIndex(), //
				LspEditorUiMessages.LspEditorPreferencePage_background_index, //
				LspEditorUiMessages.LspEditorPreferencePage_background_index);
	}

	@Override
	public PreferenceMetadata<String> completionStyle() {
		return new PreferenceMetadata<>(String.class, //
				"completion_style", //$NON-NLS-1$
				defaults.completionStyle(), //
				LspEditorUiMessages.LspEditorPreferencePage_completion, //
				LspEditorUiMessages.LspEditorPreferencePage_completion_description);
	}

	@Override
	public PreferenceMetadata<Boolean> prettyPrint() {
		return new PreferenceMetadata<>(Boolean.class, //
				"pretty_print", //$NON-NLS-1$
				defaults.prettyPrint(), //
				LspEditorUiMessages.LspEditorPreferencePage_pretty_print, //
				LspEditorUiMessages.LspEditorPreferencePage_pretty_print);
	}

	@Override
	public PreferenceMetadata<String> queryDriver() {
		return new PreferenceMetadata<>(String.class, //
				"query_driver", //$NON-NLS-1$
				defaults.queryDriver(), LspEditorUiMessages.LspEditorPreferencePage_drivers, //
				LspEditorUiMessages.LspEditorPreferencePage_drivers_description);
	}

	@Override
	public PreferenceMetadata<String> additionalOptions() {
		return new PreferenceMetadata<>(String.class, //
				"additional_options", //$NON-NLS-1$
				defaults.additionalOptions().stream().collect(Collectors.joining(System.lineSeparator())), //$NON-NLS-1$
				LspEditorUiMessages.LspEditorPreferencePage_additional, //
				LspEditorUiMessages.LspEditorPreferencePage_additional_description);
	}

	@Override
	public PreferenceMetadata<Boolean> logToConsole() {
		return new PreferenceMetadata<>(Boolean.class, //
				"log_to_console", //$NON-NLS-1$
				defaults2.logToConsole(), //
				LspEditorUiMessages.LspEditorPreferencePage_Log_to_Console, //
				LspEditorUiMessages.LspEditorPreferencePage_Log_to_Console_description);
	}

}
