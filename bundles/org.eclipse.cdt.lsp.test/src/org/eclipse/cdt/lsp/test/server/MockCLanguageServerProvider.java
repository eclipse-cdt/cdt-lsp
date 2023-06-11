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
 * Alexander Fedorov (ArSysOp) - rework access to preferences
 *******************************************************************************/

package org.eclipse.cdt.lsp.test.server;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.core.resources.IProject;

public class MockCLanguageServerProvider implements ICLanguageServerProvider {

	@Override
	public List<String> getCommands(URI rootUri) {
		return Collections.emptyList();
	}

	@Override
	public boolean isEnabledFor(IProject project) {
		return false;
	}

}
