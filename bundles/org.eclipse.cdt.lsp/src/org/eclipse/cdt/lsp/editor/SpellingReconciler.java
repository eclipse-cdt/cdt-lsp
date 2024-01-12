package org.eclipse.cdt.lsp.editor;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.Reconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;

public class SpellingReconciler extends Reconciler {
	private ISourceViewer viewer;

	@Override
	public void install(ITextViewer textViewer) {
		// TODO Auto-generated method stub
		if (textViewer instanceof ISourceViewer)
			this.viewer = (ISourceViewer) textViewer;
	}

	@Override
	public void uninstall() {
		// TODO Auto-generated method stub

	}

	@Override
	public IReconcilingStrategy getReconcilingStrategy(String contentType) {
		// TODO Auto-generated method stub
		if (viewer != null)
			return new SpellingReconcileStrategy(viewer, EditorsUI.getSpellingService());
		return null;
	}

	//	@Override
	//	protected void process(DirtyRegion dirtyRegion) {
	//		// TODO Auto-generated method stub
	//
	//	}
	//
	//	@Override
	//	protected void reconcilerDocumentChanged(IDocument newDocument) {
	//		// TODO Auto-generated method stub
	//
	//	}

}
