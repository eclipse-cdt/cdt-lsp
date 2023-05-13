/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 * Alexander Fedorov (ArSysOp) - use Platform for logging
 *******************************************************************************/

package org.eclipse.cdt.lsp.editor.ui.clangd;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
import org.eclipse.cdt.lsp.LspPlugin;
import org.eclipse.cdt.lsp.LspUtils;
import org.eclipse.cdt.lsp.editor.ui.LspEditorUiMessages;
import org.eclipse.cdt.lsp.editor.ui.LspEditorUiPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.yaml.snakeyaml.scanner.ScannerException;

public class CProjectChangeMonitor {
	
	private final ICProjectDescriptionListener listener = new ICProjectDescriptionListener() {

		@Override
		public void handleEvent(CProjectDescriptionEvent event) {
			ICProjectDescription newCProjectDecription = event.getNewCProjectDescription();
			if (newCProjectDecription != null) {
				IProject project = event.getProject();
				boolean isEnabled = Optional.ofNullable(LspPlugin.getDefault()).map(LspPlugin::getCLanguageServerProvider).map(provider -> provider.isEnabledFor(project)).orElse(Boolean.FALSE);
				if (project != null && isEnabled) {
					ICConfigurationDescription newConfig = newCProjectDecription.getDefaultSettingConfiguration();
					var cwdBuilder = newConfig.getBuildSetting().getBuilderCWD();
					if (cwdBuilder != null) {
						try {
							var cwdString = CCorePlugin.getDefault().getCdtVariableManager().resolveValue(cwdBuilder.toOSString(), "", null, newConfig);
							var projectLocation = project.getLocation().addTrailingSeparator().toOSString();
							var databasePath = cwdString.replace(projectLocation, "");
							try {
								ClangdConfigurationManager.setCompilationDatabase(project, databasePath);
							} catch (ScannerException e) {
								var status = new Status(IStatus.ERROR, LspEditorUiPlugin.PLUGIN_ID, e.getMessage());
								var configFile = ClangdConfigurationManager.CLANGD_CONFIG_FILE_NAME;
								LspUtils.showErrorMessage(LspEditorUiMessages.CProjectChangeMonitor_yaml_scanner_error, 
										LspEditorUiMessages.CProjectChangeMonitor_yaml_scanner_error_message + projectLocation + configFile , status);
							}
						} catch (CoreException e) {
							Platform.getLog(getClass()).log(e.getStatus());
						} catch (IOException e) {
							Platform.getLog(getClass()).error(e.getMessage(), e);
						}
					}
				}			
			}
		}
		
	};
	
	public CProjectChangeMonitor start() {
		CCorePlugin.getDefault().getProjectDescriptionManager().addCProjectDescriptionListener(listener, CProjectDescriptionEvent.APPLIED);
		return this;
	}
	
	public void stop() {
		CCorePlugin.getDefault().getProjectDescriptionManager().removeCProjectDescriptionListener(listener);
	}

}
