/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.ui.NamedComponent;

/**
 * This class encapsulates the names and values of a collection of text areas.
 */
public class TextAreaTabs implements ITextComponent<String> {

	private boolean canBeEmpty;
	private boolean readOnly;

	private List<JTextArea> textAreas;

	private JTabbedPane tabs;

	/**
	 * Constructor.
	 * 
	 * @param names
	 *            The list of tab names.
	 * @param initialValues
	 *            The list of initial text area values.
	 * @param rows
	 *            The number of rows in these text areas; 0 means don't set;
	 *            applied to all text areas.
	 * @param cols
	 *            The number of rows in these text areas; 0 means don't set;
	 *            applied to all text areas.
	 * @param readOnly
	 *            Are all these areas read-only?
	 * @param canBeEmpty
	 *            Can any of these areas be empty? (all or none)
	 */
	public TextAreaTabs(List<String> names, List<String> initialValues,
			int rows, int cols, boolean readOnly, boolean canBeEmpty) {
		assert names.size() == initialValues.size();

		this.readOnly = readOnly;
		this.canBeEmpty = canBeEmpty;
		this.textAreas = new ArrayList<JTextArea>();

		List<NamedComponent> namedComponents = new ArrayList<NamedComponent>();

		for (int i = 0; i < names.size(); i++) {
			JTextArea textArea = new JTextArea(
					initialValues.get(i) == null ? "" : initialValues.get(i));

			textAreas.add(textArea);

			// textArea.setBorder(BorderFactory.createTitledBorder(names.get(i)));
			if (!isReadOnly()) {
				textArea.setToolTipText("Enter " + names.get(i));
			}

			namedComponents.add(new NamedComponent(names.get(i),
					new JScrollPane(textArea)));
		}

		for (JTextArea textArea : textAreas) {
			if (rows != 0) {
				textArea.setRows(rows);
			}

			if (cols != 0) {
				textArea.setColumns(cols);
			}
		}

		tabs = PluginComponentFactory.createTabs(namedComponents);
	}

	@Override
	public String getName() {
		return null;
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
		StringBuffer value = new StringBuffer();

		for (JTextArea textArea : textAreas) {
			value.append(textArea.getText());
			value.append("\n");
		}

		return value.toString();
	}

	@Override
	public String getStringValue() {
		StringBuffer buf = new StringBuffer();

		for (JTextArea textArea : textAreas) {
			buf.append(textArea.getText());
			buf.append("\n");
		}

		return buf.toString();
	}

	@Override
	public void setEditable(boolean state) {
		readOnly = !state;

	}

	@Override
	public void setValue(String value) {
		if ("".equals(value)) {
			for (JTextArea textArea : textAreas) {
				textArea.setText("");
			}
		} else {
			String[] values = value.split("\\<sentinel\\>");
			for (int i = 0; i < values.length; i++) {
				textAreas.get(i).setText(values[i]);
			}
		}
	}

	@Override
	public JComponent getUIComponent() {
		return tabs;
	}
}
