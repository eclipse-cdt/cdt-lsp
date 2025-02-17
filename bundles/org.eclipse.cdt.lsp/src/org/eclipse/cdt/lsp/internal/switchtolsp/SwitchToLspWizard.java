/*******************************************************************************
 * Copyright (c) 2024, 2025 Kichwa Coders Canada Inc. and others.
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
import java.util.List;

import org.eclipse.cdt.lsp.ResolveProjectScope;
import org.eclipse.cdt.lsp.editor.ConfigurationVisibility;
import org.eclipse.cdt.lsp.editor.EditorConfiguration;
import org.eclipse.cdt.lsp.editor.EditorMetadata;
import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.cdt.lsp.ui.EditorConfigurationPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.notifications.NotificationPopup;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class SwitchToLspWizard {
	public static final String TRY_LSP_HELP_PATH = //
			"/org.eclipse.cdt.lsp.doc/html/lsp_cpp_editor.html"; //$NON-NLS-1$
	public static final String FEEDBACK_URL = "https://github.com/eclipse-cdt/cdt-lsp/issues/new/choose"; //$NON-NLS-1$

	public void startSwitch(ITextEditor editor, boolean newExperience) {

		boolean hasProjectSpecificSetting = false;
		IProject project = null;
		IEditorInput editorInput = editor.getEditorInput();
		if (editorInput instanceof IFileEditorInput fileEditorInput) {
			project = fileEditorInput.getFile().getProject();
			hasProjectSpecificSetting = hasProjectSpecificSettingForPreferLspEditor(project);
		}

		Shell shell = editor.getSite().getShell();
		if (hasProjectSpecificSetting && project != null) {
			if (promptToOpenProjectProperties(shell, project)) {
				PreferenceDialog preferenceDialogOn = PreferencesUtil.createPropertyDialogOn(shell, project,
						EditorConfigurationPage.PROPERTY_PAGE_ID, null /* display all pages */,
						EditorConfigurationPage.HIGHLIGHT_PREFER_LSP);
				preferenceDialogOn.setBlockOnOpen(false);
				preferenceDialogOn.open();
			}
			return;
		}

		if (openConfirmation(shell, newExperience)) {
			boolean saveAllEditors = IDE.saveAllEditors(new IResource[] { ResourcesPlugin.getWorkspace().getRoot() },
					true);
			if (saveAllEditors) {
				doIt(editor, newExperience);
			}
		}
	}

	/**
	 * Return true if prefer LSP checkbox is visible for context of the given editor.
	 */
	public static boolean showPreferLsp(ITextEditor editor) {
		ConfigurationVisibility visibility = PlatformUI.getWorkbench().getService(ConfigurationVisibility.class);
		boolean hasProjectSpecificSetting = false;
		if (editor != null) {
			IEditorInput editorInput = editor.getEditorInput();
			if (editorInput instanceof IFileEditorInput fileEditorInput) {
				IProject project = fileEditorInput.getFile().getProject();
				hasProjectSpecificSetting = SwitchToLspWizard.hasProjectSpecificSettingForPreferLspEditor(project);
			}
		}
		boolean showBanner = visibility.showPreferLsp(hasProjectSpecificSetting);
		return showBanner;
	}

	/**
	 * Return true if the given project has project specific setting for preferLspEditor
	 */
	public static boolean hasProjectSpecificSettingForPreferLspEditor(IProject project) {
		if (project == null) {
			return false;
		}
		IWorkspace workspace = PlatformUI.getWorkbench().getService(IWorkspace.class);
		// If the given project already has project specific settings, then open
		// preferences instead of default flow
		EditorConfiguration configuration = PlatformUI.getWorkbench().getService(EditorConfiguration.class);
		return new ResolveProjectScope(workspace).apply(project) //
				.map(p -> p.getNode(configuration.qualifier())) //
				.map(n -> n.get(EditorMetadata.Predefined.preferLspEditor.identifer(), null)) //
				.isPresent();
	}

	private void doIt(ITextEditor editor, boolean newExperience) {
		IWorkspace workspace = PlatformUI.getWorkbench().getService(IWorkspace.class);
		ServiceCaller.callOnce(SwitchToLspWizard.class, EditorConfiguration.class, //
				cc -> cc.storage(workspace).save(newExperience, EditorMetadata.Predefined.preferLspEditor));

		IEditorInput editorInput = editor.getEditorInput();
		IWorkbenchPage page = editor.getSite().getPage();

		reopenEditors(page, editorInput, newExperience);
		// null the editor because after reopenEditors the editor may not be valid anymore
		editor = null;

		notifyUserWhatsHappening(page.getWorkbenchWindow().getShell(), newExperience);
	}

	/**
	 * Reopen all editors.
	 *
	 * @implNote This code tries to make sure editors are not unnecessarily activated by
	 * dealing with {@link IEditorReference} and handling editors per page so that only
	 * one editor per page needs to be activated.
	 *
	 * firstInput/firstPage is to ensure that pair of input/page is the active editor on reopening.
	 *
	 * @param firstInput the editor input to bring to the top, can be null
	 * @param firstPage the page of the editor input to bring to the top, can be null
	 * @param newExperience whether the info is for new experience, or back to traditional editor
	 */
	private void reopenEditors(IWorkbenchPage firstPage, IEditorInput firstEditorInput, boolean newExperience) {
		for (var info : collectAllEditorRefsToReopen(firstEditorInput, firstPage, newExperience)) {
			SafeRunner.run(() -> {
				info.page.closeEditors(info.refsToClose, false);
				IEditorReference[] refsToOpen = info.page.openEditors(info.editorInputs, info.editorIds,
						IWorkbenchPage.MATCH_INPUT);

				// XXX: openEditors doesn't open the editors enough and on restart the editors that
				// were never examined are broken. See https://github.com/eclipse-platform/eclipse.platform.ui/issues/2805
				// Workaround is to force restore of all these editors
				for (IEditorReference ref : refsToOpen) {
					ref.getEditor(true);
				}
			});
		}
	}

	/**
	 * This contains the info needed to close and reopen editors
	 */
	private static class InfoOnEditorsToReopen {
		private IWorkbenchPage page;
		private IEditorReference[] refsToClose;
		private IEditorInput[] editorInputs;
		private String[] editorIds;
	}

	/**
	 * Collect all the editors that need to be closed, along with the info on what needs to be reopened.
	 *
	 * firstInput/firstPage is to ensure that pair of input/page is the active editor on reopening.
	 *
	 * @param firstInput the editor input to bring to the top, can be null
	 * @param firstPage the page of the editor input to bring to the top, can be null
	 * @param newExperience whether the info is for new experience, or back to traditional editor
	 * @return set of infos on all pages
	 */
	private List<InfoOnEditorsToReopen> collectAllEditorRefsToReopen(IEditorInput firstInput, IWorkbenchPage firstPage,
			boolean newExperience) {
		String editorIdToClose = newExperience ? LspPlugin.C_EDITOR_ID : LspPlugin.LSP_C_EDITOR_ID;
		String editorIdToOpen = newExperience ? LspPlugin.LSP_C_EDITOR_ID : LspPlugin.C_EDITOR_ID;
		List<InfoOnEditorsToReopen> allCdtEditors = new ArrayList<>();
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			IWorkbenchPage[] pages = window.getPages();
			for (IWorkbenchPage page : pages) {
				IEditorReference[] editorRefs = page.getEditorReferences();
				List<IEditorReference> cEditorsOnThisPage = new ArrayList<>();
				List<IEditorInput> editorInputs = new ArrayList<>();
				List<String> editorIds = new ArrayList<>();
				for (var editorRef : editorRefs) {
					String id = editorRef.getId();
					if (editorIdToClose.equals(id)) {
						IEditorInput editorInput = SafeRunner.run(() -> editorRef.getEditorInput());
						if (editorInput != null) {
							if (editorInput.equals(firstInput) && page.equals(firstPage)) {
								cEditorsOnThisPage.addFirst(editorRef);
								editorInputs.addFirst(editorInput);
								editorIds.addFirst(editorIdToOpen);
							} else {
								cEditorsOnThisPage.add(editorRef);
								editorInputs.add(editorInput);
								editorIds.add(editorIdToOpen);
							}
						}
					}
				}
				InfoOnEditorsToReopen info = new InfoOnEditorsToReopen();
				info.page = page;
				info.refsToClose = cEditorsOnThisPage.toArray(IEditorReference[]::new);
				info.editorInputs = editorInputs.toArray(IEditorInput[]::new);
				info.editorIds = editorIds.toArray(String[]::new);
				allCdtEditors.add(info);
			}
		}
		return allCdtEditors;
	}

	private void notifyUserWhatsHappening(Shell shell, boolean newExperience) {
		var notification = NotificationPopup.forShell(shell) //
				.title(Messages.SwitchToLsp_NewExperienceTitle, true) //
				.content((parent) -> createInfoWithLinks(parent, shell, newExperience, true));
		notification.open();
	}

	private boolean promptToOpenProjectProperties(Shell shell, IProject project) {
		final int indexOfDialogButtonToProceed = 0;
		String[] dialogButtonLabels = { //
				Messages.SwitchToLsp_OpenProjectSettings, //
				Messages.SwitchToLsp_Cancel //
		};
		MessageDialog dialog = new MessageDialog(shell, //
				Messages.SwitchToLsp_NewExperienceTitile, //
				null /* no custom title image */, //
				null /* dialog message is in the link below */, //
				MessageDialog.INFORMATION, indexOfDialogButtonToProceed, dialogButtonLabels) {
			@Override
			protected Control createMessageArea(Composite parent) {
				// use super to create image...
				Control result = super.createMessageArea(parent);
				// ... but use a custom message that allows links
				Link link = new Link(parent, SWT.NONE);
				String message = Messages.SwitchToLsp_ProjectSpecificSettingsLabel + LinkHelper.getLinks(false);
				link.setText(message);
				link.setFont(parent.getFont());
				GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(link);
				link.addListener(SWT.Selection, (event) -> LinkHelper.handleLinkClick(shell, event));
				return result;
			}

		};

		return dialog.open() == indexOfDialogButtonToProceed;
	}

	/**
	 * Open a confirmation message dialog.
	 *
	 * @param shell to parent the dialog on
	 * @param newExperience whether the info is for new experience, or back to traditional editor
	 * @return true to proceed
	 */
	private boolean openConfirmation(Shell shell, boolean newExperience) {
		final int indexOfDialogButtonToProceed = 0;
		String[] dialogButtonLabels = { //
				newExperience ? Messages.SwitchToLsp_UseNewExperience : Messages.SwitchToLsp_UseClassicExperience, //
				Messages.SwitchToLsp_Cancel //
		};
		MessageDialog dialog = new MessageDialog(shell, //
				Messages.SwitchToLsp_NewExperienceTitile, //
				null /* no custom title image */, //
				null /* dialog message is in the link below */, //
				MessageDialog.INFORMATION, indexOfDialogButtonToProceed, dialogButtonLabels) {
			@Override
			protected Control createMessageArea(Composite parent) {
				// use super to create image...
				Control result = super.createMessageArea(parent);
				// ... but use a custom message that allows links
				createInfoWithLinks(parent, parent.getShell(), newExperience, false);
				return result;
			}

		};

		return dialog.open() == indexOfDialogButtonToProceed;
	}

	/**
	 * Creates a control to show user key information about the switch to/from the new experience.
	 * Contains links to key features.
	 *
	 * @param composite the composite to place the control in
	 * @param parentShell the shell that any dialogs that are opened should use as the parent
	 * @param newExperience whether the info is for new experience, or back to traditional editor
	 * @param showPreferenceLink whether to include the preference link. In the middle of a switch
	 * it would be confusing to open the preferences.
	 * @return the created control
	 */
	private Control createInfoWithLinks(Composite parent, Shell parentShell, boolean newExperience,
			boolean showPreferenceLink) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(composite);
		Link link = new Link(composite, SWT.NONE);
		String message;
		if (newExperience) {
			message = Messages.SwitchToLsp_EditorsWitllReopenToNewExperience + LinkHelper.getLinks(showPreferenceLink);
		} else {
			message = Messages.SwitchToLsp_EditorsWitllReopenToClassicExperience
					+ LinkHelper.getLinks(showPreferenceLink);

		}

		link.setText(message);
		link.setFont(composite.getFont());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(link);
		link.addListener(SWT.Selection, (event) -> LinkHelper.handleLinkClick(parentShell, event));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(link);

		return link;
	}

}
