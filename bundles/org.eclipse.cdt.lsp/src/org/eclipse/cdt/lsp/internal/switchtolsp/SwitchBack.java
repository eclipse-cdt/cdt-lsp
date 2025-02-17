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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.texteditor.ITextEditor;

public class SwitchBack implements ISwitchBackToTraditional {

	@Override
	public Composite createSwitchBackToTraditional(ITextEditor part, Composite parent) {
		SwitchToLspBanner banner = new SwitchToLspBannerTraditional(part);
		return banner.create(parent);
	}
}
