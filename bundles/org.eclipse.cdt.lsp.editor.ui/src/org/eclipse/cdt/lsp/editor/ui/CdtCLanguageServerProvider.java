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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.lsp.editor.ui.preference.LspEditorPreferences;
import org.eclipse.cdt.lsp.editor.ui.properties.LspEditorPropertiesPage;
import org.eclipse.cdt.lsp.server.DefaultCLanguageServerProvider;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.jface.preference.IPreferenceStore;


public class CdtCLanguageServerProvider extends DefaultCLanguageServerProvider {
	private static final IPreferenceStore preferenceStore = LspEditorUiPlugin.getDefault().getLsPreferences();

	@Override
	protected List<String> createCommands() {
		List<String> retCommands = new ArrayList<>();
		List<String> defaultCommands = super.createCommands();
		List<String> commandsFromStore = getCommandsFromStore();
		if (!commandsFromStore.isEmpty()) {
			retCommands.add(commandsFromStore.get(0)); //add server path
			if (commandsFromStore.size() > 1) {
				for (int i=1; i<commandsFromStore.size(); i++) {
					retCommands.add(commandsFromStore.get(i));
				}
			} else {
				for (int i=1; i<defaultCommands.size(); i++) {
					retCommands.add(defaultCommands.get(i));
				}				
			}
			return retCommands;
		}
		return defaultCommands;
	}
	
	private List<String> getCommandsFromStore(){
		List<String> commands = new ArrayList<>();
		String serverPath = preferenceStore.getString(LspEditorPreferences.SERVER_PATH);
		if (serverPath.isBlank()) {
			return commands;
		}
		commands.add(serverPath);
		String options = preferenceStore.getString(LspEditorPreferences.SERVER_OPTIONS);
		if (options.isBlank()) {
			return commands;
		}
		commands.addAll(Arrays.asList(CommandLineUtil.argumentsToArray(options)));
		return commands;
	}
}
