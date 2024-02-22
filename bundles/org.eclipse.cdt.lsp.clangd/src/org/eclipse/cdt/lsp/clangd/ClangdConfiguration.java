/*******************************************************************************
 * Copyright (c) 2023 ArSysOp.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.clangd;

import java.net.URI;
import java.util.List;

import org.eclipse.cdt.lsp.config.Configuration;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

/**
 * Provides access to the clangd options according to the required scope
 *
 * @see ClangdOptions
 * @see IScopeContext
 *
 */
public interface ClangdConfiguration extends Configuration {

	/**
	 * Provides list of commands suitable for {@link ProcessStreamConnectionProvider} for the given context like {@link IResource} or {@link URI}, must not return <code>null</code>
	 * @param context to be adapter to the proper scope
	 *
	 * @return list of commands
	 */
	List<String> commands(Object context);

}
