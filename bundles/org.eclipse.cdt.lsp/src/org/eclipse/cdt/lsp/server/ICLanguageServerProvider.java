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

package org.eclipse.cdt.lsp.server;

import java.net.URI;
import java.util.List;

import org.eclipse.core.resources.IProject;

public interface ICLanguageServerProvider {

	/**
	 * The command list includes the location of the language server followed by its calling arguments.
	 *
	 * @return Command list to run language server
	 */
	public List<String> getCommands();

	/**
	 * The command list includes the location of the language server followed by its calling arguments.
	 *
	 * @param commands
	 */
	public void setCommands(List<String> commands);

	/**
	 * Optional initialization options for the language server during. Will be put in the initialize jsonrpc call.
	 *
	 * @param rootUri
	 * @return
	 */
	public default Object getInitializationOptions(URI rootUri) {
		return null;
	}

	/**
	 * Check whether the LSP based C/C++ Editor and the language server shall be used for the given project.
	 *
	 * @param project
	 * @return true if LSP based C/C++ Editor and language server shall be enabled for the given project, otherwise false.
	 */
	public boolean isEnabledFor(IProject project);

}
