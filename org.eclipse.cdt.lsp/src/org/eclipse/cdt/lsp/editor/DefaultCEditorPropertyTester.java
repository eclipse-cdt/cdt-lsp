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

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;

public class DefaultCEditorPropertyTester extends AbstractCEditorPropertyTester {

	@Override
	public boolean useLanguageServerEditor(IEditorInput editorInput) {
		return false;
	}

	/**
	 * Test whether a language server shall be enabled for the given resource or editor in receiver.
	 * This can be necessary when there is more than one editor for a content type.
	 * The receiver can be an instance of {@link IEditorInput}, {@link IFile}, {@link IDocument} or String representing the editor id.
	 * TODO: Requires the LSP4E PR #400: add resource to be opened to evaluate method to provide info for tester
	 * {@link https://github.com/eclipse/lsp4e/pull/400}
	 * This test is necessary to ensure that only certain files will enable the language server for the given content type.
	 * E.g. only files from a new C/C++ project nature shall be opened by the LSP based editor.
	 * All other files with the same content type shall not enable the language server.
	 * TODO: Check whether a simple test for the activeEditor variable using <with> can be appropriate in the contetTypeMapping -> enabledWhen statement in the
	 * plugin.xml.
	 * First tests leads to the assumption that there can be race conditions, when the value of the activeEditor is not updated prior to its access in the test.
	 * The solution in LSP4E PR #400 will eliminate that problem by checking the resource to be opened and not the editor.
	 * The race conditions occurred when switching from a LSP C-editor tab to a CDT C-Editor tab. The language server has been triggered for the old editor as
	 * well.
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		return false;
	}

}
