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

/**
 * @since 3.0
 */
public interface ICLanguageServerProvider3 extends ICLanguageServerProvider {

	/**
	 * Enable logging to console
	 * @return true if language server log shall be written to console
	 */
	public boolean logToConsole();
}
