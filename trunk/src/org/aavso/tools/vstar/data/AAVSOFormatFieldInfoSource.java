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
package org.aavso.tools.vstar.data;

import java.util.HashMap;
import java.util.Map;

import org.aavso.tools.vstar.data.validation.ITableFieldInfoSource;
import org.aavso.tools.vstar.ui.model.ITableColumnInfoSource;

/**
 * Field and column information source for AAVSO Download file format 
 * and database read observations.
 */
public class AAVSOFormatFieldInfoSource implements ITableColumnInfoSource,
		ITableFieldInfoSource {

	public static final AAVSOFormatFieldInfoSource aavsoFormatFieldInfoSource = new AAVSOFormatFieldInfoSource();

	// Text format fields.
	private static final int JD_FIELD = 0;
	private static final int MAGNITUDE_FIELD = 1;
	private static final int UNCERTAINTY_FIELD = 2;
	private static final int HQ_UNCERTAINTY_FIELD = 3;
	private static final int BAND_FIELD = 4;
	private static final int OBSERVER_CODE_FIELD = 5;
	private static final int COMMENT_CODE_FIELD = 6;
	private static final int COMP_STAR_1_FIELD = 7;
	private static final int COMP_STAR_2_FIELD = 8;
	private static final int CHARTS_FIELD = 9;
	private static final int COMMENTS_FIELD = 10;
	private static final int TRANSFORMED_FIELD = 11;
	private static final int AIRMASS_FIELD = 12;
	private static final int VALFLAG_FIELD = 13;
	private static final int CMAG_FIELD = 14;
	private static final int KMAG_FIELD = 15;
	private static final int HJD_FIELD = 16;
	private static final int NAME_FIELD = 17;
	private static final int MTYPE_FIELD = 18;
	
	public static final int FIELD_COUNT = MTYPE_FIELD+1;

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
	private static final int LINE_NUM_COLUMN = 19;
	private static final int DISCREPANT_COLUMN = 20;

	private static final int COLUMNS = DISCREPANT_COLUMN + 1;

	private Map<String, Integer> fieldIndexMap;

	/**
	 * Constructor.
	 */
	public AAVSOFormatFieldInfoSource() {
		this.fieldIndexMap = new HashMap<String, Integer>();
		this.fieldIndexMap.put("JD_FIELD", JD_FIELD);
		this.fieldIndexMap.put("MAGNITUDE_FIELD", MAGNITUDE_FIELD);
		this.fieldIndexMap.put("UNCERTAINTY_FIELD", UNCERTAINTY_FIELD);
		this.fieldIndexMap.put("HQ_UNCERTAINTY_FIELD", HQ_UNCERTAINTY_FIELD);
		this.fieldIndexMap.put("BAND_FIELD", BAND_FIELD);
		this.fieldIndexMap.put("OBSERVER_CODE_FIELD", OBSERVER_CODE_FIELD);
		this.fieldIndexMap.put("COMMENT_CODE_FIELD", COMMENT_CODE_FIELD);
		this.fieldIndexMap.put("COMP_STAR_1_FIELD", COMP_STAR_1_FIELD);
		this.fieldIndexMap.put("COMP_STAR_2_FIELD", COMP_STAR_2_FIELD);
		this.fieldIndexMap.put("CHARTS_FIELD", CHARTS_FIELD);
		this.fieldIndexMap.put("COMMENTS_FIELD", COMMENTS_FIELD);
		this.fieldIndexMap.put("TRANSFORMED_FIELD", TRANSFORMED_FIELD);
		this.fieldIndexMap.put("AIRMASS_FIELD", AIRMASS_FIELD);
		this.fieldIndexMap.put("VALFLAG_FIELD", VALFLAG_FIELD);
		this.fieldIndexMap.put("CMAG_FIELD", CMAG_FIELD);
		this.fieldIndexMap.put("KMAG_FIELD", KMAG_FIELD);
		this.fieldIndexMap.put("HJD_FIELD", HJD_FIELD);
		this.fieldIndexMap.put("NAME_FIELD", NAME_FIELD);
		this.fieldIndexMap.put("MTYPE_FIELD", MTYPE_FIELD);
	}

	public int getColumnCount() {
		return COLUMNS;
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
		case LINE_NUM_COLUMN:
			columnName = "Line";
			break;
		case DISCREPANT_COLUMN:
			columnName = "Discrepant?";
			break;
		}

		return columnName;
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
			value = ob.getHqUncertainty();
			break;
		case BAND_COLUMN:
			value = ob.getBand();
			break;
		case OBSERVER_CODE_COLUMN:
			value = ob.getObsCode();
			break;
		case COMMENT_CODE_COLUMN:
			value = ob.getCommentCode();
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
			value = ob.isTransformed();
			break;
		case AIRMASS_COLUMN:
			value = ob.getAirmass();
			break;
		case VALFLAG_COLUMN:
			value = ob.getValidationType().getValflag(); // TODO: or print enum value?
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
			value = ob.getMType();
			break;
		case LINE_NUM_COLUMN:
			value = ob.getLineNumber();
			break;
		case DISCREPANT_COLUMN:
			value = ob.isDiscrepant();
			break;
		}

		return value;
	}

	public Map<String, Integer> getFieldIndexMap() {
		return this.fieldIndexMap;
	}
}
