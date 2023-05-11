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

package org.eclipse.cdt.lsp.editor.ui.preference;

import org.eclipse.cdt.lsp.editor.ui.LspEditorUiPlugin;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

public class LspEditorPreferencesTester extends PropertyTester {
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IProject) {
			return preferLspEditor((IProject) receiver);
		}
		return false;
	}

	private static boolean preferLspEditor(IProject project) {
		// check project properties:
		PreferenceMetadata<Boolean> option = LspEditorPreferences.getPreferenceMetadata();
		return Platform.getPreferencesService().getBoolean(LspEditorUiPlugin.PLUGIN_ID, option.identifer(),
				option.defaultValue(), new IScopeContext[] { new ProjectScope(project) });
	}

}
