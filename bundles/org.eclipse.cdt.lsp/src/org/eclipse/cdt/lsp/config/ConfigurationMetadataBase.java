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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.core.runtime.preferences.PreferenceMetadata;

/**
 * Base implementation of {@link ConfigurationMetadata}
 *
 * @since 3.0
 */
public abstract class ConfigurationMetadataBase implements ConfigurationMetadata {

	private final Map<String, PreferenceMetadata<?>> defined = new LinkedHashMap<>();

	public ConfigurationMetadataBase() {
		definePreferences().forEach(d -> defined.put(d.identifer(), d));
	}

	/**
	 *
	 * @return the list of defined preferences
	 */
	protected abstract List<PreferenceMetadata<?>> definePreferences();

	protected final PreferenceMetadata<Boolean> overrideBoolean(PreferenceMetadata<Boolean> predefined,
			boolean override) {
		return new PreferenceMetadata<>(predefined.valueClass(), predefined.identifer(), //
				override, //
				predefined.name(), predefined.description());
	}

	protected final PreferenceMetadata<String> overrideString(PreferenceMetadata<String> predefined, String override) {
		return new PreferenceMetadata<>(predefined.valueClass(), predefined.identifer(), //
				override, //
				predefined.name(), predefined.description());
	}

	@Override
	public final List<PreferenceMetadata<?>> defined() {
		return List.copyOf(defined.values());
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <V> Optional<PreferenceMetadata<V>> defined(String id, Class<V> type) {
		return Optional.ofNullable(id).map(defined::get).filter(p -> Objects.equals(p.valueClass(), type))
				.map(p -> (PreferenceMetadata<V>) p);
	}

}
