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
 * Alexander Fedorov (ArSysOp) - rework to OSGi component
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.eclipse.cdt.lsp.InitialUri;
import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.ExistingResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public final class InitialFileManager implements InitialUri {

	private static final QualifiedName INITIAL_URI = new QualifiedName(LspPlugin.PLUGIN_ID, "initialUri"); //$NON-NLS-1$
	private URI uri;

	@Reference
	private IWorkspace workspace;

	@Override
	public synchronized void register(URI uri) {
		if (this.uri == null && new ExistingResource(workspace).apply(uri).isPresent()) {
			try {
				workspace.getRoot().setPersistentProperty(INITIAL_URI, uri.toString());
				this.uri = uri;
			} catch (CoreException e) {
				Platform.getLog(InitialFileManager.class).error(e.getMessage(), e);
			}
		}
	}

	@Override
	public Optional<URI> find(URI root) {
		if (this.uri == null) {
			String initialUriString = null;
			try {
				initialUriString = workspace.getRoot().getPersistentProperty(INITIAL_URI);
				if (initialUriString != null) {
					this.uri = new URI(initialUriString);
				}
			} catch (CoreException | URISyntaxException e) {
				Platform.getLog(InitialFileManager.class).error(e.getMessage(), e);
			}
		}
		return Optional.ofNullable(this.uri);
	}
}
