package org.eclipse.cdt.lsp.editor;

import org.eclipse.cdt.internal.ui.text.spelling.CSpellingService;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;

public class CSpellingReconcileStrategy extends SpellingReconcileStrategy {

	public CSpellingReconcileStrategy(ISourceViewer viewer) {
		super(viewer, CSpellingService.getInstance());
	}

	@Override
	public void initialReconcile() {
		var document = getDocument();
		if (document != null)
			reconcile(new Region(0, document.getLength()));
	}

	@Override
	public void setDocument(IDocument document) {
		super.setDocument(document);
		//initialReconcile();
	}

	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		if (getDocument() != null)
			super.reconcile(dirtyRegion, subRegion);
	}

}
