/*******************************************************************************
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.editor;

import org.eclipse.cdt.lsp.PreferredOptions;
import org.eclipse.cdt.lsp.editor.EditorMetadata;
import org.eclipse.cdt.lsp.editor.EditorOptions;
import org.eclipse.cdt.lsp.editor.LanguageServerEnable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IScopeContext;

public final class EditorPreferredOptions extends PreferredOptions implements EditorOptions, LanguageServerEnable {
	private final LanguageServerEnable enable;

	public EditorPreferredOptions(EditorMetadata metadata, String qualifier, IScopeContext[] scopes,
			LanguageServerEnable enable) {
		super(metadata, qualifier, scopes);
		this.enable = enable;
	}

	@Override
	public boolean preferLspEditor() {
		return booleanValue(EditorMetadata.Predefined.preferLspEditor);
	}

	@Override
	public boolean showTryLspBanner() {
		return booleanValue(EditorMetadata.Predefined.showTryLspBanner);
	}

	@Override
	public boolean formatOnSave() {
		return booleanValue(EditorMetadata.Predefined.formatOnSave);
	}

	@Override
	public boolean formatAllLines() {
		return booleanValue(EditorMetadata.Predefined.formatAllLines);
	}

	@Override
	public boolean formatEditedLines() {
		return booleanValue(EditorMetadata.Predefined.formatEditedLines);
	}

	@Override
	public boolean isEnabledFor(IProject project) {
		if (enable != null) {
			return enable.isEnabledFor(project);
		}
		return booleanValue(EditorMetadata.Predefined.preferLspEditor);
	}

}
