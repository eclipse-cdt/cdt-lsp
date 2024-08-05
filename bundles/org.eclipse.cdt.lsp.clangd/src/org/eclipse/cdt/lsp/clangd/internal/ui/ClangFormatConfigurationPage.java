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

import org.eclipse.cdt.lsp.clangd.ClangFormatFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

public class ClangFormatConfigurationPage extends PropertyPage implements IWorkbenchPreferencePage {
	private IProject project;
	private ClangFormatFile formatFile;

	public ClangFormatConfigurationPage() {
		formatFile = PlatformUI.getWorkbench().getService(ClangFormatFile.class);
	}

	@Override
	public void init(IWorkbench workbench) {
		// do nothing
	}

	@Override
	public void setContainer(IPreferencePageContainer container) {
		super.setContainer(container);
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
		createButton(composite);
	}

	private Button createButton(Composite composite) {
		Button button = new Button(composite, SWT.PUSH);
		button.setLayoutData(GridDataFactory.fillDefaults().span(1, 1).indent(0, 0).create());
		button.setText(LspEditorUiMessages.ClangFormatConfigurationPage_openProjectFormatFile);
		button.setToolTipText(LspEditorUiMessages.ClangFormatConfigurationPage_openFormatFileTooltip);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (project != null) {
					formatFile.openClangFormatFile(project);
					getShell().close();
				}
			}
		});
		return button;
	}
}
