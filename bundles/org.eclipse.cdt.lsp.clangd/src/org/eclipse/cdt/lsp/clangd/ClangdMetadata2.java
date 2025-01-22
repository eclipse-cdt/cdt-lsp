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

import org.eclipse.core.runtime.preferences.PreferenceMetadata;

/**
 * @since 2.2
 */
public interface ClangdMetadata2 extends ClangdMetadata {

	/**
	 * Returns the metadata for the "Log to Console" option, must not return <code>null</code>.
	 *
	 * @return the metadata for the "Log to Console" option
	 *
	 * @see ClangdOptions2#logToConsole()
	 */
	PreferenceMetadata<Boolean> logToConsole();

}
