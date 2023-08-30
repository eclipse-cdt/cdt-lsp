/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.clangd.editor;

import org.eclipse.cdt.lsp.clangd.ClangdConfigurationVisibility;
import org.osgi.service.component.annotations.Component;

@Component(property = { "service.ranking:Integer=0" })
public final class DefaultClangdConfigurationVisibility implements ClangdConfigurationVisibility {

	@Override
	public boolean showPreferClangd(boolean isProjectScope) {
		return true;
	}

}
