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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.navigator.CNavigatorContentProvider;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.lsp4e.outline.SymbolsModel.DocumentSymbolWithURI;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;

public class CSymbolsContentProvider extends CNavigatorContentProvider {

	private final SymbolsManager symbolsManager = SymbolsManager.INSTANCE;
	private DeferredCSymbolLoader loader;
	private Object currentInput;

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
			loader.addUpdateCompleteListener(new JobChangeAdapter() {

				@Override
				public void done(IJobChangeEvent event) {
					if (event.getResult().isOK()) {
						// Force a selection event
						viewer.setSelection(viewer.getSelection());
					}
				}
			});
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
		} else if (parentElement instanceof ITranslationUnit) {
			try {
				return getTranslationUnitChildren((ITranslationUnit) parentElement);
			} catch (CModelException e) {
			}
		}
		return NO_CHILDREN;
	}

	@Override
	protected Object[] getTranslationUnitChildren(ITranslationUnit unit) throws CModelException {
		if (loader != null) {
			return loader.getChildren(unit);
		}
		return NO_CHILDREN;
	}

	private static class DeferredCSymbolLoader extends DeferredTreeContentManager {
		private final IDeferredWorkbenchAdapter adapter;

		public DeferredCSymbolLoader(AbstractTreeViewer viewer, IDeferredWorkbenchAdapter adapter) {
			super(viewer);
			this.adapter = adapter;
		}

		@Override
		protected IDeferredWorkbenchAdapter getAdapter(Object element) {
			return adapter;
		}
	}

}
