/*******************************************************************************
 * Copyright (c) 2023, 2025 COSEDA Technologies GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Dominic Scharfe (COSEDA Technologies GmbH) - initial implementation
 *     Alexander Fedorov (ArSysOp) - options API evolution
 *******************************************************************************/
package org.eclipse.cdt.lsp.examples.preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.cdt.lsp.clangd.ClangdMetadata;
import org.eclipse.cdt.lsp.config.ConfigurationMetadataBase;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.osgi.service.component.annotations.Component;

@Component(property = { "service.ranking:Integer=100" })
public class MyClangdOptionsDefaults extends ConfigurationMetadataBase implements ClangdMetadata {

	@Override
	protected List<PreferenceMetadata<?>> definePreferences() {
		List<PreferenceMetadata<?>> defined = new ArrayList<>();
		defined.add(clangdPath);
		defined.add(useTidy);
		defined.add(useBackgroundIndex);
		defined.add(completionStyle);
		defined.add(prettyPrint);
		defined.add(queryDriver);
		defined.add(overrideString(additionalOptions, //
				List.of("--header-insertion=never", "--default-config").stream()
						.collect(Collectors.joining(System.lineSeparator()))));
		defined.add(logToConsole);
		defined.add(validateClangdOptions);
		return defined;
	}

}
