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
import java.util.Optional;

import org.eclipse.core.runtime.IPath;

public interface ICompileCommandsDirLocator {
	
	/** 
	 * Path to directory containing the compile_commands.json to be used for the given file URI.
	 * 
	 * @param uri of document to be opened by C/C++ language server
	 * @return Optional path to directory containing the compile_commands.json
	 */
	public Optional<IPath> getCompileCommandsDir(URI uri);
}
