package org.eclipse.cdt.lsp.editor;

import org.eclipse.cdt.internal.ui.text.spelling.CSpellingService;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.texteditor.spelling.SpellingReconcileStrategy;

public class CSpellingReconcileStrategy extends SpellingReconcileStrategy {

	public CSpellingReconcileStrategy(ISourceViewer viewer) {
		super(viewer, CSpellingService.getInstance());
	}

	@Override
	public void initialReconcile() {
		// Do nothing, since the fDocument can be null when Spelling gets enabled in the preference page:
		//reconcile(new Region(0, fDocument.getLength()));
	}

}
