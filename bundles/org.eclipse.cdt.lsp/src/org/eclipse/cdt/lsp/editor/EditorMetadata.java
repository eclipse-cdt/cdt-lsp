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

import org.eclipse.core.runtime.preferences.PreferenceMetadata;

public interface EditorMetadata {

	/**
	 * Returns the metadata for the "Prefer C/C++ Editor (LSP)" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Prefer C/C++ Editor (LSP)" option
	 *
	 * @see EditorOptions#preferLspEditor()
	 */
	PreferenceMetadata<Boolean> preferLspEditor();

}
