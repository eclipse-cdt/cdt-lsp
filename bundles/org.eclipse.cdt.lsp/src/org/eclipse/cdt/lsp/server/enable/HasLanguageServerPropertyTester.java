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
 * Alexander Fedorov (ArSysOp) - use OSGi services
 * Alexander Fedorov (ArSysOp) - rework access to preferences
 *******************************************************************************/

package org.eclipse.cdt.lsp.server.enable;

import java.io.File;
import java.net.URI;
import java.util.Optional;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.lsp.InitialUri;
import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.LspUtils;
import org.eclipse.cdt.lsp.UriResource;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.lsp4e.outline.SymbolsModel.DocumentSymbolWithFile;

public class HasLanguageServerPropertyTester extends PropertyTester {
	private final ICLanguageServerProvider cLanguageServerProvider;
	private final ServiceCaller<InitialUri> initial;
	private final ServiceCaller<IWorkspace> workspace;

	public HasLanguageServerPropertyTester() {
		this.cLanguageServerProvider = LspPlugin.getDefault().getCLanguageServerProvider();
		this.initial = new ServiceCaller<>(getClass(), InitialUri.class);
		this.workspace = new ServiceCaller<>(getClass(), IWorkspace.class);
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (cLanguageServerProvider != null) {
			if (receiver instanceof URI) {
				// called from the language server enabler for LSP4E:
				var uri = (URI) receiver;
				if (!validContentType(uri))
					return false;
				// when getProject is empty, it's an external file: Check if the file is already opened, if not check the active editor:
				var isEnabled = enabledFor(uri);
				if (isEnabled) {
					initial.call(iu -> iu.register(uri));
				}
				return isEnabled;
			} else if (receiver instanceof ITranslationUnit) {
				// called to enable the LS based CSymbolsContentProvider:
				return Optional.of((ITranslationUnit) receiver).map(ITranslationUnit::getCProject)
						.map(ICProject::getProject).map(cLanguageServerProvider::isEnabledFor).orElse(Boolean.FALSE);
			} else if (receiver instanceof DocumentSymbolWithFile) {
				// called to enable the LS based CSymbolsContentProvider:
				return true;
			}
			return false;
		}
		return false;
	}

	private boolean validContentType(URI uri) {
		var contentType = Platform.getContentTypeManager().findContentTypeFor(new File(uri.getPath()).getName());
		if (contentType != null) {
			return LspUtils.isCContentType(contentType.getId());
		}
		return false;
	}

	private boolean enabledFor(URI uri) {
		boolean[] provider = new boolean[1];
		workspace.call(w -> provider[0] = new UriResource(w).apply(uri)//
				.map(IResource::getProject)//
				//FIXME: AF: consider changing signature here from IProject to Object
				.map(cLanguageServerProvider::isEnabledFor)//
				.orElseGet(() -> LspUtils.isFileOpenedInLspEditor(uri)));
		return provider[0];
	}

}
