/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   See git history
 *******************************************************************************/

package org.eclipse.cdt.lsp.internal.server;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.eclipse.cdt.internal.core.LRUCache;
import org.eclipse.cdt.lsp.editor.EditorMetadata;
import org.eclipse.core.internal.content.ContentTypeManager;
import org.eclipse.core.runtime.content.IContentTypeManager.ContentTypeChangeEvent;
import org.eclipse.core.runtime.content.IContentTypeManager.IContentTypeChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.genericeditor.ExtensionBasedTextEditor;

/**
 * Caches the enable status for a given resource URI. Used by {@link HasLanguageServerPropertyTester#test(Object, String, Object[], Object)}
 * The cache is getting cleared: on changes in the C/C++ content types or the prefer LSP editor option has been changed (workspace or project level).
 * A resource URI shall be removed from the cache if it's getting closed in the editor.
 */
public final class URIEnableCache
		implements IPreferenceChangeListener, IContentTypeChangeListener, IPartListener, IWindowListener {
	private static final String C_SOURCE = "org.eclipse.cdt.core.cSource"; //$NON-NLS-1$
	private static final String CXX_SOURCE = "org.eclipse.cdt.core.cxxSource"; //$NON-NLS-1$
	private static final String C_HEADER = "org.eclipse.cdt.core.cHeader"; //$NON-NLS-1$
	private static final String CXX_HEADER = "org.eclipse.cdt.core.cxxHeader"; //$NON-NLS-1$
	private static final Map<URI, Boolean> cache = Collections.synchronizedMap(new LRUCache<>(100));
	private static URIEnableCache instance = null;

	private URIEnableCache() {
		ContentTypeManager.getInstance().addContentTypeChangeListener(this);
		if (PlatformUI.isWorkbenchRunning()) {
			var workbench = PlatformUI.getWorkbench();
			workbench.addWindowListener(this);
			Arrays.stream(workbench.getWorkbenchWindows()).map(IWorkbenchWindow::getPages).flatMap(Arrays::stream)
					.forEach(p -> p.addPartListener(this));
		}
	}

	public static void stop() {
		if (instance != null) {
			var workbench = PlatformUI.getWorkbench();
			workbench.removeWindowListener(instance);
			Arrays.stream(workbench.getWorkbenchWindows()).map(IWorkbenchWindow::getPages).flatMap(Arrays::stream)
					.forEach(p -> p.removePartListener(instance));
			cache.clear();
		}
	}

	public static synchronized URIEnableCache getInstance() {
		if (instance == null) {
			instance = new URIEnableCache();
		}
		return instance;
	}

	public Boolean get(URI uri) {
		return cache.get(uri);
	}

	public void put(URI uri, Boolean value) {
		cache.put(uri, value);
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (EditorMetadata.PREFER_LSP_KEY.contentEquals(event.getKey())) {
			cache.clear();
		}
	}

	@Override
	public void contentTypeChanged(ContentTypeChangeEvent event) {
		var id = event.getContentType().getId();
		if (C_SOURCE.contentEquals(id) || CXX_SOURCE.contentEquals(id) || C_HEADER.contentEquals(id)
				|| CXX_HEADER.contentEquals(id)) {
			cache.clear();
		}
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		// do nothing
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		// do nothing
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof ExtensionBasedTextEditor editor) {
			Optional.ofNullable(LSPEclipseUtils.toUri(editor.getEditorInput())).ifPresent(uri -> cache.remove(uri));
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
		// do nothing
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		// do nothing
	}

	@Override
	public void windowActivated(IWorkbenchWindow window) {
		// do nothing
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
		// do nothing
	}

	@Override
	public void windowClosed(IWorkbenchWindow window) {
		Arrays.stream(window.getPages()).forEach(p -> p.removePartListener(this));
	}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
		Arrays.stream(window.getPages()).forEach(p -> p.addPartListener(this));
	}
}
