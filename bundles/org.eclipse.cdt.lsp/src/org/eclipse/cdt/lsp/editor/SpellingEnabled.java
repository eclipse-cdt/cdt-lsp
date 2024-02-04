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

package org.eclipse.cdt.lsp.editor;

import java.util.Optional;

import org.eclipse.cdt.lsp.LspUtils;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.spelling.SpellingService;

public class SpellingEnabled extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof TextEditor editor) {
			var id = Optional.of(editor.getEditorInput()).map(i -> getFile(i)).map(f -> {
				try {
					return f.getContentDescription();
				} catch (CoreException e) {
					// do nothing
				}
				return null;
			}).map(cd -> cd.getContentType()).map(ct -> ct.getId()).orElse(""); //$NON-NLS-1$
			return isSpellingEnabled() && LspUtils.isCContentType(id);
		}
		return false;
	}

	private IFile getFile(IEditorInput editorInput) {
		if (editorInput instanceof IFileEditorInput fileEditorInput) {
			return fileEditorInput.getFile();
		}
		return null;
	}

	private boolean isSpellingEnabled() {
		return EditorsUI.getPreferenceStore().getBoolean(SpellingService.PREFERENCE_SPELLING_ENABLED);
	}

}
