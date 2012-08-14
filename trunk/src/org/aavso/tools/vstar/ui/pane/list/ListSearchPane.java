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
package org.aavso.tools.vstar.ui.pane.list;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.PatternSyntaxException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * A list search pane component, supporting regular expressions on all columns
 * or a single column.
 */
@SuppressWarnings("serial")
public class ListSearchPane<S extends TableModel> extends JPanel {

	private final static int ALL_COLUMNS = -1;

	private TableRowSorter<S> rowSorter;
	private RowFilter defaultRowFilter;

	private JButton searchButton;
	private JButton resetButton;
	private JCheckBox allColumnsCheckBox;
	private JComboBox columnSelector;
	private JTextField searchField;

	private int searchColumnIndex;

	public ListSearchPane(AbstractTableModel model, TableRowSorter<S> rowSorter) {
		super();

		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.setBorder(BorderFactory.createTitledBorder("Pattern Search"));

		this.rowSorter = rowSorter;
		defaultRowFilter = rowSorter.getRowFilter();

		searchColumnIndex = ALL_COLUMNS;

		String[] columnNames = new String[model.getColumnCount()];
		for (int i = 0; i < model.getColumnCount(); i++) {
			columnNames[i] = model.getColumnName(i);
		}
		columnSelector = new JComboBox(columnNames);
		columnSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchColumnIndex = columnSelector.getSelectedIndex();
			}
		});
		columnSelector.setEnabled(false);
		this.add(columnSelector);

		allColumnsCheckBox = new JCheckBox("All Columns?");
		allColumnsCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JCheckBox checkBox = (JCheckBox) e.getSource();
				if (checkBox.isSelected()) {
					columnSelector.setEnabled(false);
					searchColumnIndex = ALL_COLUMNS;
				} else {
					columnSelector.setEnabled(true);
					searchColumnIndex = columnSelector.getSelectedIndex();
				}
			}
		});
		allColumnsCheckBox.setSelected(true);
		this.add(allColumnsCheckBox);

		searchField = new JTextField();
		searchField.setToolTipText("Enter a regular expression...");
		this.add(searchField);

		searchButton = new JButton("Apply");
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = searchField.getText();
				try {
					if (searchColumnIndex == ALL_COLUMNS) {
						setRowFilter(RowFilter.regexFilter(text));
					} else {
						setRowFilter(RowFilter.regexFilter(text,
								searchColumnIndex));
					}
				} catch (PatternSyntaxException ex) {
					MessageBox.showErrorDialog(LocaleProps.get("APPLY_BUTTON"),
							"Invalid regular expression: '" + text + "'");
				}
			}
		});
		this.add(searchButton);

		resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchField.setText("");
				restoreDefaultRowFilter();
			}
		});
		this.add(resetButton);
	}

	/**
	 * @param defaultRowFilter
	 *            the defaultRowFilter to set
	 */
	public void setDefaultRowFilter(RowFilter defaultRowFilter) {
		this.defaultRowFilter = defaultRowFilter;
	}

	private void restoreDefaultRowFilter() {
		rowSorter.setRowFilter(defaultRowFilter);
	}

	/**
	 * Set the filter on the row sorter.
	 * 
	 * @param rowFilter
	 *            the rowFilter to set
	 */
	public void setRowFilter(RowFilter rowFilter) {
		rowSorter.setRowFilter(rowFilter);
	}

	public void disable() {
		searchField.setText("");
		searchField.setEnabled(false);
		searchButton.setEnabled(false);
		resetButton.setEnabled(false);
	}

	public void enable() {
		searchField.setEnabled(true);
		searchButton.setEnabled(true);
		resetButton.setEnabled(true);
	}
}
