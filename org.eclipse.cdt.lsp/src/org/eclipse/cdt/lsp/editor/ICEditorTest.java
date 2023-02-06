/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.editor;

import org.eclipse.ui.IEditorInput;

public interface ICEditorTest {

	/**
	 * Checks whether the editorInput shall be opened by the LSP based editor
	 * 
	 * @param editorInput
	 * @return true if editorInput shall be opened by the LSP based C/C++ Editor
	 */
	public boolean useLanguageServerEditor(IEditorInput editorInput);

}
