/*******************************************************************************
 * Copyright (c) 2024 Bachmann electronic GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.reconciler.Reconciler;
import org.eclipse.jface.text.source.ISourceViewer;

public final class CSpellingReconciler extends Reconciler {

	@Override
	public void install(ITextViewer textViewer) {
		if (textViewer instanceof ISourceViewer sourceViewer) {
			this.setReconcilingStrategy(new CSpellingReconcileStrategy(sourceViewer), IDocument.DEFAULT_CONTENT_TYPE);
		}
		// call super.install AFTER the CSpellingReconcileStrategy has been added to the super class via setReconcilingStrategy call,
		// otherwise reconcilerDocumentChanged (which is called during super.install) would not be performed on our CSpellingReconcileStrategy
		super.install(textViewer);
	}
}
