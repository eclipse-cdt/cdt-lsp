/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.server;

import java.io.OutputStream;

/**
 * @since 2.2
 */
public interface ILogProvider {

	/**
	 * Provides an output stream for the language servers log output.
	 * This could be a console stream.
	 *
	 * @return OutputStream for language server log messages
	 */
	OutputStream getOutputStream();

	/**
	 * Close all open streams. Will  be called when the language server process has been terminated.
	 */
	void close();
}
