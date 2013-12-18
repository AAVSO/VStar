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
 * @deprecated
 */
package org.aavso.tools.vstar.ui.dialog.plugin.manager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

/**
 * A table model for a selectable (via checkbox) list of plugins.
 */
@SuppressWarnings("serial")
public class PluginTableModel extends AbstractTableModel {

	public final static int DESCRIPTION_COL = 0;
	public final static int OPERATION_COL = 1;

	private List<String> pluginDescriptions;

	/**
	 * Constructor
	 * 
	 * @param manager
	 *            The plugin manager.
	 */
	public PluginTableModel(PluginManager manager) {
		super();

		// Get a combined, unique list of plugin descriptions.
		Set<String> pluginDescSet = new LinkedHashSet<String>();
		pluginDescSet.addAll(manager.getLocalDescriptions());
		pluginDescSet.addAll(manager.getRemoteDescriptions());
		pluginDescriptions = new ArrayList<String>(pluginDescSet);
	}

	/**
	 * @return the pluginDescriptions
	 */
	public List<String> getPluginDescriptions() {
		return pluginDescriptions;
	}

	/**
	 * Remove a plugin description from the model.
	 * 
	 * @param description
	 *            The description to remove.
	 */
	public void removePluginDescription(String description) {
		pluginDescriptions.remove(description);
		fireTableDataChanged();
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return columnIndex == DESCRIPTION_COL ? String.class
				: PluginManagementOperation.class;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int columnIndex) {
		return columnIndex == DESCRIPTION_COL ? "Description" : "Operation";
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return columnIndex == DESCRIPTION_COL ? pluginDescriptions
				.get(rowIndex) : null;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == OPERATION_COL;
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return 2;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return pluginDescriptions.size();
	}
}
