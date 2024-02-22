/*******************************************************************************
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.editor;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.cdt.ui.text.c.hover.ICEditorTextHover;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;

/**
 * Delegates hover to the text hover implementation of cdt.
 */
public class DebugHoverProvider implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {
	@Override
	public IInformationControlCreator getHoverControlCreator() {
		return getDelegate(ITextHoverExtension.class).map(ITextHoverExtension::getHoverControlCreator).orElse(null);
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		return getDelegate(ITextHover.class).map(delegate -> delegate.getHoverInfo(textViewer, hoverRegion))
				.orElse(null);
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return getDelegate(ITextHover.class).map(delegate -> delegate.getHoverRegion(textViewer, offset)).orElse(null);
	}

	@Override
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion) {
		return getDelegate(ITextHoverExtension2.class).map(delegate -> delegate.getHoverInfo2(textViewer, hoverRegion))
				.orElse(null);
	}

	private <T> Optional<T> getDelegate(Class<T> clazz) {
		return Optional.ofNullable(DebugUITools.getDebugContext())
				.map(adaptable -> adaptable.getAdapter(ICEditorTextHover.class)).filter(Objects::nonNull)
				.filter(clazz::isInstance).map(clazz::cast);
	}

}
