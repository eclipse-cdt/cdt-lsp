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

import org.eclipse.cdt.lsp.config.Configuration;
import org.eclipse.cdt.lsp.editor.EditorOptions;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.format.IFormatRegionsProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(property = { "serverDefinitionId:String=org.eclipse.cdt.lsp.server" })
public class FormatOnSave implements IFormatRegionsProvider {

	@Reference
	private Configuration configuration;

	@Override
	public IRegion[] getFormattingRegions(IDocument document) {
		var file = LSPEclipseUtils.getFile(document);
		if (file != null) {
			var editorOptions = (EditorOptions) configuration.options(file);
			if (editorOptions != null && editorOptions.formatOnSave()) {
				if (editorOptions.formatAllLines()) {
					return IFormatRegionsProvider.allLines(document);
				}
				if (editorOptions.formatEditedLines()) {
					return IFormatRegionsProvider.calculateEditedLineRegions(document, new NullProgressMonitor());
				}
			}
		}
		return null;
	}

}
