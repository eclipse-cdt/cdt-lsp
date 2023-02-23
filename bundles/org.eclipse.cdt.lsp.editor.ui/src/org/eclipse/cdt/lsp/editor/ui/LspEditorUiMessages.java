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

package org.eclipse.cdt.lsp.editor.ui;

import org.eclipse.osgi.util.NLS;

public class LspEditorUiMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.lsp.editor.ui.LspEditorUiMessages"; //$NON-NLS-1$
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, LspEditorUiMessages.class);
	}
	
	public static String LspEditorPreferencePage_description;
	public static String LspEditorPreferencePage_preferLspEditor;
	public static String LspEditorPreferencePage_preferLspEditor_description;
	public static String LspEditorPreferencePage_server_options;
	public static String LspEditorPreferencePage_server_path;
	
	public static String LspEditorPropertiesPage_projectSpecificSettings;

}
