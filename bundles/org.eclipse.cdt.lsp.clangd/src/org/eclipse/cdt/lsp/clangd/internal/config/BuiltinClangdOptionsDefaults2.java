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

package org.eclipse.cdt.lsp.clangd.internal.config;

import org.eclipse.cdt.lsp.clangd.ClangdOptionsDefaults2;
import org.osgi.service.component.annotations.Component;

/**
 * Provides the default clangd extended options for cdt-lsp.
 */
@Component(property = { "service.ranking:Integer=0" })
public class BuiltinClangdOptionsDefaults2 extends BuiltinClangdOptionsDefaults implements ClangdOptionsDefaults2 {

	@Override
	public boolean logToConsole() {
		return false;
	}

}
