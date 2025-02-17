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

import java.util.function.Supplier;

/**
 * @since 3.0
 */
public final class ClangdContentAssistQualifier implements Supplier<String> {

	@Override
	public String get() {
		return "org.eclipse.cdt.lsp.clangd.content.assist"; //$NON-NLS-1$
	}

}
