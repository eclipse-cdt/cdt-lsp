package org.eclipse.cdt.lsp.editor;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.lsp4e.IFormatOnSave;

public class FormatOnSave implements IFormatOnSave {

	@Override
	public boolean isEnabledFor(IDocument document) {
		return true;
	}

	@Override
	public IRegion[] getFormattingRegions(ITextFileBuffer buffer) {
		return new IRegion[] { new Region(0, buffer.getDocument().getLength()) };
	}

}
