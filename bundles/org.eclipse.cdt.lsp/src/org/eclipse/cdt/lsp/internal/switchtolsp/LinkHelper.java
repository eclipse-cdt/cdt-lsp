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

import java.util.ArrayList;

import org.eclipse.cdt.lsp.ui.EditorConfigurationPage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class LinkHelper {

	public static final String SPARKLES = "\u2728 "; //$NON-NLS-1$
	public static final String LINK_SPACER = " · "; //$NON-NLS-1$

	public static String A(String linkText) {
		return "<a>" + linkText + "</a>"; //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Creates the standard set of links to show in notifications and message dialogs.
	 *
	 * Some workflows don't make sense to show the preferences link, such as confirmation
	 * dialog.
	 * @return string of links that {@link #handleLinkClick(Shell, Event)} will be able to process
	 */
	public static String getLinks(boolean showPreferenceLink) {
		StringBuilder message = new StringBuilder();
		var texts = new ArrayList<String>();
		texts.add(A(Messages.SwitchToLsp_LearnMoreLink));
		if (showPreferenceLink) {
			texts.add(A(Messages.SwitchToLsp_OpenPreferencesLink));
		}
		texts.add(A(Messages.SwitchToLsp_GiveFeedbackLink));
		message.append(String.join(LINK_SPACER, texts));
		return message.toString();
	}

	/**
	 * Handler for the links created by {@link #getLinks(boolean)}
	 */
	public static boolean handleLinkClick(Shell parentShell, Event event) {
		if (event.text != null && event.text.equals(Messages.SwitchToLsp_LearnMoreLink)) {
			PlatformUI.getWorkbench().getHelpSystem().displayHelpResource(SwitchToLspWizard.TRY_LSP_HELP_PATH);
		} else if (event.text != null && event.text.equals(Messages.SwitchToLsp_OpenPreferencesLink)) {
			PreferenceDialog preferenceDialogOn = PreferencesUtil.createPreferenceDialogOn(parentShell,
					EditorConfigurationPage.PREFERENCE_PAGE_ID, null /* display all pages */,
					EditorConfigurationPage.HIGHLIGHT_PREFER_LSP);
			preferenceDialogOn.setBlockOnOpen(false);
			preferenceDialogOn.open();
		} else if (event.text != null && event.text.equals(Messages.SwitchToLsp_GiveFeedbackLink)) {
			Program.launch(SwitchToLspWizard.FEEDBACK_URL);
		} else {
			// Did not detect a link - do nothing
			return false;
		}
		return true;
	}

}
