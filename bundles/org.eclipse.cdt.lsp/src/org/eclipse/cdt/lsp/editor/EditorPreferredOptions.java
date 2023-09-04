/*******************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.editor;

import java.util.Objects;

import org.eclipse.cdt.lsp.PreferredOptions;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IScopeContext;

public class EditorPreferredOptions extends PreferredOptions implements EditorOptions, LanguageServerEnable {
	private final EditorMetadata metadata;
	private final LanguageServerEnable enable;

	public EditorPreferredOptions(String qualifier, IScopeContext[] scopes, EditorMetadata metadata,
			LanguageServerEnable enable) {
		super(qualifier, scopes);
		this.metadata = Objects.requireNonNull(metadata);
		this.enable = enable;
	}

	@Override
	public boolean preferLspEditor() {
		return booleanValue(metadata.preferLspEditor());
	}

	@Override
	public boolean formatOnSave() {
		return booleanValue(metadata.formatOnSave());
	}

	@Override
	public boolean formatAllLines() {
		return booleanValue(metadata.formatAllLines());
	}

	@Override
	public boolean formatEditedLines() {
		return booleanValue(metadata.formatEditedLines());
	}

	@Override
	public boolean isEnabledFor(IProject project) {
		if (enable != null) {
			return enable.isEnabledFor(project);
		}
		return booleanValue(metadata.preferLspEditor());
	}

}
