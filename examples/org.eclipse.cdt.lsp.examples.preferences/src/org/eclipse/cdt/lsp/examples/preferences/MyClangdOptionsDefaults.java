/*******************************************************************************
 * Copyright (c) 2023 COSEDA Technologies GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Dominic Scharfe (COSEDA Technologies GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.examples.preferences;

import java.util.List;

import org.eclipse.cdt.lsp.clangd.ClangdOptionsDefaults;
import org.osgi.service.component.annotations.Component;

@Component(service = ClangdOptionsDefaults.class, property = { "service.ranking:Integer=100" })
public class MyClangdOptionsDefaults implements ClangdOptionsDefaults {

	@Override
	public List<String> additionalOptions() {
		return List.of("--header-insertion=never", "--default-config");
	}

	@Override
	public String clangdPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean useTidy() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean useBackgroundIndex() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String completionStyle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean prettyPrint() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String queryDriver() {
		// TODO Auto-generated method stub
		return null;
	}

}
