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

import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;

public class CLanguageServerStreamConnectionProvider extends ProcessStreamConnectionProvider
		implements StreamConnectionProvider {

	public CLanguageServerStreamConnectionProvider() {
		var cLanguageServerProvider = LspPlugin.getDefault().getCLanguageServerProvider();
		setCommands(cLanguageServerProvider.getCommands());

		// set the working directory for the Java process which runs the C/C++ language server:
		setWorkingDirectory(System.getProperty("user.dir"));
	}

}
