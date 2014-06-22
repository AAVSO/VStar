/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2012  AAVSO (http://www.aavso.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package org.aavso.tools.vstar.ui.dialog;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

/**
 * This class encapsulates the name and value of a text field along with a GUI
 * textComponent and methods to operate upon it.
 */
public class TextField implements ITextComponent<String> {

	public enum Kind {
		LINE, AREA;
	}

	private String name;
	private Kind kind;
	private boolean canBeEmpty;
	private boolean readOnly;

	private JTextComponent textComponent;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            The field's name.
	 * @param initialValue
	 *            The field's initial value.
	 * @param readOnly
	 *            Is this field read-only?
	 * @param canBeEmpty
	 *            Can this field be empty?
	 * @param kind
	 *            The kind of field: line or area.
	 */
	public TextField(String name, String initialValue, boolean readOnly,
			boolean canBeEmpty, Kind kind) {
		this.name = name;
		this.kind = kind;
		this.readOnly = readOnly;
		this.canBeEmpty = canBeEmpty;

		if (kind == Kind.LINE) {
			this.textComponent = new JTextField(initialValue == null ? ""
					: initialValue);
		} else if (kind == Kind.AREA) {
			this.textComponent = new JTextArea(initialValue == null ? ""
					: initialValue);
		}

		textComponent.setBorder(BorderFactory.createTitledBorder(name));
		if (!isReadOnly()) {
			textComponent.setToolTipText("Enter " + name);
		}
	}

	/**
	 * Construct a writable field with no initial value that cannot be empty.
	 * 
	 * @param name
	 *            The field's name.
	 * @param kind
	 *            The kind of field: line or area.
	 */
	public TextField(String name, Kind kind) {
		this(name, null, false, false, kind);
	}

	/**
	 * Construct a writable single-line field with no initial value that cannot
	 * be empty.
	 * 
	 * @param name
	 *            The field's name.
	 */
	public TextField(String name) {
		this(name, null, false, false, Kind.LINE);
	}

	/**
	 * Construct a read-only field with an initial value; it doesn't matter
	 * whether or not it can be empty, so we permit this in order to prevent
	 * field validation from failing, and e.g. stopping a dialog from closing.
	 * 
	 * @param name
	 *            The field's name.
	 * @param initialValue
	 *            The field's initial value.
	 * @param kind
	 *            The kind of field: line or area.
	 */
	public TextField(String name, String initialValue, Kind kind) {
		this(name, initialValue, true, true, kind);
	}

	/**
	 * Construct a read-only single-line field with an initial value; it doesn't
	 * matter whether or not it can be empty, so we permit this in order to
	 * prevent field validation from failing, and e.g. stopping a dialog from
	 * closing.
	 * 
	 * @param name
	 *            The field's name.
	 * @param initialValue
	 *            The field's initial value.
	 */
	public TextField(String name, String initialValue) {
		this(name, initialValue, true, true, Kind.LINE);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean canBeEmpty() {
		return canBeEmpty;
	}

	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public String getValue() {
		return textComponent.getText();
	}

	@Override
	public String getStringValue() {
		return getValue();
	}
	
	@Override
	public JComponent getUIComponent() {
		JComponent comp = textComponent;
		
		// Make text areas scrollable since we don't know how much content
		// one will have.
		if (kind == Kind.AREA) {
			comp = new JScrollPane(textComponent);
		}
		
		return comp;
	}

	@Override
	public void setEditable(boolean state) {
		textComponent.setEditable(state);
	}
}
