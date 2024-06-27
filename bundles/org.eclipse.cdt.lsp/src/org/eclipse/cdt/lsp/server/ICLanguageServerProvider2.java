/*******************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import org.eclipse.core.resources.IProject;

/**
 * @since 3.0.0
 */
public interface ICLanguageServerProvider2 extends ICLanguageServerProvider {

	/**
	 * The init function gets called always prior a file from the given project gets opened.
	 * @param project
	 */
	public void preFileOpening(IProject project);

}
