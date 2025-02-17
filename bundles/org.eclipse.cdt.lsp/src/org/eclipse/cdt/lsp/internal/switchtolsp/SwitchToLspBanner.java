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

import org.eclipse.cdt.lsp.editor.EditorConfiguration;
import org.eclipse.cdt.lsp.editor.EditorMetadata;
import org.eclipse.cdt.lsp.editor.EditorOptions;
import org.eclipse.cdt.lsp.internal.messages.LspUiMessages;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.ServiceCaller;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.notifications.NotificationPopup;
import org.eclipse.jface.notifications.internal.CommonImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public abstract class SwitchToLspBanner {

	/**
	 * The composite containing the "try CDT LSP" controls.
	 */
	private Composite tryLspComposite;
	private GridData tryLspCompositeLayoutData;
	private ITextEditor editor;

	public SwitchToLspBanner(ITextEditor part) {
		this.editor = part;
	}

	private void createContent() {
		var parent = tryLspComposite;
		var composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).create());
		composite.setLayout(GridLayoutFactory.fillDefaults().margins(4, 4).spacing(10, 10).numColumns(3).create());
		composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
		composite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		var fill = new Label(composite, SWT.NONE);
		fill.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		var tryLspButton = new Link(composite, SWT.NONE);
		tryLspButton.setText(tryLspBannerLink());
		tryLspButton.addListener(SWT.Selection, (e) -> {
			tryLspAction(e);
		});

		createCloseButton(composite);

	}

	protected abstract void tryLspAction(Event event);

	protected abstract String tryLspBannerLink();

	protected ITextEditor getPart() {
		return editor;
	}

	void createCloseButton(Composite parent) {
		// reuse these images - but just in case this internal API changes, surround in a safe runner
		@SuppressWarnings("restriction")
		Image close = SafeRunner.run(() -> CommonImages.getImage(CommonImages.NOTIFICATION_CLOSE));
		@SuppressWarnings("restriction")
		Image closeHover = SafeRunner.run(() -> CommonImages.getImage(CommonImages.NOTIFICATION_CLOSE_HOVER));

		final Label button = new Label(parent, SWT.NONE);
		button.setImage(close);
		button.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseEnter(MouseEvent e) {
				button.setImage(closeHover);
			}

			@Override
			public void mouseExit(MouseEvent e) {
				button.setImage(close);
			}
		});
		button.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				close();
			}

		});
	}

	private void close() {
		hideTryLsp();

		Shell shell = editor.getSite().getShell();
		var notification = NotificationPopup.forShell(shell) //
				.title(Messages.SwitchToLsp_NewExperienceTitile, true) //
				.content((parent) -> createInfoWithLinks(parent, shell));
		notification.open();
	}

	private Control createInfoWithLinks(Composite parent, Shell parentShell) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(composite);
		var intro = new Label(composite, SWT.NONE);
		intro.setText(Messages.SwitchToLsp_LearnMoreMessage);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(intro);

		var showButton = new Button(composite, SWT.CHECK);
		showButton.setText(LspUiMessages.LspEditorConfigurationPage_showTryLspBanner);
		showButton.setToolTipText(LspUiMessages.LspEditorConfigurationPage_showTryLspBanner_description);
		showButton.setSelection(true);
		showButton.addListener(SWT.Selection, (event) -> {
			IWorkspace workspace = PlatformUI.getWorkbench().getService(IWorkspace.class);
			ServiceCaller.callOnce(SwitchToLspWizard.class, EditorConfiguration.class, //
					cc -> cc.storage(workspace).save(showButton.getSelection(),
							EditorMetadata.Predefined.showTryLspBanner));
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(showButton);

		Link link = new Link(composite, SWT.NONE);
		link.setText(LinkHelper.getLinks(true));
		link.setFont(composite.getFont());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(link);
		link.addListener(SWT.Selection, (event) -> LinkHelper.handleLinkClick(parentShell, event));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(link);

		return composite;
	}

	private boolean isBannerEnabled() {
		boolean showBanner = SwitchToLspWizard.showPreferLsp(editor);
		if (!showBanner) {
			return false;
		}

		return getOptionShowBanner();
	}

	private boolean getOptionShowBanner() {
		EditorConfiguration configuration = PlatformUI.getWorkbench().getService(EditorConfiguration.class);
		if (configuration == null) {
			return false;
		}

		EditorOptions options = null;
		IWorkspace workspace = PlatformUI.getWorkbench().getService(IWorkspace.class);
		options = configuration.options(workspace);

		if (options == null) {
			return false;
		}
		return options.showTryLspBanner();
	}

	/**
	 * Makes the try lsp visible. Creates its content
	 * if this is the first time it is made visible.
	 */
	private void showBanner() {
		if (tryLspComposite == null || tryLspComposite.isDisposed()) {
			// not expected, but we can't show the composite
			// See also https://bugs.eclipse.org/446203 and
			// similar comment in org.eclipse.jdt.internal.ui.javaeditor.JavaEditor.showBreadcrumb()
			// Unlike JDT, we just silently don't show the TryLsp in this case
			return;
		}
		if (tryLspComposite.getChildren().length == 0) {
			createContent();
		}

		((GridData) tryLspComposite.getLayoutData()).exclude = false;
		tryLspComposite.setVisible(true);
		tryLspComposite.getParent().layout(true, true);
	}

	/**
	 * Hides the try lsp
	 */
	private void hideTryLsp() {
		if (tryLspComposite == null)
			return;
		tryLspCompositeLayoutData.exclude = true;
		tryLspComposite.setVisible(false);
		tryLspComposite.getParent().layout(true, true);
	}

	public Composite create(Composite parent) {
		if (!isBannerEnabled()) {
			return parent;
		}

		Composite topComposite = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout(1, false);
		topLayout.marginHeight = 0;
		topLayout.marginWidth = 0;
		topLayout.horizontalSpacing = 0;
		topLayout.verticalSpacing = 0;
		topComposite.setLayout(topLayout);

		tryLspComposite = new Composite(topComposite, SWT.NONE);
		tryLspCompositeLayoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
		tryLspComposite.setLayoutData(tryLspCompositeLayoutData);
		var tryLspLayout = new GridLayout(1, false);
		tryLspLayout.marginHeight = 0;
		tryLspLayout.marginWidth = 0;
		tryLspLayout.horizontalSpacing = 0;
		tryLspLayout.verticalSpacing = 0;
		tryLspCompositeLayoutData.exclude = true;
		tryLspComposite.setLayout(tryLspLayout);

		showBanner();

		Composite editorComposite = new Composite(topComposite, SWT.NONE);
		editorComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		FillLayout editorLayout = new FillLayout(SWT.VERTICAL);
		editorLayout.marginHeight = 0;
		editorLayout.marginWidth = 0;
		editorLayout.spacing = 0;
		editorComposite.setLayout(editorLayout);
		return editorComposite;
	}

}
