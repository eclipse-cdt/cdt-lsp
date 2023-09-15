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

public interface EditorOptions {

	/**
	 * Prefer to use LSP based C/C++ Editor
	 *
	 * @return if LSP based C/C++ Editor should be preferred
	 */
	boolean preferLspEditor();

}
