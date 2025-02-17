/*******************************************************************************
 * Copyright (c) 2025 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.switchtolsp;

import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.texteditor.ITextEditor;

public class SwitchToLspBannerTraditional extends SwitchToLspBanner {

	public SwitchToLspBannerTraditional(ITextEditor part) {
		super(part);
	}

	@Override
	protected String tryLspBannerLink() {
		return Messages.SwitchToLsp_SwitchBackBannerLink + LinkHelper.LINK_SPACER + LinkHelper.getLinks(false);
	}

	@Override
	protected void tryLspAction(Event event) {
		if (!LinkHelper.handleLinkClick(getPart().getSite().getShell(), event)) {
			new SwitchToLspWizard().startSwitch(getPart(), false);
		}

	}
}
