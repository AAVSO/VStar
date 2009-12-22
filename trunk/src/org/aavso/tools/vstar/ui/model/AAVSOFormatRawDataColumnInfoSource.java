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
 * AAVSO format (file, database) table column information source.
 */
public class AAVSOFormatRawDataColumnInfoSource implements ITableColumnInfoSource {

	public static final AAVSOFormatRawDataColumnInfoSource fileInstance = new AAVSOFormatRawDataColumnInfoSource(true);
	public static final AAVSOFormatRawDataColumnInfoSource databaseInstance = new AAVSOFormatRawDataColumnInfoSource(false);

	// Table columns.
	private static final int JD_COLUMN = 0;
	private static final int CALENDAR_DATE_COLUMN = 1;
	private static final int MAGNITUDE_COLUMN = 2;
	private static final int HQ_UNCERTAINTY_COLUMN = 3;
	private static final int BAND_COLUMN = 4;
	private static final int OBSERVER_CODE_COLUMN = 5;
	private static final int COMMENT_CODE_COLUMN = 6;
	private static final int COMP_STAR_1_COLUMN = 7;
	private static final int COMP_STAR_2_COLUMN = 8;
	private static final int CHARTS_COLUMN = 9;
	private static final int COMMENTS_COLUMN = 10;
	private static final int TRANSFORMED_COLUMN = 11;
	private static final int AIRMASS_COLUMN = 12;
	private static final int VALFLAG_COLUMN = 13;
	private static final int CMAG_COLUMN = 14;
	private static final int KMAG_COLUMN = 15;
	private static final int HJD_COLUMN = 16;
	private static final int NAME_COLUMN = 17;
	private static final int MTYPE_COLUMN = 18;
	private static final int DISCREPANT_COLUMN = 19;
	private static final int LINE_NUM_COLUMN = 20;

	protected boolean useLineNumbers;

	/**
	 * Constructor.
	 * 
	 * @param useLineNumbers
	 *            Should line numbers be used?
	 */
	protected AAVSOFormatRawDataColumnInfoSource(boolean useLineNumbers) {
		this.useLineNumbers = useLineNumbers;
	}	

	public int getColumnCount() {
		return useLineNumbers ? LINE_NUM_COLUMN + 1 : LINE_NUM_COLUMN;
	}

	public int getDiscrepantColumnIndex() {
		return DISCREPANT_COLUMN;
	}

	public String getTableColumnTitle(int index) {
		String columnName = null;

		switch (index) {
		case JD_COLUMN:
			columnName = "Julian Day";
			break;
		case CALENDAR_DATE_COLUMN:
			columnName = "Calendar Date";
			break;
		case MAGNITUDE_COLUMN:
			columnName = "Magnitude";
			break;
		case HQ_UNCERTAINTY_COLUMN:
			columnName = "HQ Uncertainty";
			break;
		case BAND_COLUMN:
			columnName = "Band";
			break;
		case OBSERVER_CODE_COLUMN:
			columnName = "Observer Code";
			break;
		case COMMENT_CODE_COLUMN:
			columnName = "Comment";
			break;
		case COMP_STAR_1_COLUMN:
			columnName = "Comp Star 1";
			break;
		case COMP_STAR_2_COLUMN:
			columnName = "Comp Star 2";
			break;
		case CHARTS_COLUMN:
			columnName = "Charts";
			break;
		case COMMENTS_COLUMN:
			columnName = "Comments";
			break;
		case TRANSFORMED_COLUMN:
			columnName = "Transformed";
			break;
		case AIRMASS_COLUMN:
			columnName = "Airmass";
			break;
		case VALFLAG_COLUMN:
			columnName = "Valflag";
			break;
		case CMAG_COLUMN:
			columnName = "CMag";
			break;
		case KMAG_COLUMN:
			columnName = "KMag";
			break;
		case HJD_COLUMN:
			columnName = "HJD";
			break;
		case NAME_COLUMN:
			columnName = "Name";
			break;
		case MTYPE_COLUMN:
			columnName = "MType";
			break;
		case DISCREPANT_COLUMN:
			columnName = "Discrepant?";
			break;
		case LINE_NUM_COLUMN:
			columnName = "Line";
			break;
		}

		return columnName;
	}

	public Class<?> getTableColumnClass(int index) {
		Class<?> clazz = String.class;

		switch (index) {
		case JD_COLUMN:
			break;
		case CALENDAR_DATE_COLUMN:
			break;
		case MAGNITUDE_COLUMN:
			break;
		case HQ_UNCERTAINTY_COLUMN:
			break;
		case BAND_COLUMN:
			break;
		case OBSERVER_CODE_COLUMN:
			break;
		case COMMENT_CODE_COLUMN:
			break;
		case COMP_STAR_1_COLUMN:
			break;
		case COMP_STAR_2_COLUMN:
			break;
		case CHARTS_COLUMN:
			break;
		case COMMENTS_COLUMN:
			break;
		case TRANSFORMED_COLUMN:
			break;
		case AIRMASS_COLUMN:
			break;
		case VALFLAG_COLUMN:
			break;
		case CMAG_COLUMN:
			break;
		case KMAG_COLUMN:
			break;
		case HJD_COLUMN:
			break;
		case NAME_COLUMN:
			break;
		case MTYPE_COLUMN:
			break;
		case DISCREPANT_COLUMN:
			clazz = Boolean.class;
			break;
		case LINE_NUM_COLUMN:
			break;
		}

		return clazz;
	}

	public Object getTableColumnValue(int index, ValidObservation ob) {
		Object value = null;

		switch (index) {
		case JD_COLUMN:
			value = ob.getDateInfo().getJulianDay();
			break;
		case CALENDAR_DATE_COLUMN:
			value = ob.getDateInfo().getCalendarDate();
			break;
		case MAGNITUDE_COLUMN:
			value = ob.getMagnitude().toString();
			break;
		case HQ_UNCERTAINTY_COLUMN:
			value = "".equals(ob.getHqUncertainty()) ? "" : String.format(
					"%1.2f", ob.getHqUncertainty());
			break;
		case BAND_COLUMN:
			value = ob.getBand().getDescription();
			break;
		case OBSERVER_CODE_COLUMN:
			value = ob.getObsCode();
			break;
		case COMMENT_CODE_COLUMN:
			value = ob.getCommentCode().getOrigString();
			break;
		case COMP_STAR_1_COLUMN:
			value = ob.getCompStar1();
			break;
		case COMP_STAR_2_COLUMN:
			value = ob.getCompStar2();
			break;
		case CHARTS_COLUMN:
			value = ob.getCharts();
			break;
		case COMMENTS_COLUMN:
			value = ob.getComments();
			break;
		case TRANSFORMED_COLUMN:
			value = ob.isTransformed() ? "yes" : "no";
			break;
		case AIRMASS_COLUMN:
			value = ob.getAirmass();
			break;
		case VALFLAG_COLUMN:
			value = ob.getValidationType().getValflag();
			break;
		case CMAG_COLUMN:
			value = ob.getCMag();
			break;
		case KMAG_COLUMN:
			value = ob.getKMag();
			break;
		case HJD_COLUMN:
			value = ob.getHJD();
			break;
		case NAME_COLUMN:
			value = ob.getName();
			break;
		case MTYPE_COLUMN:
			value = ob.getMType().toString();
			break;
		case DISCREPANT_COLUMN:
			value = ob.isDiscrepant();
			break;
		case LINE_NUM_COLUMN:
			value = ob.getLineNumber();
			break;
		}

		return value;
	}
}
