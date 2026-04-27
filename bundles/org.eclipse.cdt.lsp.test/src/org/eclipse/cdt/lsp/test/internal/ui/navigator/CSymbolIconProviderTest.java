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
package org.eclipse.cdt.lsp.test.internal.ui.navigator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.List;

import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.lsp4e.outline.SymbolsLabelProvider;
import org.eclipse.lsp4e.outline.SymbolsModel.DocumentSymbolWithURI;
import org.eclipse.lsp4e.ui.LSPImages;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.SymbolTag;
import org.eclipse.swt.graphics.Image;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CSymbolIconProviderTest {

	@ParameterizedTest
	@CsvSource({
	// @formatter:off
		"~SomeType, true",
		"SomeType, false",
		"SomeType::~SomeType, true",
		"SomeType::SomeType, false"
	// @formatter:on
	})
	public void testCustomDestructorOverlayIconUsed(String symbolName, boolean isDestructor) {
		DocumentSymbol documentSymbol = new DocumentSymbol();

		// LSP doesn't know destructors, they are modelled as constructors with a ~ in their name
		documentSymbol.setKind(SymbolKind.Constructor);

		documentSymbol.setName(symbolName);
		documentSymbol.setTags(List.of(SymbolTag.Public));
		DocumentSymbolWithURI symbol = new DocumentSymbolWithURI(documentSymbol,
				URI.create("file:///home/username/some/path/SomeType.cpp"));
		SymbolsLabelProvider labelProvider = new SymbolsLabelProvider();

		ImageDescriptor topRightOverlay;
		if (isDestructor) {
			topRightOverlay = LspPlugin.getDefault().getImageDescriptor(LspPlugin.IMG_OVR_DESTRUCTOR);
		} else {
			topRightOverlay = LSPImages.getImageDescriptor(LSPImages.IMG_OVR_CONSTRUCTOR);
		}
		Image expectedImage = LSPImages.getImageWithOverlays(
				// base image for a public method
				LSPImages.IMG_METHOD_VIS_PUBLIC,
				// no overlay icons except of our destructor overlay in the top right corner
				null, topRightOverlay, null, null, null);

		Image actualImage = labelProvider.getImage(symbol);

		assertEquals(expectedImage, actualImage);
	}

}
