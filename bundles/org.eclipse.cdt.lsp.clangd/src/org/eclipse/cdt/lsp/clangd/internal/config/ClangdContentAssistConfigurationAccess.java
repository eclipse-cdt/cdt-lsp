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

import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.ClangdContentAssistConfiguration;
import org.eclipse.cdt.lsp.clangd.ClangdContentAssistMetadata;
import org.eclipse.cdt.lsp.clangd.ClangdContentAssistOptions;
import org.eclipse.cdt.lsp.clangd.ClangdContentAssistQualifier;
import org.eclipse.cdt.lsp.config.ConfigurationAccess;
import org.eclipse.cdt.lsp.config.ConfigurationMetadata;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IPreferenceMetadataStore;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.OsgiPreferenceMetadataStore;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public final class ClangdContentAssistConfigurationAccess extends ConfigurationAccess
		implements ClangdContentAssistConfiguration {

	@Reference
	private ClangdContentAssistMetadata metadata;

	@Reference
	private IWorkspace workspace;

	public ClangdContentAssistConfigurationAccess() {
		super(new ClangdContentAssistQualifier().get());
	}

	@Override
	public IPreferenceMetadataStore storage(Object context) {
		return new OsgiPreferenceMetadataStore(//
				preferences(//
						projectScope(workspace, context)//
								.map(IScopeContext.class::cast)//
								.orElse(InstanceScope.INSTANCE)));
	}

	@Override
	public ConfigurationMetadata metadata() {
		return metadata;
	}

	@Override
	public String qualifier() {
		return qualifier;
	}

	@Override
	public ClangdContentAssistOptions defaults() {
		return new ClangdContentAssistPreferredOptions(metadata, qualifier,
				new IScopeContext[] { DefaultScope.INSTANCE });
	}

	@Override
	public ClangdContentAssistOptions options(Object context) {
		Optional<ProjectScope> project = projectScope(workspace, context);
		IScopeContext[] scopes;
		if (project.isPresent()) {
			scopes = new IScopeContext[] { project.get(), InstanceScope.INSTANCE, DefaultScope.INSTANCE };
		} else {
			scopes = new IScopeContext[] { InstanceScope.INSTANCE, DefaultScope.INSTANCE };
		}
		return new ClangdContentAssistPreferredOptions(metadata, qualifier, scopes);
	}

}
