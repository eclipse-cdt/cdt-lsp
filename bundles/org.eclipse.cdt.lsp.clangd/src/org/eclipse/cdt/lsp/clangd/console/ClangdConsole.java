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

package org.eclipse.cdt.lsp.clangd.console;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.cdt.lsp.server.ILogProvider;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

public final class ClangdConsole implements ILogProvider {
	private static final String CLANGD_CONSOLE = "Clangd"; //$NON-NLS-1$
	private OutputStream outputStream;

	@Override
	public OutputStream getOutputStream() {
		if (outputStream == null) {
			outputStream = findConsole().newOutputStream();
		}
		return outputStream;
	}

	@Override
	public void close() {
		if (outputStream != null) {
			try {
				outputStream.close();
				outputStream = null;
			} catch (IOException e) {
				Platform.getLog(ClangdConsole.class).error(e.getMessage(), e);
			}
		}

	}

	private MessageConsole findConsole() {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		for (IConsole existing : conMan.getConsoles()) {
			if (CLANGD_CONSOLE.equals(existing.getName()))
				return (MessageConsole) existing;
		}
		// no console found, so create a new one
		final var myConsole = new MessageConsole(CLANGD_CONSOLE, null);
		myConsole.setWaterMarks(80000, 800000); // limit text buffer size to prevent OOM
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}
}
