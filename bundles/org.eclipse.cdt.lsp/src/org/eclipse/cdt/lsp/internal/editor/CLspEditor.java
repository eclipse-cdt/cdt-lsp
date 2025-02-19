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

package org.eclipse.cdt.lsp.internal.editor;

import org.eclipse.cdt.lsp.internal.switchtolsp.ISwitchBackToTraditional;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextEditor;

@SuppressWarnings("restriction")
public class CLspEditor extends ExtensionBasedTextEditor {

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		Composite editorComposite = createSwitchBackToTraditionalEditorBanner(parent);
		return super.createSourceViewer(editorComposite, ruler, styles);
	}

	/**
	 * Wraps {@link ISwitchBackToTraditional#createSwitchBackToTraditional(org.eclipse.ui.texteditor.ITextEditor, Composite)}
	 * with the needed service access + fallback checks.
	 *
	 * If the {@link ISwitchBackToTraditional} service doesn't exist, or fails, this method
	 * is a no-op that simply returns its input.
	 *
	 * @see ISwitchBackToTraditional#createSwitchBackToTraditional(org.eclipse.ui.texteditor.ITextEditor, Composite)
	 */
	private Composite createSwitchBackToTraditionalEditorBanner(Composite parent) {
		Composite editorComposite = SafeRunner.run(() -> {
			ISwitchBackToTraditional switchToLsp = PlatformUI.getWorkbench().getService(ISwitchBackToTraditional.class);
			if (switchToLsp != null) {
				return switchToLsp.createSwitchBackToTraditional(CLspEditor.this, parent);
			}
			return null;
		});
		if (editorComposite == null) {
			editorComposite = parent;
		}
		return editorComposite;
	}

}
