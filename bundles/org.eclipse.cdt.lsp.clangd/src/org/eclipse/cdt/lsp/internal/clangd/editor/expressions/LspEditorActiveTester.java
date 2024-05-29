/*******************************************************************************
 * Copyright (c) 2023 COSEDA Technologies GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Dominic Scharfe (COSEDA Technologies GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.clangd.editor.expressions;

import java.util.Optional;

import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Property tester for checking if a receiver is a c editor. Will evaluate
 * {@code true} if the receiver can adapt to {@link ITextEditor} and has the c
 * editor id.
 */
public class LspEditorActiveTester extends PropertyTester {
	public final static String IS_LSP_CEDITOR_PROPERTY = "active";

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof IEditorPart && IS_LSP_CEDITOR_PROPERTY.equals(property)) {
			IEditorPart innerEditor = Optional.ofNullable((IEditorPart) Adapters.adapt(receiver, ITextEditor.class))
					.orElse((IEditorPart) receiver);

			return LspPlugin.LSP_C_EDITOR_ID.equals(innerEditor.getSite().getId());
		}
		return false;
	}
}
