/*******************************************************************************
 * Copyright (c) 2025 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.editor;

import java.text.BreakIterator;
import java.text.CharacterIterator;

import org.eclipse.cdt.internal.ui.text.CWordIterator;
import org.eclipse.cdt.internal.ui.text.DocumentCharacterIterator;
import org.eclipse.cdt.lsp.editor.EditorConfiguration;
import org.eclipse.cdt.lsp.editor.EditorOptions;
import org.eclipse.cdt.lsp.internal.switchtolsp.ISwitchBackToTraditional;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.TextNavigationAction;

@SuppressWarnings("restriction")
public class CLspEditor extends ExtensionBasedTextEditor {

	/**
	 * Text navigation action to navigate to the next sub-word.
	 *
	 * copied from org.eclipse.cdt.internal.ui.editor.CEditor
	 *
	 * @since 3.1
	 */
	protected abstract class NextSubWordAction extends TextNavigationAction {
		protected CWordIterator fIterator = new CWordIterator();

		/**
		 * Creates a new next sub-word action.
		 *
		 * @param code Action code for the default operation. Must be an action code from @see org.eclipse.swt.custom.ST.
		 */
		protected NextSubWordAction(int code) {
			super(getSourceViewer().getTextWidget(), code);
		}

		@Override
		public void run() {
			// Check whether sub word navigation is enabled.
			if (!getOptionEnableSubWordNavigation()) {
				super.run();
				return;
			}

			final ISourceViewer viewer = getSourceViewer();
			final IDocument document = viewer.getDocument();
			fIterator.setText((CharacterIterator) new DocumentCharacterIterator(document));
			int position = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset());
			if (position == -1)
				return;

			int next = findNextPosition(position);
			try {
				if (isBlockSelectionModeEnabled()
						&& document.getLineOfOffset(next) != document.getLineOfOffset(position)) {
					super.run(); // may navigate into virtual white space
				} else if (next != BreakIterator.DONE) {
					setCaretPosition(next);
					getTextWidget().showSelection();
					fireSelectionChanged();
				}
			} catch (BadLocationException x) {
				// ignore
			}
		}

		/**
		 * Finds the next position after the given position.
		 *
		 * @param position the current position
		 * @return the next position
		 */
		protected int findNextPosition(int position) {
			ISourceViewer viewer = getSourceViewer();
			int widget = -1;
			while (position != BreakIterator.DONE && widget == -1) { // TODO: optimize
				position = fIterator.following(position);
				if (position != BreakIterator.DONE)
					widget = modelOffset2WidgetOffset(viewer, position);
			}
			return position;
		}

		/**
		 * Sets the caret position to the sub-word boundary given with {@code position}.
		 *
		 * @param position Position where the action should move the caret
		 */
		protected abstract void setCaretPosition(int position);
	}

	/**
	 * Text navigation action to navigate to the next sub-word.
	 *
	 * copied from org.eclipse.cdt.internal.ui.editor.CEditor
	 *
	 * @since 3.1
	 */
	protected class NavigateNextSubWordAction extends NextSubWordAction {
		/**
		 * Creates a new navigate next sub-word action.
		 */
		public NavigateNextSubWordAction() {
			super(ST.WORD_NEXT);
		}

		@Override
		protected void setCaretPosition(final int position) {
			getTextWidget().setCaretOffset(modelOffset2WidgetOffset(getSourceViewer(), position));
		}
	}

	/**
	 * Text operation action to delete the next sub-word.
	 *
	 * copied from org.eclipse.cdt.internal.ui.editor.CEditor
	 *
	 * @since 3.1
	 */
	protected class DeleteNextSubWordAction extends NextSubWordAction implements IUpdate {
		/**
		 * Creates a new delete next sub-word action.
		 * copied from org.eclipse.cdt.internal.ui.editor.CEditor
		 */
		public DeleteNextSubWordAction() {
			super(ST.DELETE_WORD_NEXT);
		}

		@Override
		protected void setCaretPosition(final int position) {
			if (!validateEditorInputState())
				return;

			final ISourceViewer viewer = getSourceViewer();
			StyledText text = viewer.getTextWidget();
			Point widgetSelection = text.getSelection();
			if (isBlockSelectionModeEnabled() && widgetSelection.y != widgetSelection.x) {
				final int caret = text.getCaretOffset();
				final int offset = modelOffset2WidgetOffset(viewer, position);

				if (caret == widgetSelection.x)
					text.setSelectionRange(widgetSelection.y, offset - widgetSelection.y);
				else
					text.setSelectionRange(widgetSelection.x, offset - widgetSelection.x);
				text.invokeAction(ST.DELETE_NEXT);
			} else {
				Point selection = viewer.getSelectedRange();
				final int caret, length;
				if (selection.y != 0) {
					caret = selection.x;
					length = selection.y;
				} else {
					caret = widgetOffset2ModelOffset(viewer, text.getCaretOffset());
					length = position - caret;
				}

				try {
					viewer.getDocument().replace(caret, length, ""); //$NON-NLS-1$
				} catch (BadLocationException e) {
					// Should not happen
				}
			}
		}

		@Override
		protected int findNextPosition(int position) {
			return fIterator.following(position);
		}

		@Override
		public void update() {
			setEnabled(isEditorInputModifiable());
		}
	}

	/**
	 * Text operation action to select the next sub-word.
	 *
	 * copied from org.eclipse.cdt.internal.ui.editor.CEditor
	 *
	 * @since 3.1
	 */
	protected class SelectNextSubWordAction extends NextSubWordAction {
		/**
		 * Creates a new select next sub-word action.
		 */
		public SelectNextSubWordAction() {
			super(ST.SELECT_WORD_NEXT);
		}

		@Override
		protected void setCaretPosition(final int position) {
			final ISourceViewer viewer = getSourceViewer();

			final StyledText text = viewer.getTextWidget();
			if (text != null && !text.isDisposed()) {

				final Point selection = text.getSelection();
				final int caret = text.getCaretOffset();
				final int offset = modelOffset2WidgetOffset(viewer, position);

				if (caret == selection.x)
					text.setSelectionRange(selection.y, offset - selection.y);
				else
					text.setSelectionRange(selection.x, offset - selection.x);
			}
		}
	}

	/**
	 * Text navigation action to navigate to the previous sub-word.
	 *
	 * copied from org.eclipse.cdt.internal.ui.editor.CEditor
	 *
	 * @since 3.1
	 */
	protected abstract class PreviousSubWordAction extends TextNavigationAction {
		protected CWordIterator fIterator = new CWordIterator();

		/**
		 * Creates a new previous sub-word action.
		 *
		 * @param code Action code for the default operation. Must be an action code from @see org.eclipse.swt.custom.ST.
		 */
		protected PreviousSubWordAction(final int code) {
			super(getSourceViewer().getTextWidget(), code);
		}

		@Override
		public void run() {
			// Check whether sub word navigation is enabled.
			if (!getOptionEnableSubWordNavigation()) {
				super.run();
				return;
			}

			final ISourceViewer viewer = getSourceViewer();
			final IDocument document = viewer.getDocument();
			fIterator.setText((CharacterIterator) new DocumentCharacterIterator(document));
			int position = widgetOffset2ModelOffset(viewer, viewer.getTextWidget().getCaretOffset());
			if (position == -1)
				return;

			int previous = findPreviousPosition(position);
			try {
				if (isBlockSelectionModeEnabled()
						&& document.getLineOfOffset(previous) != document.getLineOfOffset(position)) {
					super.run(); // may navigate into virtual white space
				} else if (previous != BreakIterator.DONE) {
					setCaretPosition(previous);
					getTextWidget().showSelection();
					fireSelectionChanged();
				}
			} catch (BadLocationException e) {
				// ignore - getLineOfOffset failed
			}
		}

		/**
		 * Finds the previous position before the given position.
		 *
		 * @param position the current position
		 * @return the previous position
		 */
		protected int findPreviousPosition(int position) {
			ISourceViewer viewer = getSourceViewer();
			int widget = -1;
			while (position != BreakIterator.DONE && widget == -1) { // TODO: optimize
				position = fIterator.preceding(position);
				if (position != BreakIterator.DONE)
					widget = modelOffset2WidgetOffset(viewer, position);
			}
			return position;
		}

		/**
		 * Sets the caret position to the sub-word boundary given with {@code position}.
		 *
		 * @param position Position where the action should move the caret
		 */
		protected abstract void setCaretPosition(int position);
	}

	/**
	 * Text navigation action to navigate to the previous sub-word.
	 *
	 * copied from org.eclipse.cdt.internal.ui.editor.CEditor
	 *
	 * @since 3.1
	 */
	protected class NavigatePreviousSubWordAction extends PreviousSubWordAction {

		/**
		 * Creates a new navigate previous sub-word action.
		 */
		public NavigatePreviousSubWordAction() {
			super(ST.WORD_PREVIOUS);
		}

		@Override
		protected void setCaretPosition(final int position) {
			getTextWidget().setCaretOffset(modelOffset2WidgetOffset(getSourceViewer(), position));
		}
	}

	/**
	 * Text operation action to delete the previous sub-word.
	 *
	 * copied from org.eclipse.cdt.internal.ui.editor.CEditor
	 *
	 * @since 3.1
	 */
	protected class DeletePreviousSubWordAction extends PreviousSubWordAction implements IUpdate {
		/**
		 * Creates a new delete previous sub-word action.
		 */
		public DeletePreviousSubWordAction() {
			super(ST.DELETE_WORD_PREVIOUS);
		}

		@Override
		protected void setCaretPosition(int position) {
			if (!validateEditorInputState())
				return;

			final int length;
			final ISourceViewer viewer = getSourceViewer();
			StyledText text = viewer.getTextWidget();
			Point widgetSelection = text.getSelection();
			if (isBlockSelectionModeEnabled() && widgetSelection.y != widgetSelection.x) {
				final int caret = text.getCaretOffset();
				final int offset = modelOffset2WidgetOffset(viewer, position);

				if (caret == widgetSelection.x)
					text.setSelectionRange(widgetSelection.y, offset - widgetSelection.y);
				else
					text.setSelectionRange(widgetSelection.x, offset - widgetSelection.x);
				text.invokeAction(ST.DELETE_PREVIOUS);
			} else {
				Point selection = viewer.getSelectedRange();
				if (selection.y != 0) {
					position = selection.x;
					length = selection.y;
				} else {
					length = widgetOffset2ModelOffset(viewer, text.getCaretOffset()) - position;
				}

				try {
					viewer.getDocument().replace(position, length, ""); //$NON-NLS-1$
				} catch (BadLocationException e) {
					// Should not happen
				}
			}
		}

		@Override
		protected int findPreviousPosition(int position) {
			return fIterator.preceding(position);
		}

		@Override
		public void update() {
			setEnabled(isEditorInputModifiable());
		}
	}

	/**
	 * Text operation action to select the previous sub-word.
	 *
	 * copied from org.eclipse.cdt.internal.ui.editor.CEditor
	 *
	 * @since 3.1
	 */
	protected class SelectPreviousSubWordAction extends PreviousSubWordAction {
		/**
		 * Creates a new select previous sub-word action.
		 */
		public SelectPreviousSubWordAction() {
			super(ST.SELECT_WORD_PREVIOUS);
		}

		@Override
		protected void setCaretPosition(final int position) {
			final ISourceViewer viewer = getSourceViewer();

			final StyledText text = viewer.getTextWidget();
			if (text != null && !text.isDisposed()) {
				final Point selection = text.getSelection();
				final int caret = text.getCaretOffset();
				final int offset = modelOffset2WidgetOffset(viewer, position);

				if (caret == selection.x) {
					text.setSelectionRange(selection.y, offset - selection.y);
				} else {
					text.setSelectionRange(selection.x, offset - selection.x);
				}
			}
		}
	}

	private static final String CONTEXT_ID = "org.eclipse.cdt.lsp.cEditorContext"; //$NON-NLS-1$

	@Override
	protected void createNavigationActions() {
		super.createNavigationActions();

		final StyledText textWidget = getSourceViewer().getTextWidget();

		IAction action = new NavigatePreviousSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_PREVIOUS);
		setAction(ITextEditorActionDefinitionIds.WORD_PREVIOUS, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.ARROW_LEFT, SWT.NULL);

		action = new NavigateNextSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.WORD_NEXT);
		setAction(ITextEditorActionDefinitionIds.WORD_NEXT, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.ARROW_RIGHT, SWT.NULL);

		action = new SelectPreviousSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS);
		setAction(ITextEditorActionDefinitionIds.SELECT_WORD_PREVIOUS, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.SHIFT | SWT.ARROW_LEFT, SWT.NULL);

		action = new SelectNextSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT);
		setAction(ITextEditorActionDefinitionIds.SELECT_WORD_NEXT, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.SHIFT | SWT.ARROW_RIGHT, SWT.NULL);

		action = new DeletePreviousSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD);
		setAction(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.BS, SWT.NULL);
		markAsStateDependentAction(ITextEditorActionDefinitionIds.DELETE_PREVIOUS_WORD, true);

		action = new DeleteNextSubWordAction();
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD);
		setAction(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD, action);
		textWidget.setKeyBinding(SWT.CTRL | SWT.DEL, SWT.NULL);
		markAsStateDependentAction(ITextEditorActionDefinitionIds.DELETE_NEXT_WORD, true);
	}

	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { CONTEXT_ID });
	}

	@Override
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		Composite editorComposite = createSwitchBackToTraditionalEditorBanner(parent);
		return super.createSourceViewer(editorComposite, ruler, styles);
	}

	/**
	 * Wraps {@link ISwitchBackToTraditional#createSwitchBackToTraditional(org.eclipse.ui.texteditor.ITextEditor, Composite)}
	 * with the needed service access + fallback checks.
	 *
	 * If the {@link ISwitchBackToTraditional} service doesn't exist, or fails, this method
	 * is a no-op that simply returns its input.
	 *
	 * @see ISwitchBackToTraditional#createSwitchBackToTraditional(org.eclipse.ui.texteditor.ITextEditor, Composite)
	 */
	private Composite createSwitchBackToTraditionalEditorBanner(Composite parent) {
		Composite editorComposite = SafeRunner.run(() -> {
			ISwitchBackToTraditional switchToLsp = PlatformUI.getWorkbench().getService(ISwitchBackToTraditional.class);
			if (switchToLsp != null) {
				return switchToLsp.createSwitchBackToTraditional(CLspEditor.this, parent);
			}
			return null;
		});
		if (editorComposite == null) {
			editorComposite = parent;
		}
		return editorComposite;
	}

	private boolean getOptionEnableSubWordNavigation() {
		EditorConfiguration configuration = PlatformUI.getWorkbench().getService(EditorConfiguration.class);
		if (configuration == null) {
			return false;
		}

		EditorOptions options = null;
		IWorkspace workspace = PlatformUI.getWorkbench().getService(IWorkspace.class);
		options = configuration.options(workspace);

		if (options == null) {
			return false;
		}
		return options.enableSubWordNavigation();
	}

}
