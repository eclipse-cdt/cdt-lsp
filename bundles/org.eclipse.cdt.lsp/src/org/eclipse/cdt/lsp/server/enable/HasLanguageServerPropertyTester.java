/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.server.enable;

import java.net.URI;
import java.util.Optional;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.LspUtils;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.lsp4e.outline.SymbolsModel.DocumentSymbolWithFile;

public class HasLanguageServerPropertyTester extends PropertyTester {
	private final ICLanguageServerProvider cLanguageServerProvider;

	public HasLanguageServerPropertyTester() {
		cLanguageServerProvider = LspPlugin.getDefault().getCLanguageServerProvider();
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (cLanguageServerProvider != null) {
			if (receiver instanceof URI) {
				// called from the language server enabler for LSP4E:
				var uri = (URI) receiver;
				// when getProject is empty, it's an external file: Check if the file is already opened, if not check the active editor:
				return getProject(uri).map(cLanguageServerProvider::isEnabledFor).orElse(LspUtils.isFileOpenedInLspEditor(uri));
			} else if (receiver instanceof ITranslationUnit) {
				// called to enable the LS based CSymbolsContentProvider: 
				return Optional.of((ITranslationUnit) receiver)
						 .map(ITranslationUnit::getCProject)
						 .map(ICProject::getProject)
						 .map(cLanguageServerProvider::isEnabledFor)
						 .orElse(Boolean.FALSE);
			} else if (receiver instanceof DocumentSymbolWithFile) {
				// called to enable the LS based CSymbolsContentProvider:
				return true;
			}
			return false;
		}
		return false;
	}
	
	private Optional<IProject> getProject(URI uri) {
		IFile[] files = LspPlugin.getDefault().getWorkspace().getRoot().findFilesForLocationURI(uri);
		if (files.length > 0) {	
			return Optional.ofNullable(files[0].getProject());
		}
		return Optional.empty();	
	}

}
