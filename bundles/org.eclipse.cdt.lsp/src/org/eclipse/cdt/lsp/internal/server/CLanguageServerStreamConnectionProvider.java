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

package org.eclipse.cdt.lsp.internal.server;

import java.net.URI;
import java.util.Optional;

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

public class CLanguageServerStreamConnectionProvider extends ProcessStreamConnectionProvider {
	private final ICLanguageServerProvider cLanguageServerProvider;

	public CLanguageServerStreamConnectionProvider() {
		this.cLanguageServerProvider = LspPlugin.getDefault().getCLanguageServerProvider();
		Optional.ofNullable(cLanguageServerProvider).ifPresent(p -> setCommands(p.getCommands()));

		// set the working directory for the Java process which runs the C/C++ language server:
		setWorkingDirectory(System.getProperty("user.dir")); //$NON-NLS-1$
	}

	@Override
	public Object getInitializationOptions(URI rootUri) {
		return Optional.ofNullable(cLanguageServerProvider).map(p -> p.getInitializationOptions(rootUri)).orElse(null);
	}

}
