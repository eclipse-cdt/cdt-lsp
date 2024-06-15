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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

public class ClangFormatConfigurationPage extends PropertyPage implements IWorkbenchPreferencePage {
	private static final String format_file = ".clang-format"; //$NON-NLS-1$
	private IProject project;
	protected IWorkspace workspace;

	@Override
	public void init(IWorkbench workbench) {
		this.workspace = workbench.getService(IWorkspace.class);
	}

	@Override
	public void setContainer(IPreferencePageContainer container) {
		super.setContainer(container);
		if (workspace == null) {
			workspace = PlatformUI.getWorkbench().getService(IWorkspace.class);
		}
		this.project = (IProject) getElement();
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
					var formatFile = project.getFile(format_file);
					createProjectClangFormatFile(formatFile, ".clang-format-project"); //$NON-NLS-1$
					openFile(formatFile.getLocationURI().toString());
				} else {
					createWorkspaceClangFormatFile();
					openFile(workspace.getRoot().getLocation().append(format_file).toPath().toUri().toString());
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

	private void createWorkspaceClangFormatFile() {
		var path = workspace.getRoot().getLocation().append(format_file).toOSString();
		var formatFile = new File(path);
		if (!formatFile.exists()) {
			try (final var source = getClass().getResourceAsStream(".clang-format-ws");) { //$NON-NLS-1$
				Files.copy(source, formatFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}
		}
	}

	private void createProjectClangFormatFile(IFile configFile, String templateFileName) {
		if (!configFile.exists()) {
			try (final var source = getClass().getResourceAsStream(templateFileName);) {
				configFile.create(source, true, new NullProgressMonitor());
			} catch (CoreException e) {
				Platform.getLog(getClass()).log(e.getStatus());
			} catch (IOException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}
		}
	}

}
