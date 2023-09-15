/*******************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/
package org.eclipse.cdt.lsp.examples.preferences;

import org.eclipse.cdt.lsp.editor.LanguageServerEnable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.osgi.service.component.annotations.Component;

@Component(property = { "service.ranking:Integer=100" })
public class MyClangdEnable implements LanguageServerEnable {

	@Override
	public boolean isEnabledFor(IProject project) {
		if (project != null) {
			try {
				return project.hasNature("org.eclipse.cdt.cmake.core.cmakeNature");
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
