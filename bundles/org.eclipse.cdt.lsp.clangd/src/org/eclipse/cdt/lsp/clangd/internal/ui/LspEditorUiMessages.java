/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd.internal.ui;

import org.eclipse.osgi.util.NLS;

public class LspEditorUiMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.lsp.clangd.internal.ui.LspEditorUiMessages"; //$NON-NLS-1$

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, LspEditorUiMessages.class);
	}

	public static String LspEditorPreferencePage_path;
	public static String LspEditorPreferencePage_path_description;
	public static String LspEditorPreferencePage_enable_tidy;
	public static String LspEditorPreferencePage_background_index;
	public static String LspEditorPreferencePage_completion;
	public static String LspEditorPreferencePage_completion_description;
	public static String LspEditorPreferencePage_pretty_print;
	public static String LspEditorPreferencePage_drivers;
	public static String LspEditorPreferencePage_drivers_description;
	public static String LspEditorPreferencePage_additional;
	public static String LspEditorPreferencePage_additional_description;
	public static String LspEditorPreferencePage_browse_button;
	public static String LspEditorPreferencePage_clangd_options_label;
	public static String LspEditorPreferencePage_enable_project_specific;
	public static String LspEditorPreferencePage_configure_ws_specific;
	public static String LspEditorPreferencePage_completion_detailed;
	public static String LspEditorPreferencePage_completion_bundled;
	public static String LspEditorPreferencePage_completion_default;
	public static String LspEditorPreferencePage_select_clangd_executable;
	public static String LspEditorPreferencePage_Log_to_Console;
	public static String LspEditorPreferencePage_Log_to_Console_description;
	public static String LspEditorPreferencePage_Validate_clangd_options;
	public static String LspEditorPreferencePage_Validate_clangd_options_description;

	public static String ClangFormatConfigurationPage_openProjectFormatFile;
	public static String ClangFormatConfigurationPage_openFormatFileTooltip;

}
