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

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = Messages.class.getPackageName() + ".messages"; //$NON-NLS-1$
	public static String SwitchToLsp_LearnMoreMessage;
	public static String SwitchToLsp_NewExperienceTitile;
	public static String SwitchToLsp_GiveFeedbackLink;
	public static String SwitchToLsp_Cancel;
	public static String SwitchToLsp_DontShowThisBannerAgainLink;
	public static String SwitchToLsp_EditorsWitllReopenToClassicExperience;
	public static String SwitchToLsp_EditorsWitllReopenToNewExperience;
	public static String SwitchToLsp_LearnMoreLink;
	public static String SwitchToLsp_NewExperienceTitle;
	public static String SwitchToLsp_OpenPreferencesLink;
	public static String SwitchToLsp_OpenProjectSettings;
	public static String SwitchToLsp_ProjectSpecificSettingsLabel;
	public static String SwitchToLsp_SwitchBackBannerLink;
	public static String SwitchToLsp_UseClassicExperience;
	public static String SwitchToLsp_UseNewExperience;
	public static String SwitchToLsp_TryNewExperienceBannerLink;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
