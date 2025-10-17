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

package org.eclipse.cdt.lsp.internal.switchtolsp;

public interface ILsProvider {

	/**
	 * Get the language server executable path.
	 *
	 * @param context the file/project to get the path for, or {@code null} for the default
	 * @return the language server executable path or an empty string
	 */
	String getLsPath(Object context);

}
