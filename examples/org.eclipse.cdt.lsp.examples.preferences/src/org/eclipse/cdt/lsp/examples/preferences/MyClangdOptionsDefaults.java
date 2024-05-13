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
import java.util.Optional;

import org.eclipse.cdt.lsp.clangd.ClangdOptionsDefaults;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.runtime.IPath;
import org.osgi.service.component.annotations.Component;

@Component(service = ClangdOptionsDefaults.class, property = { "service.ranking:Integer=100" })
public class MyClangdOptionsDefaults implements ClangdOptionsDefaults {

	@Override
	public List<String> additionalOptions() {
		return List.of("--header-insertion=never", "--default-config");
	}

	@Override
	public String clangdPath() {
		return Optional.ofNullable(PathUtil.findProgramLocation("clangd", null)) //$NON-NLS-1$
				.map(IPath::toOSString)//
				.orElse("clangd"); //  //$NON-NLS-1$
	}

	@Override
	public boolean useTidy() {
		return true;
	}

	@Override
	public boolean useBackgroundIndex() {
		return true;
	}

	@Override
	public String completionStyle() {
		return "detailed"; //$NON-NLS-1$
	}

	@Override
	public boolean prettyPrint() {
		return true;
	}

	@Override
	public String queryDriver() {
		return Optional.ofNullable(PathUtil.findProgramLocation("gcc", null)) //$NON-NLS-1$
				.map(p -> p.removeLastSegments(1).append(IPath.SEPARATOR + "*"))// //$NON-NLS-1$
				.map(IPath::toString)//
				.orElse(""); //  //$NON-NLS-1$
	}

}
