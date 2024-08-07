/*******************************************************************************
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd.internal.config;

import org.eclipse.cdt.lsp.clangd.ClangFormatFile;
import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public final class ClangFormatMonitor implements IPartListener2, IWindowListener {
	private ClangFormatFile clangFormat;

	public ClangFormatMonitor start() {
		if (PlatformUI.isWorkbenchRunning()) {
			var workbench = PlatformUI.getWorkbench();
			clangFormat = workbench.getService(ClangFormatFile.class);
			if (clangFormat == null) {
				Platform.getLog(getClass()).error("Cannot get ClangFormatFile service."); //$NON-NLS-1$
				return this;
			}
			workbench.addWindowListener(this);

			// Ensure existing windows get connected
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			for (int i = 0, length = windows.length; i < length; i++) {
				windows[i].getPartService().addPartListener(this);
			}
		}
		return this;
	}

	public void stop() {
		PlatformUI.getWorkbench().removeWindowListener(this);
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		createClangFormatFile(partRef);
	}

	@Override
	public void windowActivated(IWorkbenchWindow window) {
		// do nothing
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
		// do nothing
	}

	@Override
	public void windowClosed(IWorkbenchWindow window) {
		window.getPartService().removePartListener(this);
	}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
		window.getPartService().addPartListener(this);
	}

	private void createClangFormatFile(IWorkbenchPartReference partRef) {
		if (isLspCEditor(partRef)) {
			var file = partRef.getPage().getActiveEditor().getEditorInput().getAdapter(IFile.class);

			if (file == null) {
				return;
			}
			clangFormat.createClangFormatFile(file.getProject());
		}
	}

	private boolean isLspCEditor(IWorkbenchPartReference partRef) {
		return partRef != null ? LspPlugin.LSP_C_EDITOR_ID.equals(partRef.getId()) : false;
	}

}
