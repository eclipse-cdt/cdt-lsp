/*******************************************************************************
 * Copyright (c) 2025 ArSysOp.
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
package org.eclipse.cdt.lsp.editor;

import org.eclipse.cdt.lsp.config.Configuration;
import org.eclipse.core.runtime.preferences.IScopeContext;

/**
 * Provides access to the editor options according to the required scope
 *
 * @see EditorOptions
 * @see IScopeContext
 *
 * @since 3.0
 */
public interface EditorConfiguration extends Configuration {

	@Override
	EditorOptions defaults();

	@Override
	EditorOptions options(Object context);

}
