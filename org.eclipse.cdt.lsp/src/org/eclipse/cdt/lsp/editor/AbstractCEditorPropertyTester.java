/*******************************************************************************
 * Copyright (c) 2023 Bachmann electronic GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.lsp.editor;

import org.eclipse.core.expressions.PropertyTester;

/**
 * This class can be used in the serverProvider extension point to add a property test to the language server enabler.
 * The test method can be used to distinguish whether a file shall be opened by a LSP based editor or not.
 * This can be necessary when there is more than one editor for a content type.
 */
public abstract class AbstractCEditorPropertyTester extends PropertyTester implements ICEditorTest {

}
