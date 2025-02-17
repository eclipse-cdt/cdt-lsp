/*******************************************************************************
 * Copyright (c) 2025 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.switchtolsp;

import org.eclipse.cdt.lsp.internal.editor.CLspEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This interface can be implemented by CDT-LSP to display a banner in the {@link CLspEditor}
 * to switch back to the traditional C/C++ editing experience
 *
 * This interface is not public API
 *
 * @apiNote this interface, at time of initial implementation, is expected to work
 * with CDT 11.6 and CDT 12. There should be no requirements on API
 * added in CDT 12 (specifically ISwitchToLsp)
 *
 * See org.eclipse.cdt.internal.ui.switchtolsp.ISwitchToLsp
 */
public interface ISwitchBackToTraditional {

	/**
	 * Create the banner controls for the "switch back to traditional"
	 *
	 * @param part the editor part that the banner is added on
	 * @param parent the parent control
	 * @return the new parent control the editor should use
	 */
	public Composite createSwitchBackToTraditional(ITextEditor part, Composite parent);

}
