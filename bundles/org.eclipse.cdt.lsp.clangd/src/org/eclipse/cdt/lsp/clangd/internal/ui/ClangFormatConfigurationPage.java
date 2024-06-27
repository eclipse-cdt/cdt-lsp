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

package org.eclipse.cdt.lsp.clangd.internal.ui;

import org.eclipse.cdt.lsp.clangd.utils.ClangFormatUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.PropertyPage;

public class ClangFormatConfigurationPage extends PropertyPage implements IWorkbenchPreferencePage {
	private final String id = "org.eclipse.cdt.lsp.clangd.format.preferencePage"; //$NON-NLS-1$
	private IProject project;
	private IWorkspace workspace;
	private ClangFormatUtils utils = new ClangFormatUtils();

	@Override
	public void init(IWorkbench workbench) {
		workspace = workbench.getService(IWorkspace.class);
	}

	@Override
	public void setContainer(IPreferencePageContainer container) {
		super.setContainer(container);
		if (workspace == null) {
			workspace = PlatformUI.getWorkbench().getService(IWorkspace.class);
		}
		project = (IProject) getElement();
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		composite.setFont(parent.getFont());
		createConfigurationArea(composite);
		return composite;
	}

	public void createConfigurationArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1).create());
		if (project != null) {
			createLink(composite, LspEditorUiMessages.ClangFormatConfigurationPage_configure_ws_specific);
		}
		createButton(composite);
	}

	private Button createButton(Composite composite) {
		Button button = new Button(composite, SWT.PUSH);
		button.setLayoutData(GridDataFactory.fillDefaults().span(1, 1).indent(0, 0).create());
		var txt = LspEditorUiMessages.ClangFormatConfigurationPage_openProjectFormatFile;
		if (project == null) {
			txt = LspEditorUiMessages.ClangFormatConfigurationPage_openWorkspaceFormatFile;
		}
		button.setText(txt);
		button.setToolTipText(LspEditorUiMessages.ClangFormatConfigurationPage_openFormatFileTooltip);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (project != null) {
					var formatFile = project.getFile(ClangFormatUtils.format_file);
					utils.checkProjectClangFormatFile(formatFile);
					openFile(formatFile.getLocationURI().toString());
				} else {
					utils.checkWorkspaceClangFormatFile(workspace);
					openFile(workspace.getRoot().getLocation().append(ClangFormatUtils.format_file).toPath().toUri()
							.toString());
				}
			}
		});
		return button;
	}

	private void openFile(String path) {
		LSPEclipseUtils.open(path, null);
		// close preference page:
		getShell().close();
	}

	private Link createLink(Composite composite, String text) {
		Link link = new Link(composite, SWT.NONE);
		link.setFont(composite.getFont());
		link.setText("<A>" + text + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, null).open();
				//close this shell as well to not hide the (possibly) opened workspace .clang-format file:
				getShell().close();
			}
		});
		return link;
	}
}
