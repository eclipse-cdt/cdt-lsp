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
 * Alexander Fedorov (ArSysOp) - rework access to preferences
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.server;

import java.net.URI;

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

public final class CLanguageServerStreamConnectionProvider extends ProcessStreamConnectionProvider {

	private final ICLanguageServerProvider provider;

	public CLanguageServerStreamConnectionProvider() {
		this.provider = LspPlugin.getDefault().getCLanguageServerProvider();
		// set the working directory for the Java process which runs the C/C++ language server:
		setWorkingDirectory(System.getProperty("user.dir")); //$NON-NLS-1$
	}

	@Override
	public Object getInitializationOptions(URI rootUri) {
		setCommands(provider.getCommands(rootUri));
		return provider.getInitializationOptions(rootUri);
	}

}
