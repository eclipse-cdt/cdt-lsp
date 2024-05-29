/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 * Alexander Fedorov (ArSysOp) - rework to OSGi components
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd.internal.config;

import java.net.URI;
import java.util.Optional;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.lsp.ExistingResource;
import org.eclipse.cdt.lsp.clangd.ClangdFallbackFlags;
import org.eclipse.cdt.lsp.editor.InitialUri;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.environment.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Used to create and set the clangd fallbackFlags.
 * This is needed for Windows OS to allow clangd to determine the system includes in case no compile_commands.json can be found.
 * see also: https://clangd.llvm.org/extensions#compilation-commands
 *
 */
@Component
public final class ClangdFallbackManager implements ClangdFallbackFlags {

	class FallbackFlags {
		// fallbackFlags is used as element name in the initialize jsonrpc call:
		// "initializationOptions":{"fallbackFlags":[...
		final String fallbackFlags[];

		FallbackFlags(String[] systemIncludes) {
			if (systemIncludes != null) {
				fallbackFlags = new String[systemIncludes.length];
				for (int i = 0; i < systemIncludes.length; i++) {
					this.fallbackFlags[i] = ISYSTEM.concat(systemIncludes[i]);
				}
			} else {
				fallbackFlags = null;
			}
		}
	}

	private static final String ISYSTEM = "-isystem"; //$NON-NLS-1$
	private final boolean isWindows = Constants.OS_WIN32.equals(Platform.getOS());

	@Reference
	private ICBuildConfigurationManager build;
	@Reference
	private InitialUri uri;
	@Reference
	private IWorkspace workspace;

	@Override
	public FallbackFlags getFallbackFlagsFromInitialUri(URI root) {
		if (isWindows) {
			return uri.find(root)//
					.flatMap(new ExistingResource(workspace))//
					.flatMap(this::flags)//
					.orElse(null);
		}
		return null;
	}

	private Optional<FallbackFlags> flags(IResource initial) {
		return buildConfiguration(initial)//
				.map(bc -> bc.getScannerInformation(initial))//
				.map(IScannerInfo::getIncludePaths)//
				.map(FallbackFlags::new);
	}

	private Optional<ICBuildConfiguration> buildConfiguration(IResource initial) {
		try {
			var active = initial.getProject().getActiveBuildConfig();
			if (active != null) {
				return Optional.ofNullable(build.getBuildConfiguration(active));
			}
		} catch (CoreException e) {
			Platform.getLog(ClangdFallbackManager.class).error(e.getMessage(), e);
		}
		return Optional.empty();
	}
}
