/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 * Alexander Fedorov (ArSysOp) - rework to OSGi components
 * Alexander Fedorov (ArSysOp) - rework access to preferences
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.clangd;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.clangd.ClangdEnable;
import org.eclipse.cdt.lsp.clangd.ClangdFallbackFlags;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.ServiceCaller;

public final class ClangdLanguageServerProvider implements ICLanguageServerProvider {

	private final ServiceCaller<ClangdConfiguration> configuration = new ServiceCaller<>(getClass(),
			ClangdConfiguration.class);

	@Override
	public Object getInitializationOptions(URI rootUri) {
		List<Object> result = new ArrayList<>();
		ServiceCaller.callOnce(getClass(), ClangdFallbackFlags.class, //
				f -> result.add(f.getFallbackFlagsFromInitialUri(rootUri)));
		return result.stream().filter(Objects::nonNull).findFirst().orElse(null);
	}

	@Override
	public List<String> getCommands(URI rootUri) {
		List<String> result = new ArrayList<>();
		configuration.call(c -> result.addAll(c.commands(rootUri)));
		return result;
	}

	@Override
	public boolean isEnabledFor(IProject project) {
		boolean[] enabled = new boolean[1];
		configuration.call(c -> enabled[0] = ((ClangdEnable) c.options(project)).isEnabledFor(project));
		return enabled[0];
	}

}
