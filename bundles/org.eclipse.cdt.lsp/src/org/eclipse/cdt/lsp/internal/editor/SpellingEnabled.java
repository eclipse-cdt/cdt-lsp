/*******************************************************************************
 * Copyright (c) 2024 Bachmann electronic GmbH and others.
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

package org.eclipse.cdt.lsp.internal.editor;

import java.util.Optional;

import org.eclipse.cdt.lsp.LspUtils;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.spelling.SpellingService;

public final class SpellingEnabled extends PropertyTester {
	private static final String EMPTY = ""; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		return isSpellingEnabled() && isCContentType(receiver);
	}

	private static boolean isSpellingEnabled() {
		return EditorsUI.getPreferenceStore().getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED);
	}

	private static boolean isCContentType(Object receiver) {
		if (receiver instanceof TextEditor editor) {
			return LspUtils.isCContentType(getContentType(editor.getEditorInput()));
		}
		return false;
	}

	private static String getContentType(IEditorInput editorInput) {
		if (editorInput instanceof IFileEditorInput fileEditorInput) {
			try {
				return Optional.ofNullable(fileEditorInput.getFile().getContentDescription())
						.map(cd -> cd.getContentType()).map(ct -> ct.getId()).orElse(EMPTY);
			} catch (CoreException e) {
				// do nothing
			}
		}
		return EMPTY;
	}

}
