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
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd;

import java.util.Optional;

import org.eclipse.cdt.core.build.ICBuildConfigurationManager;
import org.eclipse.cdt.lsp.InitialFileManager;
import org.eclipse.cdt.lsp.LspUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.osgi.service.environment.Constants;

/**
 * Used to create and set the clangd fallbackFlags.
 * This is needed for Windows OS to allow clangd to determine the system includes in case no compile_commands.json can be found.
 * see also: https://clangd.llvm.org/extensions#compilation-commands
 *
 */
public class ClangdFallbackManager {

	class FallbackFlags {
		// fallbackFlags is used as element name in the initialize jsonrpc call:
		// "initializationOptions":{"fallbackFlags":[...
		final String fallbackFlags[];

		FallbackFlags(String[] systemIncludes) {
			this.fallbackFlags = systemIncludes;
			if (systemIncludes != null) {
				for (int i = 0; i < systemIncludes.length; i++) {
					this.fallbackFlags[i] = ISYSTEM.concat(systemIncludes[i]);
				}
			}
		}
	}

	private static final String ISYSTEM = "-isystem"; //$NON-NLS-1$
	private final ServiceCaller<ICBuildConfigurationManager> configManagerServiceCaller = new ServiceCaller<>(
			getClass(), ICBuildConfigurationManager.class);
	private final InitialFileManager initialFileManager = InitialFileManager.getInstance();
	private final boolean isWindows = Constants.OS_WIN32.equals(Platform.getOS());

	public FallbackFlags getFallbackFlagsFromInitialUri() {
		if (isWindows) {
			var configManager = configManagerServiceCaller.current();
			try {
				var initialFile = Optional.of(initialFileManager).map(m -> m.getInitialUri()).map(LspUtils::getFile)
						.orElse(Optional.empty());
				if (initialFile.isPresent() && configManager.isPresent()) {
					var activeBuildConfig = initialFile.get().getProject().getActiveBuildConfig();
					var cBuildConfig = configManager.get().getBuildConfiguration(activeBuildConfig);
					if (cBuildConfig != null) {
						var scannerInfo = cBuildConfig.getScannerInformation(initialFile.get());
						if (scannerInfo != null) {
							return new FallbackFlags(scannerInfo.getIncludePaths());
						}
					}
				}
			} catch (CoreException e) {
				Platform.getLog(ClangdFallbackManager.class).error(e.getMessage(), e);
			}
		}
		return null;
	}
}
