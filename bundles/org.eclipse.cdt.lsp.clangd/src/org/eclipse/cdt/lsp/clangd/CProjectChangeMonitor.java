/*******************************************************************************
 * Copyright (c) 2024 Bachmann electronic GmbH and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.clangd;

/**
 * This monitor gets started when this plugin is loaded and stopped on a plugin stop respectively.
 */
public interface CProjectChangeMonitor {

	/**
	 * Listeners can be added in this method. It gets called on plugin start.
	 * @return CProjectChangeMonitor
	 */
	CProjectChangeMonitor start();

	/**
	 * Listeners can be removed in this method. It gets called on plugin stop.
	 */
	void stop();

}