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

//TODO: comment this code in when https://github.com/eclipse/lsp4e/pull/783 has been merged

//package org.eclipse.cdt.lsp.editor;
//
//import org.eclipse.jface.text.IDocument;
//import org.eclipse.jface.text.IRegion;
//import org.eclipse.jface.text.Region;
//import org.eclipse.lsp4e.LSPEclipseUtils;
//import org.eclipse.lsp4e.format.FormatEditedLines;
//import org.eclipse.lsp4e.format.IFormatRegionsProvider;
//import org.osgi.service.component.annotations.Component;
//import org.osgi.service.component.annotations.Reference;
//
//@Component(property = { "serverDefinitionId:String=org.eclipse.cdt.lsp.server" })
//public class FormatOnSave implements IFormatRegionsProvider {
//	private final FormatEditedLines formatEditedLines = new FormatEditedLines();
//
//	@Reference
//	private EditorConfiguration configuration;
//
//	@Override
//	public IRegion[] getFormattingRegions(IDocument document) {
//		var file = LSPEclipseUtils.getFile(document);
//		if (file != null) {
//			var editorOptions = configuration.options(file);
//			if (editorOptions != null && editorOptions.formatOnSave()) {
//				if (editorOptions.formatAllLines()) {
//					return new IRegion[] { new Region(0, document.getLength()) };
//				}
//				if (editorOptions.formatEditedLines()) {
//					return formatEditedLines.getFormattingRegions(document);
//				}
//			}
//		}
//		return null;
//	}
//
//}
