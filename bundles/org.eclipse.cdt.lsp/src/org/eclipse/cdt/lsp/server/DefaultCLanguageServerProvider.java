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

package org.eclipse.cdt.lsp.server;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.runtime.IPath;

public class DefaultCLanguageServerProvider implements ICLanguageServerProvider {
	public static final String CLANG_TIDY = "--clang-tidy";
	public static final String BACKGROUND_INDEX = "--background-index";
	public static final String COMPLETION_STYLE = "--completion-style=detailed";
	public static final String PRETTY = "--pretty";
	public static final String QUERY_DRIVER = "--query-driver=";
	
	public static final String DEFAULT_COMPILE_COMMANDS_DIR = "build/default";
	
	private List<String> defaultCommands;
	private List<String> commands;
	
	protected EnableExpression enableExpression;
	
	public DefaultCLanguageServerProvider() {
		commands = createCommands();
	}

	@Override
	public List<String> getCommands() {
		return commands;
	}
	
	@Override
	public void setCommands(List<String> commands) {
		this.commands = commands;		
	}
	
	@Override
	public List<String> getDefaultCommands() {
		return defaultCommands;
	}

	@Override
	public void setEnableExpression(EnableExpression enableExpression) {
		this.enableExpression= enableExpression;
	}

	@Override
	public boolean isEnabledFor(Object document) {
		// check if server has been found:
		if (getCommands().isEmpty()) {
			return false;
		}
		// check if enable expression is defined:
		if (enableExpression != null) {
			return enableExpression.evaluate(document);
		}
		//language server is always enabled when no enable expression has been defined in the extension point: 
		return true;
	}
	
	@Override
	public String getServerPath() {
		if (commands.isEmpty())
			return "";
		return commands.get(0);
	}
	
	@Override
	public String getOptionsAsString() {
		return getCommandOptionsAsString(commands);
	}
	
	@Override
	public String getDefaultServerPath() {
		if (defaultCommands.isEmpty())
			return "";
		return defaultCommands.get(0);
	}

	@Override
	public String getDefaultOptionsAsString() {
		return getCommandOptionsAsString(defaultCommands);
	}
	
	protected List<String> createCommands() {
		List<String> commands = new ArrayList<>();
		IPath clangdLocation = PathUtil.findProgramLocation("clangd", null); //in case pathStr is null environment variable ${PATH} is inspected
		if (clangdLocation != null) {
			commands.add(clangdLocation.toOSString());
			commands.add(CLANG_TIDY);
			commands.add(BACKGROUND_INDEX);
			commands.add(COMPLETION_STYLE);
			commands.add(PRETTY);
			// clangd will execute drivers and fetch necessary include paths to compile your code:
			IPath compilerLocation = PathUtil.findProgramLocation("gcc", null);
			if (compilerLocation != null) {
				commands.add(QUERY_DRIVER + compilerLocation.removeLastSegments(1).append(IPath.SEPARATOR + "*"));
			}
		}
		defaultCommands = commands; //initialize defaults
		return commands;
	}
	
	private String getCommandOptionsAsString(List<String> commands) {
		StringBuilder builder = new StringBuilder();
		commands.forEach(cmd -> { builder.append(" ").append(cmd); });
		return builder.toString();
	}
}
