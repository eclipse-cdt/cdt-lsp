/*******************************************************************************
 * Copyright (c) 2023 ArSysOp.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.clangd.editor.configuration;

import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 *
 * Allows to reuse {@link FieldEditor} for {@link PropertyPage}
 *
 */
interface AdjustableFieldEditor {

	void adjust(int columns);

	final static class BooleanData extends BooleanFieldEditor implements AdjustableFieldEditor {

		BooleanData(PreferenceMetadata<Boolean> meta, Composite parent) {
			super(meta.identifer(), meta.name(), parent);
		}

		@Override
		public void adjust(int columns) {
			super.adjustForNumColumns(columns);
		}
	}

	final static class FileData extends FileFieldEditor implements AdjustableFieldEditor {

		FileData(PreferenceMetadata<String> meta, Composite parent) {
			super(meta.identifer(), meta.name(), parent);
		}

		@Override
		public void adjust(int columns) {
			super.adjustForNumColumns(columns);
		}
	}

	final static class StringData extends StringFieldEditor implements AdjustableFieldEditor {

		StringData(PreferenceMetadata<String> meta, Composite parent) {
			super(meta.identifer(), meta.name(), parent);
		}

		@Override
		public void adjust(int columns) {
			super.adjustForNumColumns(columns);
		}

	}

}
