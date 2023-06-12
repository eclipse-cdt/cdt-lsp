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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IPreferenceMetadataStore;
import org.eclipse.core.runtime.preferences.IScopeContext;

/**
 * Provides access to the clangd options according to the required scope
 *
 * @see ClangdOptions
 * @see IScopeContext
 *
 */
public interface ClangdConfiguration {

	/**
	 * Returns the clangd options for the given context like {@link IResource} or {@link URI}, must not return <code>null</code>
	 * @param context to be adapter to the proper scope
	 *
	 * @return clangd options
	 */
	ClangdOptions options(Object context);

	/**
	 * Returns the clangd preference store for the given context like {@link IResource} or {@link URI}, must not return <code>null</code>
	 * @param context to be adapter to the proper scope
	 *
	 * @return preference store
	 */
	IPreferenceMetadataStore storage(Object context);

	/**
	 * Return the metadata for clangd options, must not return <code>null</code>
	 *
	 * @return the clangd option metadata
	 */
	ClangdMetadata metadata();

	/**
	 * Default qualifier to use for preference storage
	 * @return preference qualifier
	 */
	String qualifier();

}
