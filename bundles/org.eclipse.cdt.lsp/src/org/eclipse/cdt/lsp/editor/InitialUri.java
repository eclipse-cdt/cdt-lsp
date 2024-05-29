/*******************************************************************************
 * Copyright (c) 2023 ArSysOp and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Alexander Fedorov (ArSysOp) - initial API
 *******************************************************************************/
package org.eclipse.cdt.lsp.editor;

import java.net.URI;
import java.util.Optional;

/**
 * Access to initial URI used determine the compiler flags
 * for the clangd fallback settings on Windows machines
 *
 */
public interface InitialUri {

	Optional<URI> find(URI root);

	void register(URI requested);

}
