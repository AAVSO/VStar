/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
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

import java.util.Locale;

import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import junit.framework.TestCase;

/**
 * Pure unit tests for {@link ListSearchPane}.
 *
 * Uses a plain {@link DefaultTableModel} and {@link TableRowSorter} to drive
 * the pane without any production model dependency. No display is required
 * since {@code ListSearchPane} is a {@code JPanel} that only accesses the
 * row sorter and model data structures.
 *
 * Part of issue #579 (prong C): GUI code coverage.
 */
public class ListSearchPaneTest extends TestCase {

	private DefaultTableModel tableModel;
	private TableRowSorter<DefaultTableModel> rowSorter;
	private ListSearchPane<DefaultTableModel> pane;

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);

		tableModel = new DefaultTableModel(
				new Object[][] {
						{ "Alpha", "1.0" },
						{ "Beta",  "2.0" },
						{ "Gamma", "3.0" }
				},
				new String[] { "Name", "Value" });

		rowSorter = new TableRowSorter<DefaultTableModel>(tableModel);
		pane = new ListSearchPane<DefaultTableModel>(tableModel, rowSorter);
	}

	public void testConstructionDoesNotThrow() {
		assertNotNull(pane);
	}

	public void testPaneIsNotNull() {
		assertNotNull(pane);
	}

	public void testDisableDoesNotThrow() {
		pane.disable();
	}

	public void testEnableDoesNotThrow() {
		pane.enable();
	}

	public void testDisableThenEnableDoesNotThrow() {
		pane.disable();
		pane.enable();
	}

	public void testSetRowFilterNullDoesNotThrow() {
		pane.setRowFilter(null);
	}

	@SuppressWarnings("unchecked")
	public void testSetRowFilterAppliesFilter() {
		RowFilter<DefaultTableModel, Object> filter =
				RowFilter.regexFilter("Alpha");
		pane.setRowFilter(filter);
		assertEquals(1, rowSorter.getViewRowCount());
	}

	public void testSetDefaultRowFilterDoesNotThrow() {
		RowFilter<DefaultTableModel, Object> customDefault =
				RowFilter.regexFilter(".*");
		pane.setDefaultRowFilter(customDefault);
	}

	public void testConstructionWithEmptyModel() {
		DefaultTableModel emptyModel = new DefaultTableModel(
				new Object[][] {},
				new String[] { "Col1", "Col2" });
		TableRowSorter<DefaultTableModel> sorter =
				new TableRowSorter<DefaultTableModel>(emptyModel);
		ListSearchPane<DefaultTableModel> emptyPane =
				new ListSearchPane<DefaultTableModel>(emptyModel, sorter);
		assertNotNull(emptyPane);
	}

	public void testConstructionWithSingleColumnModel() {
		DefaultTableModel singleCol = new DefaultTableModel(
				new Object[][] { { "row1" }, { "row2" } },
				new String[] { "Only" });
		TableRowSorter<DefaultTableModel> sorter =
				new TableRowSorter<DefaultTableModel>(singleCol);
		ListSearchPane<DefaultTableModel> singlePane =
				new ListSearchPane<DefaultTableModel>(singleCol, sorter);
		assertNotNull(singlePane);
	}
}
