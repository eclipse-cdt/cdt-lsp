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

package org.eclipse.cdt.lsp.clangd;

import java.util.List;

import org.eclipse.core.runtime.IStatus;

/**
 * Validator interface for clangd command line options.
 *
 * @since 2.2
 */
public interface IClangdCommandLineValidator {

	/**
	 * Checks if the command line validation is supported.
	 * @param clangdBinaryPath absolute path to clangd binary
	 * @return IStatus.OK if validation is supported for the given clangd binary, otherwise IStatus.CANCEL
	 */
	public IStatus supportsValidation(final String clangdBinaryPath);

	/**
	 * Validates the clangd command line options prior to a LS start. Prevents LS from being started if returned IStatus is not OK.
	 * @param commands the language servers command line options to be validated
	 * @return validation status.
	 */
	public IStatus validateCommandLineOptions(final List<String> commands);

}
