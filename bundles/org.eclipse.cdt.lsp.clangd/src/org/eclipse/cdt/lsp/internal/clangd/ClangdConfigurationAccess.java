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
package org.eclipse.cdt.lsp.internal.clangd;

import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdOptions;
import org.eclipse.cdt.lsp.clangd.ClangdQualifier;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IPreferenceMetadataStore;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.OsgiPreferenceMetadataStore;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public final class ClangdConfigurationAccess implements ClangdConfiguration {

	private final ClangdMetadata metadata;
	private final String qualifier;

	@Reference
	private IWorkspace workspace;

	@Reference
	private IPreferencesService preferences;

	public ClangdConfigurationAccess() {
		this.qualifier = new ClangdQualifier().get();
		this.metadata = new ClangdMetadataDefaults();
	}

	@Override
	public ClangdMetadata metadata() {
		return metadata;
	}

	@Override
	public ClangdOptions options(Object context) {
		Optional<ProjectScope> project = projectScope(context);
		IScopeContext[] scopes;
		if (project.isPresent()) {
			scopes = new IScopeContext[] { project.get(), InstanceScope.INSTANCE };
		} else {
			scopes = new IScopeContext[] { InstanceScope.INSTANCE };
		}
		return new ClangdPreferredOptions(preferences, qualifier, scopes, metadata);
	}

	@Override
	public IPreferenceMetadataStore storage(Object context) {
		return new OsgiPreferenceMetadataStore(//
				preferences(//
						projectScope(context)//
								.map(IScopeContext.class::cast)//
								.orElse(InstanceScope.INSTANCE)));
	}

	@Override
	public String qualifier() {
		return qualifier;
	}

	private Optional<ProjectScope> projectScope(Object context) {
		return new ResolveProjectScope(workspace).apply(context);
	}

	private IEclipsePreferences preferences(IScopeContext scope) {
		return Optional.ofNullable(scope.getNode(qualifier))//
				.filter(IEclipsePreferences.class::isInstance)//
				.map(IEclipsePreferences.class::cast)//
				.orElseThrow(() -> new IllegalStateException(//
						NLS.bind("Unable to get preferences for node: {0}", //
								qualifier)));
	}

}
