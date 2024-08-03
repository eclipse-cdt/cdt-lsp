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

import org.eclipse.cdt.lsp.clangd.utils.ClangFormatUtils;
import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class ClangFormatMonitor implements IPartListener2, IWindowListener {
	private final ClangFormatUtils formatUtils;

	public ClangFormatMonitor(ClangFormatUtils formatUtils) {
		this.formatUtils = formatUtils;
	}

	public ClangFormatMonitor start() {
		if (PlatformUI.isWorkbenchRunning()) {
			PlatformUI.getWorkbench().addWindowListener(this);

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
			formatUtils.createClangFormatFile(file.getProject());
		}
	}

	private boolean isLspCEditor(IWorkbenchPartReference partRef) {
		return partRef != null ? LspPlugin.LSP_C_EDITOR_ID.equals(partRef.getId()) : false;
	}

}
