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

package org.eclipse.cdt.lsp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;

public class InitialFileManager {
	private static InitialFileManager initialFileManager = null;
	private static final QualifiedName INITIAL_URI = new QualifiedName(LspPlugin.PLUGIN_ID, "initialUri"); //$NON-NLS-1$
	private final IWorkspaceRoot workspaceRoot;
	private URI uri;

	private InitialFileManager() {
		workspaceRoot = LspPlugin.getDefault().getWorkspace().getRoot();
	}

	public static synchronized InitialFileManager getInstance() {
		if (initialFileManager == null)
			initialFileManager = new InitialFileManager();

		return initialFileManager;
	}

	public synchronized void setInitialUri(URI uri) {
		if (this.uri == null && LspUtils.getProject(uri).isPresent()) {
			try {
				workspaceRoot.setPersistentProperty(INITIAL_URI, uri.toString());
				this.uri = uri;
			} catch (CoreException e) {
				Platform.getLog(InitialFileManager.class).error(e.getMessage(), e);
			}
		}
	}

	public Optional<URI> getInitialUri() {
		if (this.uri == null) {
			String initialUriString = null;
			try {
				initialUriString = workspaceRoot.getPersistentProperty(INITIAL_URI);
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
