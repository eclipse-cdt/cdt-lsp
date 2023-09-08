/*******************************************************************************
 * Copyright (c) 2023 COSEDA Technologies GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Dominic Scharfe (COSEDA Technologies GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.examples.preferences;

import java.util.List;

import org.eclipse.cdt.lsp.clangd.BuiltinClangdOptionsDefaults;
import org.eclipse.cdt.lsp.clangd.ClangdOptionsDefaults;
import org.osgi.service.component.annotations.Component;

@Component(service = ClangdOptionsDefaults.class, property = { "service.ranking:Integer=100" })
public class MyClangdOptionsDefaults extends BuiltinClangdOptionsDefaults {

	@Override
	public List<String> additionalOptions() {
		return List.of("--header-insertion=never", "--default-config");
	}

}
