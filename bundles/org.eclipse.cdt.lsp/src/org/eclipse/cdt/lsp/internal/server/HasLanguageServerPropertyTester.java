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

package org.eclipse.cdt.lsp.internal.server;

import java.io.File;
import java.net.URI;
import java.util.Optional;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.lsp.ExistingResource;
import org.eclipse.cdt.lsp.editor.InitialUri;
import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider2;
import org.eclipse.cdt.lsp.util.LspUtils;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.lsp4e.outline.SymbolsModel.DocumentSymbolWithURI;

public class HasLanguageServerPropertyTester extends PropertyTester {
	private final ICLanguageServerProvider cLanguageServerProvider;
	private final ServiceCaller<InitialUri> initial;
	private final ServiceCaller<IWorkspace> workspace;
	private Optional<IProject> project;

	public HasLanguageServerPropertyTester() {
		this.cLanguageServerProvider = LspPlugin.getDefault().getCLanguageServerProvider();
		this.initial = new ServiceCaller<>(getClass(), InitialUri.class);
		this.workspace = new ServiceCaller<>(getClass(), IWorkspace.class);
		this.project = Optional.empty();
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
			} else if (receiver instanceof DocumentSymbolWithURI) {
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
		fetchProject(uri);
		//FIXME: AF: consider changing signature here from IProject to Object
		var enabled = project.map(cLanguageServerProvider::isEnabledFor) //
				.orElseGet(() -> LspUtils.isFileOpenedInLspEditor(uri));
		// call initialization function:
		if (enabled) {
			init();
		}
		return enabled;
	}

	private void fetchProject(URI uri) {
		workspace.call(w -> project = new ExistingResource(w).apply(uri).map(IResource::getProject));
	}

	private void init() {
		if (project.isPresent() && cLanguageServerProvider instanceof ICLanguageServerProvider2 provider) {
			provider.init(project.get());
		}
	}

}
