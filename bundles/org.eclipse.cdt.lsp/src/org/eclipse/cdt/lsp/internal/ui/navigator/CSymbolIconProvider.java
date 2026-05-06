/*******************************************************************************
 * Copyright (c) 2026 Advantest GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Dietrich Travkin (Solunar GmbH) - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.ui.navigator;

import java.util.List;

import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.lsp4e.ui.SymbolIconProvider;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.SymbolTag;

public class CSymbolIconProvider extends SymbolIconProvider {

	private static ImageDescriptor IMG_DESTRUCTOR_OVERLAY = null;

	private ImageDescriptor getDestructorOverlay() {
		if (IMG_DESTRUCTOR_OVERLAY == null) {
			IMG_DESTRUCTOR_OVERLAY = LspPlugin.getDefault().getImageDescriptor(LspPlugin.IMG_OVR_DESTRUCTOR);
		}
		return IMG_DESTRUCTOR_OVERLAY;
	}

	@Override
	protected Overlays getOverlaysFor(final SymbolKind symbolKind, final List<SymbolTag> symbolTags, int severity,
			Object symbol) {

		final var overlays = super.getOverlaysFor(symbolKind, symbolTags, severity, symbol);

		if (symbolKind == SymbolKind.Constructor) {
			String name = getName(symbol);
			// if the symbol's name represents a destructor, e.g. is named MyType::~MyType
			if (name != null && name.contains("~")) { //$NON-NLS-1$
				// then replace the constructor overlay with a destructor overlay
				ImageDescriptor destructorOverlayDescriptor = getDestructorOverlay();
				return new Overlays(overlays.topLeft, destructorOverlayDescriptor, overlays.bottomLeft,
						overlays.bottomRight, overlays.underlay);
			}
		}

		return overlays;
	}

}
