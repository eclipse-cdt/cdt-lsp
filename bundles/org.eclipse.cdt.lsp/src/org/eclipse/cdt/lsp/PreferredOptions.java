/*******************************************************************************
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.cdt.lsp.config.ConfigurationMetadata;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;

public abstract class PreferredOptions {
	/**
	 * @since 3.0
	 */
	protected final ConfigurationMetadata metadata;
	protected final String qualifier;
	protected final IScopeContext[] scopes;

	/**
	 *
	 * @param metadata
	 * @param qualifier
	 * @param scopes
	 *
	 * @since 3.0
	 */
	public PreferredOptions(ConfigurationMetadata metadata, String qualifier, IScopeContext[] scopes) {
		this.metadata = Objects.requireNonNull(metadata);
		this.qualifier = Objects.requireNonNull(qualifier);
		this.scopes = Objects.requireNonNull(scopes);
	}

	private String commonValue(PreferenceMetadata<?> meta) {
		String actual = String.valueOf(meta.defaultValue());
		for (int i = scopes.length - 1; i >= 0; i--) {
			IScopeContext scope = scopes[i];
			String previous = actual;
			actual = scope.getNode(qualifier).get(meta.identifer(), previous);
		}
		return actual;
	}

	protected final boolean booleanValue(PreferenceMetadata<Boolean> predefined) {
		PreferenceMetadata<Boolean> customized = metadata.defined(predefined.identifer(), Boolean.class)
				.orElse(predefined);
		return Optional.of(customized)//
				.map(this::commonValue)//
				.map(Boolean::valueOf)//
				.orElseGet(customized::defaultValue);
	}

	protected final String stringValue(PreferenceMetadata<String> predefined) {
		PreferenceMetadata<String> customized = metadata.defined(predefined.identifer(), String.class)
				.orElse(predefined);
		return Optional.of(customized)//
				.map(this::commonValue)//
				.orElseGet(customized::defaultValue);
	}

}
