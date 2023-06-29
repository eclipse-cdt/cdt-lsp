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

import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.internal.clangd.editor.LspEditorUiMessages;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

final class ClangdMetadataDefaults implements ClangdMetadata {

	@Override
	public PreferenceMetadata<Boolean> preferClangd() {
		return new PreferenceMetadata<>(Boolean.class, //
				"prefer_clangd", //$NON-NLS-1$
				false, //
				LspEditorUiMessages.LspEditorPreferencePage_preferLspEditor,
				LspEditorUiMessages.LspEditorPreferencePage_preferLspEditor_description);
	}

	@Override
	public PreferenceMetadata<String> clangdPath() {
		return new PreferenceMetadata<>(String.class, //
				"clangd_path", //$NON-NLS-1$
				Optional.ofNullable(PathUtil.findProgramLocation("clangd", null)) //$NON-NLS-1$
						.map(IPath::toOSString)//
						.orElse("clangd"), //  //$NON-NLS-1$
				"Path", //
				"Path to clangd executable");
	}

	@Override
	public PreferenceMetadata<Boolean> useTidy() {
		return new PreferenceMetadata<>(Boolean.class, //
				"use_tidy", //$NON-NLS-1$
				true, //
				"Enable clang-tidy diagnostics", //
				"Enable clang-tidy diagnostics");
	}

	@Override
	public PreferenceMetadata<Boolean> useBackgroundIndex() {
		return new PreferenceMetadata<>(Boolean.class, //
				"background_index", //$NON-NLS-1$
				true, //
				"Index project code in the background and persist index on disk", //
				"Index project code in the background and persist index on disk.");
	}

	@Override
	public PreferenceMetadata<String> completionStyle() {
		return new PreferenceMetadata<>(String.class, //
				"completion_style", //$NON-NLS-1$
				"detailed", //
				"Completion", //
				"Granularity of code completion suggestions");
	}

	@Override
	public PreferenceMetadata<Boolean> prettyPrint() {
		return new PreferenceMetadata<>(Boolean.class, //
				"pretty_print", //$NON-NLS-1$
				true, //
				"Pretty-print JSON output", //
				"Pretty-print JSON output");
	}

	@Override
	public PreferenceMetadata<String> queryDriver() {
		return new PreferenceMetadata<>(String.class, //
				"query_driver", //$NON-NLS-1$
				Optional.ofNullable(PathUtil.findProgramLocation("gcc", null)) //$NON-NLS-1$
						.map(p -> p.removeLastSegments(1).append(IPath.SEPARATOR + "*"))// //$NON-NLS-1$
						.map(IPath::toString)//
						.orElse(""), //  //$NON-NLS-1$
				"Drivers", //
				"Comma separated list of globs for white-listing gcc-compatible drivers that are safe to execute");
	}

	@Override
	public PreferenceMetadata<String> customOptions() {
		return new PreferenceMetadata<>(String.class, //
				"custom_options", //$NON-NLS-1$
				"", //  //$NON-NLS-1$
				"Options", //
				"New line separated list of further options for clangd");
	}

}
