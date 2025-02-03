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
 *     Alexander Fedorov (ArSysOp) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.config;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.preferences.PreferenceMetadata;

/**
 * Provider interface to customize defined preferences and its defaults
 *
 * @see ConfigurationMetadataBase
 *
 * @since 3.0
 */
public interface ConfigurationMetadata {

	/**
	 *
	 * @return the {@link List} of defined preferences
	 */
	List<PreferenceMetadata<?>> defined();

	/**
	 *
	 * @param <V> the value type for the preference
	 * @param id the preference identifier
	 * @param type the {@link Class} representing type for the preference
	 * @return the {@link Optional} with {@link PreferenceMetadata} or {@link Optional#empty()} if not defined
	 */
	<V> Optional<PreferenceMetadata<V>> defined(String id, Class<V> type);

}
