/*******************************************************************************
 * Copyright (c) 2023, 2024 Bachmann electronic GmbH and others.
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

package org.eclipse.cdt.lsp.clangd.internal.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.cdt.lsp.clangd.ClangdConfiguration;
import org.eclipse.cdt.lsp.clangd.ClangdFallbackFlags;
import org.eclipse.cdt.lsp.clangd.ClangdOptions2;
import org.eclipse.cdt.lsp.config.Configuration;
import org.eclipse.cdt.lsp.editor.LanguageServerEnable;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider3;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.core.variables.VariablesPlugin;

public final class ClangdLanguageServerProvider implements ICLanguageServerProvider3 {

	private final ServiceCaller<ClangdConfiguration> configuration = new ServiceCaller<>(getClass(),
			ClangdConfiguration.class);

	private final ServiceCaller<Configuration> editorConfiguration = new ServiceCaller<>(getClass(),
			Configuration.class);

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
		configuration.call(c -> result.addAll(c.commands(rootUri).stream()
				.map(ClangdLanguageServerProvider::resolveVariables).collect(Collectors.toList())));
		return result;
	}

	private static String resolveVariables(String cmd) {
		try {
			return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(cmd);
		} catch (CoreException e) {
			return cmd;
		}
	}

	@Override
	public boolean isEnabledFor(IProject project) {
		boolean[] enabled = new boolean[1];
		editorConfiguration.call(c -> enabled[0] = ((LanguageServerEnable) c.options(project)).isEnabledFor(project));
		return enabled[0];
	}

	@Override
	public boolean logToConsole() {
		boolean[] enabled = new boolean[1];
		configuration.call(c -> enabled[0] = ((ClangdOptions2) c.options(null)).logToConsole());
		return enabled[0];
	}

}
