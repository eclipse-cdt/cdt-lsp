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

package org.eclipse.cdt.lsp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.runtime.IPath;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;

public class ClangdStreamConnectionProvider extends ProcessStreamConnectionProvider implements StreamConnectionProvider {
	public static final String CLANG_TIDY = "--clang-tidy";
	public static final String BACKGROUND_INDEX = "--background-index";
	public static final String COMPLETION_STYLE = "--completion-style=detailed";
	public static final String PRETTY = "--pretty";
	public static final String QUERY_DRIVER = "--query-driver";
	
	public ClangdStreamConnectionProvider() {
		var commandProvider = new CLanguageServerRegistry().createCLanguageServerCommandProvider();
		if (commandProvider != null) {
			setCommands(commandProvider.getCommands());
		} else {
			setCommands(getDefault());
		}		
		// set the working directory for the Java process which runs clangd:
		setWorkingDirectory(System.getProperty("user.dir"));
	}

	private List<String> getDefault(){
		List<String> commands = new ArrayList<>();
		IPath clangdLocation = PathUtil.findProgramLocation("clangd", null); //case pathStr is null environment variable ${PATH} is inspected
		if (clangdLocation != null) {
			commands.add(clangdLocation.toOSString());
			commands.add(CLANG_TIDY);
			commands.add(BACKGROUND_INDEX);
			commands.add(COMPLETION_STYLE);
			commands.add(PRETTY);
			// clangd will execute drivers and fetch necessary include paths to compile your code:
			IPath compilerLocation = PathUtil.findProgramLocation("gcc", null);
			if (compilerLocation != null) {
				commands.add(QUERY_DRIVER + "=" + compilerLocation.removeLastSegments(1).append(IPath.SEPARATOR + "*"));
			}
		}
		return commands;
	}

}


