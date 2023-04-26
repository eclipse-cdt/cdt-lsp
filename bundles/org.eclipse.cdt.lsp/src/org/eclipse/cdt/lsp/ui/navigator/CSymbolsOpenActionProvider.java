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

package org.eclipse.cdt.lsp.ui.navigator;

import org.eclipse.cdt.internal.ui.cview.CViewMessages;
import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.outline.SymbolsModel.DocumentSymbolWithFile;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.OpenFileAction;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import org.eclipse.ui.texteditor.ITextEditor;

@SuppressWarnings("restriction")
public class CSymbolsOpenActionProvider extends CommonActionProvider {

	private class OpenCFileAction extends OpenFileAction {
		private IWorkbenchPage page;
		private DocumentSymbolWithFile fOpenElement;

		public OpenCFileAction(IWorkbenchPage page) {
			super(page);
			this.page = page;
		}

		@Override
		public void run() {
			if (fOpenElement != null) {
				IEditorPart part;
				try {
					part = IDE.openEditor(this.page, fOpenElement.file);
					revealInEditor(part, fOpenElement);
				} catch (CoreException exc) {
					LspPlugin.logError(exc.getMessage(), exc);
				}
			}
		}
		
		@Override
		protected boolean updateSelection(IStructuredSelection selection) {
			fOpenElement = null;
			if (selection.size() == 1) {
				Object element = selection.getFirstElement();
				if (element instanceof DocumentSymbolWithFile) {
					fOpenElement = (DocumentSymbolWithFile) element;
				}
			}
			return fOpenElement != null || super.updateSelection(selection);
		}

		public IWorkbenchPage getPage() {
			return page;
		}
		
		private void revealInEditor(IEditorPart part, DocumentSymbolWithFile element) {
			if (element == null) {
				return;
			}
			if (part instanceof ITextEditor) {
				try {
					var range = element.symbol.getSelectionRange();
					var document = LSPEclipseUtils.getDocument(element.file);
					int startOffset = document.getLineOffset(range.getStart().getLine())
							+ range.getStart().getCharacter();
					int endOffset = document.getLineOffset(range.getEnd().getLine())
							+ range.getEnd().getCharacter();
					((ITextEditor) part).selectAndReveal(startOffset, endOffset - startOffset);
				} catch (BadLocationException exc) {
					LspPlugin.logError(exc.getMessage(), exc);
				}
			}
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
	public void updateActionBars() {
		if (openCFileAction != null) {
			IStructuredSelection celements = (IStructuredSelection) getContext().getSelection();
			openCFileAction.selectionChanged(celements);
		}
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		if (openCFileAction != null) {
			IStructuredSelection celements = (IStructuredSelection) getContext().getSelection();
			
			openCFileAction.selectionChanged(celements);
			if (openCFileAction.isEnabled()) {
				menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, openCFileAction);
				fillOpenWithMenu(menu, celements);
			}
		}
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
		IFile file;
		if (selection.getFirstElement() instanceof DocumentSymbolWithFile) {
			file = ((DocumentSymbolWithFile) selection.getFirstElement()).file;
		} else {
			return;
		}

		MenuManager submenu = new MenuManager(CViewMessages.OpenWithMenu_label, ICommonMenuConstants.GROUP_OPEN_WITH);
		submenu.add(new OpenWithMenu(openCFileAction.getPage(), file));
		menu.insertAfter(ICommonMenuConstants.GROUP_OPEN_WITH, submenu);
	}
}
