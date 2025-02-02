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

import org.eclipse.core.runtime.IStatus;

/**
 * @since 2.2
 */
public interface ICLanguageServerCommandLineValidator {

	/**
	 * Validates the language servers command line options prior to a LS start. Prevents LS from being started if returned IStatus is not OK.
	 * @return validation status.
	 */
	public IStatus validateCommandLineOptions();

}
