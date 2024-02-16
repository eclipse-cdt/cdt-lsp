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

package org.eclipse.cdt.lsp.internal.messages;

import org.eclipse.osgi.util.NLS;

public class LspUiMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.lsp.internal.messages.LspUiMessages"; //$NON-NLS-1$

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, LspUiMessages.class);
	}

	public static String NavigatorView_ErrorOnLoad;

	public static String LspEditorConfigurationPage_spelling_link;
	public static String LspEditorConfigurationPage_spelling_link_tooltip;

	public static String LspEditorConfigurationPage_enable_project_specific;
	public static String LspEditorConfigurationPage_configure_ws_specific;
	public static String LspEditorConfigurationPage_preferLspEditor;
	public static String LspEditorConfigurationPage_preferLspEditor_description;

	public static String SaveActionsConfigurationPage_FormatSourceCode;
	public static String SaveActionsConfigurationPage_FormatSourceCode_description;
	public static String SaveActionsConfigurationPage_FormatAllLines;
	public static String SaveActionsConfigurationPage_FormatAllLines_description;
	public static String SaveActionsConfigurationPage_FormatEditedLines;
	public static String SaveActionsConfigurationPage_FormatEditedLines_description;

}
