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
package org.eclipse.cdt.lsp.clangd.internal.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.clangd.ClangdMetadata2;
import org.eclipse.cdt.lsp.clangd.ClangdOptions2;
import org.eclipse.cdt.lsp.clangd.ClangdQualifier;
import org.eclipse.cdt.lsp.config.ConfigurationAccess;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IPreferenceMetadataStore;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.OsgiPreferenceMetadataStore;
import org.eclipse.osgi.util.NLS;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public final class ClangdConfigurationAccess extends ConfigurationAccess implements ClangdConfiguration {

	@Reference
	private ClangdMetadata2 metadata;

	@Reference
	private IWorkspace workspace;

	public ClangdConfigurationAccess() {
		super(new ClangdQualifier().get());
	}

	@Override
	public ClangdMetadata2 metadata() {
		return metadata;
	}

	@Override
	public ClangdOptions2 defaults() {
		return new ClangdPreferredOptions(qualifier, new IScopeContext[] { DefaultScope.INSTANCE }, metadata);
	}

	@Override
	public ClangdOptions2 options(Object context) {
		Optional<ProjectScope> project = projectScope(workspace, context);
		IScopeContext[] scopes;
		if (project.isPresent()) {
			scopes = new IScopeContext[] { project.get(), InstanceScope.INSTANCE, DefaultScope.INSTANCE };
		} else {
			scopes = new IScopeContext[] { InstanceScope.INSTANCE, DefaultScope.INSTANCE };
		}
		return new ClangdPreferredOptions(qualifier, scopes, metadata);
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
	public String qualifier() {
		return qualifier;
	}

	@Override
	public List<String> commands(Object context) {
		ClangdOptions2 options = options(context);
		List<String> list = new ArrayList<>();
		list.add(options.clangdPath());
		if (options.useTidy()) {
			list.add("--clang-tidy"); //$NON-NLS-1$
		}
		if (options.useBackgroundIndex()) {
			list.add("--background-index"); //$NON-NLS-1$
		}
		if (!options.completionStyle().isBlank()) {
			list.add(NLS.bind("--completion-style={0}", options.completionStyle())); //$NON-NLS-1$
		}
		if (options.prettyPrint()) {
			list.add("--pretty"); //$NON-NLS-1$
		}
		if (!options.queryDriver().isBlank()) {
			list.add(NLS.bind("--query-driver={0}", options.queryDriver())); //$NON-NLS-1$
		}

		list.addAll(options.additionalOptions());
		return list;
	}

}
