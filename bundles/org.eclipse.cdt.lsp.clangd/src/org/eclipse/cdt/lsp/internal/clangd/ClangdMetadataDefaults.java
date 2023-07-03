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
package org.eclipse.cdt.lsp.internal.clangd;

import java.util.stream.Collectors;

import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.cdt.lsp.internal.clangd.editor.LspEditorUiMessages;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public final class ClangdMetadataDefaults implements ClangdMetadata {

	@Reference
	private ClangdOptions defaults;

	public ClangdMetadataDefaults() {
	}

	@Override
	public PreferenceMetadata<Boolean> preferClangd() {
		return new PreferenceMetadata<>(Boolean.class, //
				"prefer_clangd", //$NON-NLS-1$
				defaults.preferClangd(), //
				LspEditorUiMessages.LspEditorPreferencePage_preferLspEditor,
				LspEditorUiMessages.LspEditorPreferencePage_preferLspEditor_description);
	}

	@Override
	public PreferenceMetadata<String> clangdPath() {
		return new PreferenceMetadata<>(String.class, //
				"clangd_path", //$NON-NLS-1$
				defaults.clangdPath(), "Path", //
				"Path to clangd executable");
	}

	@Override
	public PreferenceMetadata<Boolean> useTidy() {
		return new PreferenceMetadata<>(Boolean.class, //
				"use_tidy", //$NON-NLS-1$
				defaults.useTidy(), //
				"Enable clang-tidy diagnostics", //
				"Enable clang-tidy diagnostics");
	}

	@Override
	public PreferenceMetadata<Boolean> useBackgroundIndex() {
		return new PreferenceMetadata<>(Boolean.class, //
				"background_index", //$NON-NLS-1$
				defaults.useBackgroundIndex(), //
				"Index project code in the background and persist index on disk", //
				"Index project code in the background and persist index on disk.");
	}

	@Override
	public PreferenceMetadata<String> completionStyle() {
		return new PreferenceMetadata<>(String.class, //
				"completion_style", //$NON-NLS-1$
				defaults.completionStyle(), //
				"Completion", //
				"Granularity of code completion suggestions");
	}

	@Override
	public PreferenceMetadata<Boolean> prettyPrint() {
		return new PreferenceMetadata<>(Boolean.class, //
				"pretty_print", //$NON-NLS-1$
				defaults.prettyPrint(), //
				"Pretty-print JSON output", //
				"Pretty-print JSON output");
	}

	@Override
	public PreferenceMetadata<String> queryDriver() {
		return new PreferenceMetadata<>(String.class, //
				"query_driver", //$NON-NLS-1$
				defaults.queryDriver(), "Drivers", //
				"Comma separated list of globs for white-listing gcc-compatible drivers that are safe to execute");
	}

	@Override
	public PreferenceMetadata<String> additionalOptions() {
		return new PreferenceMetadata<>(String.class, //
				"additional_options", //$NON-NLS-1$
				defaults.additionalOptions().stream().collect(Collectors.joining(System.lineSeparator())), //$NON-NLS-1$
				"Additional", //
				"Newline separated list of additional options for clangd");
	}

}
