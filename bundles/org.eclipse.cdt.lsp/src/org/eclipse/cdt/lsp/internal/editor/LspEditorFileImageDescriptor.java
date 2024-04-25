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

import java.net.URL;
import java.util.HashMap;

import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.cdt.ui.lsp.ICFileImageDescriptor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Bundle;

public class LspEditorFileImageDescriptor implements ICFileImageDescriptor {
	private final ICLanguageServerProvider cLanguageServerProvider;
	private static HashMap<String, ImageDescriptor> imageRegistry = new HashMap<>(2);
	private static final String ICONS_PATH = "$nl$/icons/"; //$NON-NLS-1$

	public static final String IMG_HFILE = "IMG_HFILE"; //$NON-NLS-1$
	public static final String IMG_CFILE = "IMG_CFILE"; //$NON-NLS-1$
	public static final String IMG_CXXFILE = "IMG_CXXFILE"; //$NON-NLS-1$

	public LspEditorFileImageDescriptor() {
		cLanguageServerProvider = LspPlugin.getDefault().getCLanguageServerProvider();
		declareRegistryImage(IMG_HFILE, ICONS_PATH + "h.png"); //$NON-NLS-1$
		declareRegistryImage(IMG_CFILE, ICONS_PATH + "c.png"); //$NON-NLS-1$
		declareRegistryImage(IMG_CXXFILE, ICONS_PATH + "cpp.png"); //$NON-NLS-1$
	}

	@Override
	public ImageDescriptor getCImageDescriptor() {
		return imageRegistry.get(IMG_CFILE);
	}

	@Override
	public ImageDescriptor getCXXImageDescriptor() {
		return imageRegistry.get(IMG_CXXFILE);
	}

	@Override
	public ImageDescriptor getHeaderImageDescriptor() {
		return imageRegistry.get(IMG_HFILE);
	}

	@Override
	public boolean isEnabled(IProject project) {
		return cLanguageServerProvider != null ? cLanguageServerProvider.isEnabledFor(project) : false;
	}

	private final void declareRegistryImage(String key, String path) {
		ImageDescriptor desc = ImageDescriptor.getMissingImageDescriptor();
		Bundle bundle = Platform.getBundle(LspPlugin.PLUGIN_ID);
		URL url = null;
		if (bundle != null) {
			url = FileLocator.find(bundle, new Path(path), null);
			if (url != null) {
				desc = ImageDescriptor.createFromURL(url);
			}
		}
		imageRegistry.put(key, desc);
	}
}
