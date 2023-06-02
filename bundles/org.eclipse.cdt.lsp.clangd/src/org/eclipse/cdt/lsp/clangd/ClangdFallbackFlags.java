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
package org.eclipse.cdt.lsp.clangd;

import java.net.URI;

/**
 *
 * Retrieves data for https://clangd.llvm.org/extensions#compilation-commands
 *
 */
public interface ClangdFallbackFlags {

	Object getFallbackFlagsFromInitialUri(URI root);

}
