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
 * Alexander Fedorov (ArSysOp) - use Platform for logging
 *******************************************************************************/

package org.eclipse.cdt.lsp.ui.navigator;

import java.util.Set;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.navigator.CNavigatorContentProvider;
import org.eclipse.cdt.lsp.internal.messages.LspUiMessages;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.lsp4e.outline.SymbolsModel.DocumentSymbolWithURI;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

public class CSymbolsContentProvider extends CNavigatorContentProvider {

	private final SymbolsManager symbolsManager = SymbolsManager.INSTANCE;
	private DeferredCSymbolLoader loader;
	private Object currentInput;

	private static final WorkbenchAdapter ERROR_ELEMENT = new WorkbenchAdapter() {

		@Override
		public String getLabel(Object object) {
			return LspUiMessages.NavigatorView_ErrorOnLoad;
		}
	};

	@Override
	public void dispose() {
		if (currentInput != null && loader != null) {
			loader.cancel(currentInput);
		}
		currentInput = null;
		symbolsManager.dispose();
		loader = null;
		super.dispose();
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (oldInput != null && loader != null) {
			loader.cancel(oldInput);
		}
		currentInput = newInput;
		if (viewer instanceof AbstractTreeViewer && newInput != null) {
			loader = new DeferredCSymbolLoader((AbstractTreeViewer) viewer, (IDeferredWorkbenchAdapter) symbolsManager);
		}
		super.inputChanged(viewer, oldInput, newInput);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getPipelinedChildren(Object parent, Set currentChildren) {
		if (parent instanceof ITranslationUnit) {
			//remove children from other provider first:
			currentChildren.clear();
			for (Object child : getChildren(parent)) {
				if (child != null) {
					currentChildren.add(child);
				}
			}
		}
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof DocumentSymbolWithURI) {
			return symbolsManager.getChildren(parentElement);
		} else if (parentElement instanceof ITranslationUnit unit) {
			return getTranslationUnitChildren(unit);
		}
		return NO_CHILDREN;
	}

	@Override
	protected Object[] getTranslationUnitChildren(ITranslationUnit unit) {
		if (!symbolsManager.isDirty(unit)) {
			var symbols = symbolsManager.getTranslationUnitElements(unit);
			if (symbols != null) {
				return symbols;
			}
		}
		if (loader != null) {
			return loader.getChildren(unit);
		}
		return NO_CHILDREN;
	}

	/**
	 * A variant of {@link DeferredTreeContentManager}. By adding a fixed {@link IDeferredWorkbenchAdapter}
	 * we avoid to implement an adapter for {@link ITranslationUnit} to {@code IDeferredWorkbenchAdapter}.
	 * This would also fail, because {@code cdt} has already a registered adapter from {@code ITranslationUnit}
	 * to {@code IDeferredWorkbenchAdapter}.
	 * It doesn't use a separate UI job to fill in the tree. With UI jobs, it's simply impossible
	 * to know what has already been added when there are several loading jobs.
	 * For our use case (load the whole symbols, then add it to the tree) a
	 * {@link org.eclipse.swt.widgets.Display#syncExec(Runnable) syncExec()} is
	 * sufficient.
	 */
	private static class DeferredCSymbolLoader extends DeferredTreeContentManager {
		private final IDeferredWorkbenchAdapter adapter;
		private final AbstractTreeViewer viewer;

		public DeferredCSymbolLoader(AbstractTreeViewer viewer, IDeferredWorkbenchAdapter adapter) {
			super(viewer);
			this.viewer = viewer;
			this.adapter = adapter;
		}

		@Override
		protected IDeferredWorkbenchAdapter getAdapter(Object element) {
			return adapter;
		}

		/**
		 * Add child nodes, removing the error element if appropriate. Contrary
		 * to the super implementation, this does <em>not</em> use a UI job but
		 * a simple {@link org.eclipse.swt.widgets.Display#syncExec(Runnable)
		 * syncExec()}.
		 *
		 * @param parent
		 *            to add the {@code children} to
		 * @param children
		 *            to add to the {@code parent}
		 * @param monitor
		 *            is ignored
		 */
		@Override
		protected void addChildren(Object parent, Object[] children, IProgressMonitor monitor) {
			Control control = viewer.getControl();
			if (control == null || control.isDisposed()) {
				return;
			}
			control.getDisplay().asyncExec(() -> {
				if (!control.isDisposed()) {
					try {
						control.setRedraw(false);
						if (children.length != 1 || children[0] != ERROR_ELEMENT) {
							viewer.remove(ERROR_ELEMENT);
						}
						viewer.add(parent, children);
					} finally {
						control.setRedraw(true);
					}
				}
			});
		}
	}

}
