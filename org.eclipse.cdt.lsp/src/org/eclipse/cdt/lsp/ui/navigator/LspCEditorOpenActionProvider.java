/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.ui.navigator;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.actions.SelectionConverter;
import org.eclipse.cdt.internal.ui.cview.CViewMessages;
import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.actions.OpenInNewWindowAction;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

@SuppressWarnings("restriction")
public class LspCEditorOpenActionProvider extends CommonActionProvider {

	private class OpenCFileAction extends OpenFileAction {
		private IWorkbenchPage page;

		public OpenCFileAction(IWorkbenchPage page) {
			super(page);
			this.page = page;
		}

		@Override
		public void run() {
			var sel = page.getSelection();
			if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
				try {
					IFile file = Adapters.adapt(((IStructuredSelection) sel).getFirstElement(), IFile.class);
					if (file != null) {
						IDE.openEditor(this.page, file);
					}
				} catch (CoreException exc) {
					LspPlugin.logError(exc.getMessage(), exc);
				}
			}
		}

		public IWorkbenchPage getPage() {
			return page;
		}
	}

	private OpenCFileAction openCFileAction;
	private IViewPart viewPart = null;

	@Override
	public void init(ICommonActionExtensionSite site) {
		ICommonViewerWorkbenchSite workbenchSite = null;
		if (site.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			workbenchSite = (ICommonViewerWorkbenchSite) site.getViewSite();
		}
		if (workbenchSite != null) {
			if (workbenchSite.getPart() != null && workbenchSite.getPart() instanceof IViewPart) {
				viewPart = (IViewPart) workbenchSite.getPart();
				openCFileAction = new OpenCFileAction(viewPart.getSite().getPage());
			}
		}
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		if (openCFileAction != null) {
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openCFileAction);
			actionBars.updateActionBars();
		}
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		IStructuredSelection celements = (IStructuredSelection) getContext().getSelection();
		IStructuredSelection selection = SelectionConverter.convertSelectionToResources(celements);

		openCFileAction.selectionChanged(celements);
		if (openCFileAction.isEnabled()) {
			menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openCFileAction);
			fillOpenWithMenu(menu, selection);
		}

		addNewWindowAction(menu, selection);
	}

	/**
	 * Adds the OpenWith submenu to the context menu.
	 *
	 * @param menu
	 *                      the context menu
	 * @param selection
	 *                      the current selection
	 */
	private void fillOpenWithMenu(IMenuManager menu, IStructuredSelection selection) {
		// Only supported if exactly one file is selected.
		if (selection.size() != 1) {
			return;
		}
		Object element = selection.getFirstElement();
		if (!(element instanceof IFile)) {
			return;
		}

		MenuManager submenu = new MenuManager(CViewMessages.OpenWithMenu_label, ICommonMenuConstants.GROUP_OPEN_WITH);
		submenu.add(new OpenWithMenu(openCFileAction.getPage(), (IFile) element));
		menu.insertAfter(ICommonMenuConstants.GROUP_OPEN_WITH, submenu);
	}

	/**
	 * Adds the Open in New Window action to the context menu.
	 *
	 * @param menu
	 *                      the context menu
	 * @param selection
	 *                      the current selection
	 */
	private void addNewWindowAction(IMenuManager menu, IStructuredSelection selection) {

		// Only supported if exactly one container (i.e open project or folder) is selected.
		if (selection.size() != 1) {
			return;
		}
		Object element = selection.getFirstElement();
		if (element instanceof ICElement) {
			element = ((ICElement) element).getResource();
		}
		if (!(element instanceof IContainer)) {
			return;
		}
		if (element instanceof IProject && !(((IProject) element).isOpen())) {
			return;
		}

		menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN,
				new OpenInNewWindowAction(viewPart.getSite().getWorkbenchWindow(), (IContainer) element));
	}
}
