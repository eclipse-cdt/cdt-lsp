/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp;

import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;

public class ClangdStreamConnectionProvider extends ProcessStreamConnectionProvider implements StreamConnectionProvider {

	public ClangdStreamConnectionProvider() {
		var commandProvider = new CLanguageServerRegistry().createCLanguageServerCommandProvider();
		if (commandProvider == null)
			return;
		setCommands(commandProvider.getCommands());
		// set the working directory for the Java process which runs clangd:
		setWorkingDirectory(System.getProperty("user.dir"));
	}

}
