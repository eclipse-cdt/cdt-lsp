package org.eclipse.cdt.lsp.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public abstract class ConfigurationArea {
	protected final Map<PreferenceMetadata<Boolean>, Button> buttons;
	protected final List<Consumer<TypedEvent>> listeners;
	protected final int columns;

	public ConfigurationArea(int columns) {
		this.buttons = new HashMap<>();
		this.listeners = new ArrayList<>();
		this.columns = columns;
	}

	protected Group createGroup(Composite parent, String label, int numColumns) {
		Group group = new Group(parent, SWT.NONE);
		group.setFont(parent.getFont());
		group.setText(label);
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return group;
	}

	protected Button createButton(PreferenceMetadata<Boolean> meta, Composite composite, int style,
			int horizontalIndent) {
		Button button = new Button(composite, style);
		button.setLayoutData(GridDataFactory.fillDefaults().span(columns, 1).indent(horizontalIndent, 0).create());
		button.setData(meta);
		button.setText(meta.name());
		button.setToolTipText(meta.description());
		buttons.put(meta, button);
		return button;
	}

	public void addChangeListener(Consumer<TypedEvent> listener) {
		listeners.add(listener);
	}

	public void removeChangeListener(Consumer<TypedEvent> listener) {
		listeners.add(listener);
	}

	public void changed(TypedEvent event) {
		listeners.forEach(c -> c.accept(event));
	}

	public void dispose() {
		listeners.clear();
		buttons.clear();
	}

	public abstract void load(Object options, boolean enable);

	public abstract void store(IEclipsePreferences prefs);

}
