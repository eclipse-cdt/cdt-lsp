/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/
package org.eclipse.cdt.lsp.clangd;

import org.eclipse.cdt.lsp.clangd.internal.ui.LspEditorUiMessages;
import org.eclipse.cdt.lsp.config.ConfigurationMetadata;
import org.eclipse.cdt.lsp.editor.EditorOptions;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

/**
 * @since 3.0
 */
public interface ClangdContentAssistMetadata extends ConfigurationMetadata {

	/**
	 * Returns the metadata for the "Fill function arguments and show guessed arguments" option.
	 *
	 * @see EditorOptions#fillFunctionArguments()
	 *
	 * @since 3.0
	 */
	PreferenceMetadata<Boolean> fillFunctionArguments = new PreferenceMetadata<>(Boolean.class, //
			"fill_function_arguments", //$NON-NLS-1$
			true, //
			LspEditorUiMessages.ContentAssistConfigurationPage_fill_function_arguments,
			LspEditorUiMessages.ContentAssistConfigurationPage_fill_function_arguments_description);

}
