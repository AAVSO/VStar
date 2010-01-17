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
package org.aavso.tools.vstar.ui.model.list;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * AAVSO format (file, database) table column information source for phase plot
 * mode.
 * 
 * This class exploits the knowledge that its base class's column indices are
 * offset by one compared to its. Yes, this breaks encapsulation but promotes
 * reuse and easier maintainability in a controlled manner.
 */
public class AAVSOFormatPhasePlotColumnInfoSource extends
		AAVSOFormatRawDataColumnInfoSource {

	public static final AAVSOFormatPhasePlotColumnInfoSource fileInstance = new AAVSOFormatPhasePlotColumnInfoSource(
			true);
	public static final AAVSOFormatPhasePlotColumnInfoSource databaseInstance = new AAVSOFormatPhasePlotColumnInfoSource(
			false);

	private static final int PHASE_COLUMN = 0;

	/**
	 * Constructor.
	 * 
	 * @param useLineNumbers
	 *            Should line numbers be used?
	 */
	private AAVSOFormatPhasePlotColumnInfoSource(boolean useLineNumbers) {
		super(useLineNumbers);
	}

	public int getColumnCount() {
		return super.getColumnCount() + 1;
	}

	public int getDiscrepantColumnIndex() {
		return super.getDiscrepantColumnIndex() + 1;
	}

	public String getTableColumnTitle(int index) {
		String columnName = null;

		switch (index) {
		case PHASE_COLUMN:
			columnName = "Phase";
			break;
		default:
			columnName = super.getTableColumnTitle(index - 1);
			break;
		}

		return columnName;
	}

	public Class<?> getTableColumnClass(int index) {
		Class<?> clazz = null;

		switch (index) {
		case PHASE_COLUMN:
			clazz = String.class;
			break;
		default:
			clazz = super.getTableColumnClass(index - 1);
			break;
		}

		return clazz;
	}

	public Object getTableColumnValue(int index, ValidObservation ob) {
		Object value = null;

		switch (index) {
		case PHASE_COLUMN:
			value = String.format("%1.3f", ob.getStandardPhase());
			break;
		default:
			value = super.getTableColumnValue(index - 1, ob);
			break;
		}

		return value;
	}
}
