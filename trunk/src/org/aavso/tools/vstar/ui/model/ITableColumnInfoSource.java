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
package org.aavso.tools.vstar.ui.model;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * This interface must be implemented by any class wanting 
 * to be a source of table model column information: column 
 * count, values, names.
 */
public interface ITableColumnInfoSource {

	/**
	 * Return the number of columns associated with this source.
	 * 
	 * @return The column count.
	 */
	abstract public int getColumnCount();
	
	/**
	 * Return the index of the "is-discrepant" column.
	 *  
	 * @return The index in question.
	 */
	abstract public int getDiscrepantColumnIndex();
	
	/**
	 * Given a column index return a title for a table column.
	 * 
	 * @param index A column index.
	 * @return The column name.
	 */
	abstract public String getTableColumnTitle(int index);

	/**
	 * Given a column index return the type for that table column.
	 * 
	 * @param index A column index.
	 * @return The column type.
	 */
	abstract public Class<?> getTableColumnClass(int index);
	
	/**
	 * Given a column index and a valid observation, return an object
	 * for a table column. 
	 * 
	 * @param index A column index.
	 * @param ob A valid observation.
	 * @return The column value.
	 */
	abstract public Object getTableColumnValue(int index, ValidObservation ob);
}
