/*******************************************************************************
 * Copyright (c) 2025 Bachmann electronic GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 * Alexander Fedorov (ArSysOp) - rework access to preferences
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.server;

import static org.eclipse.lsp4e.internal.NullSafetyHelper.castNonNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider3;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

public final class CLanguageServerStreamConnectionProvider extends ProcessStreamConnectionProvider {
	private final ICLanguageServerProvider provider;
	private Runnable errorStreamPipeStopper;

	public CLanguageServerStreamConnectionProvider() {
		this.provider = LspPlugin.getDefault().getCLanguageServerProvider();
		// set the working directory for the Java process which runs the C/C++ language server:
		setWorkingDirectory(System.getProperty("user.dir")); //$NON-NLS-1$
	}

	@Override
	public Object getInitializationOptions(URI rootUri) {
		setCommands(provider.getCommands(rootUri));
		return provider.getInitializationOptions(rootUri);
	}

	/**
	 * We need to overwrite the super method because all clangd I/O has to be connected to the current Java process over a pipe (default).
	 * Otherwise we cannot pipe the stderr (where the log will be written to) from the language server to an output stream, e.g. from a console.
	 */
	@Override
	protected ProcessBuilder createProcessBuilder() {
		if (logEnabled()) {
			final var builder = new ProcessBuilder(castNonNull(getCommands()));
			final var workDir = getWorkingDirectory();
			if (workDir != null) {
				builder.directory(new File(workDir));
			}
			return builder;
		}
		return super.createProcessBuilder();
	}

	@Override
	public void start() throws IOException {
		super.start();
		if (logEnabled()) {
			var logProvider = LogProviderRegistry.createLogProvider();
			if (logProvider.isPresent()) {
				errorStreamPipeStopper = new AsyncStreamPipe().pipeTo(new BufferedInputStream(getErrorStream()),
						logProvider.get().getOutputStream());
			}
		}
	}

	@Override
	public void stop() {
		if (errorStreamPipeStopper != null)
			errorStreamPipeStopper.run();
		super.stop();
	}

	private boolean logEnabled() {
		return provider instanceof ICLanguageServerProvider3 provider3 && provider3.logToConsole();
	}

}
