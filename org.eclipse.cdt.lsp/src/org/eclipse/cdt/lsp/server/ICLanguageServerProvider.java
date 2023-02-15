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

import java.util.List;

public interface ICLanguageServerProvider {

	/**
	 * The command list includes the location of the language server followed by its calling arguments.
	 * 
	 * @return Command list to run language server
	 */
	public List<String> getCommands();
	
	public void setEnableExpression(EnableExpression enableExpression);
	
	public boolean isEnabled(Object document);

}
