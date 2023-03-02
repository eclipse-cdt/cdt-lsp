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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

public class LspUtils {
	
	/**
	 * Checks if given ContentType id matches the content types for C/C++ files.
	 * 
	 * @param id ContentType id
	 * @return {@code true} if C/C++ content type
	 */
	public static boolean isCContentType(String id) {
		// TODO: The content type definition from TM4E "lng.cpp" can be omitted if either https://github.com/eclipse-cdt/cdt/pull/310 or 
		// https://github.com/eclipse/tm4e/pull/500 has been merged.
		return ( id.startsWith("org.eclipse.cdt.core.c") && (id.endsWith("Source") || id.endsWith("Header")) ) || "lng.cpp".equals(id);
	}
	
	/**
	 * Show error dialog to user
	 * @param title
	 * @param errorText
	 * @param status
	 */
	public static void showErrorMessage(final String title, final String errorText, final Status status) {
		UIJob job = new UIJob("LSP Utils") //$NON-NLS-1$
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				ErrorDialog.openError(getActiveShell(), title, errorText, status);
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}
	
	private static Shell getActiveShell() {
		if (PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null)
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		return null;
	}

}
