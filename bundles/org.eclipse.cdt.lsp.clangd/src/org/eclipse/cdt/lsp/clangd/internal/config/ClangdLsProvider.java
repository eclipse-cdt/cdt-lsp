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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.internal.switchtolsp.ILsProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class ClangdLsProvider implements ILsProvider {

	@Reference
	ClangdConfiguration configuration;

	@Override
	public String getLsPath(Object context) {
		List<String> result = new ArrayList<>();
		Optional.ofNullable(configuration).map(c -> result.addAll(c.commands(context).stream()
				.map(ClangdLanguageServerProvider::resolveVariables).collect(Collectors.toList())));
		return result.isEmpty() ? "" : result.get(0); //$NON-NLS-1$
	}

}
