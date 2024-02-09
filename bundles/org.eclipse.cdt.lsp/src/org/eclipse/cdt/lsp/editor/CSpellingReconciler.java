package org.eclipse.cdt.lsp.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.Reconciler;
import org.eclipse.jface.text.source.ISourceViewer;

public class CSpellingReconciler extends Reconciler {
	private CSpellingReconcileStrategy strategy;

	@Override
	public void install(ITextViewer textViewer) {
		super.install(textViewer);
		if (textViewer instanceof ISourceViewer sourceViewer) {
			strategy = new CSpellingReconcileStrategy(sourceViewer);
			this.setReconcilingStrategy(strategy, IDocument.DEFAULT_CONTENT_TYPE);
		}
	}
}
