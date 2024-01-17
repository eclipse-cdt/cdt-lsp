/*******************************************************************************
 * Copyright (c) 2024 Bachmann electronic GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;

/**
 * Helper class to resolve macros in builder CWD
 */
public class MacroResolver {

	public String resolveValue(String value, String nonexistentMacrosValue, String listDelimiter,
			ICConfigurationDescription cfg) throws CdtVariableException {
		return CCorePlugin.getDefault().getCdtVariableManager().resolveValue(value, nonexistentMacrosValue,
				listDelimiter, cfg);
	}

}
