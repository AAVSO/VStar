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

import java.util.HashMap;
import java.util.Map;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * AAVSO format (file, database) raw data table column information source.
 */
public class AAVSOFormatRawDataColumnInfoSource implements
		ITableColumnInfoSource {

	// Table columns.
	private static final int JD_COLUMN = 0;
	private static final int CALENDAR_DATE_COLUMN = 1;
	private static final int MAGNITUDE_COLUMN = 2;
	private static final int UNCERTAINTY_COLUMN = 3;
	private static final int BAND_COLUMN = 4;
	private static final int OBSERVER_CODE_COLUMN = 5;
	private static final int VALFLAG_COLUMN = 6;
	private static final int COMP_STAR_1_COLUMN = 7;
	private static final int COMP_STAR_2_COLUMN = 8;
	private static final int CHARTS_COLUMN = 9;
	private static final int COMMENT_CODE_COLUMN = 10;
	private static final int COMMENTS_COLUMN = 11;
	private static final int TRANSFORMED_COLUMN = 12;
	private static final int AIRMASS_COLUMN = 13;
	private static final int CMAG_COLUMN = 14;
	private static final int KMAG_COLUMN = 15;
	private static final int HJD_COLUMN = 16;
	private static final int HQ_UNCERTAINTY_COLUMN = 17;
	private static final int MTYPE_COLUMN = 18;
	private static final int GROUP_COLUMN = 19;
	private static final int AFFILIATION_COLUMN = 20;
	private static final int ADS_REFERENCE_COLUMN = 21;
	private static final int DIGITIZER_COLUMN = 22;
	private static final int CREDIT_COLUMN = 23;
	private static final int DISCREPANT_COLUMN = 24;
	private static final int NAME_COLUMN = 25;
	private static final int LINE_NUM_COLUMN = 26;

	private static final String JD_COLUMN_NAME = "Julian Day";
	private static final String CALENDAR_DATE_COLUMN_NAME = "Calendar Date";
	private static final String MAGNITUDE_COLUMN_NAME = "Magnitude";
	private static final String UNCERTAINTY_COLUMN_NAME = "Uncertainty";
	private static final String BAND_COLUMN_NAME = "Band";
	private static final String OBSERVER_CODE_COLUMN_NAME = "Observer Code";
	private static final String VALFLAG_COLUMN_NAME = "Validation";
	private static final String COMP_STAR_1_COLUMN_NAME = "Comp Star 1";
	private static final String COMP_STAR_2_COLUMN_NAME = "Comp Star 2";
	private static final String CHARTS_COLUMN_NAME = "Charts";
	private static final String COMMENT_CODE_COLUMN_NAME = "Comment Type";
	private static final String COMMENTS_COLUMN_NAME = "Comments";
	private static final String TRANSFORMED_COLUMN_NAME = "Transformed";
	private static final String AIRMASS_COLUMN_NAME = "Airmass";
	private static final String CMAG_COLUMN_NAME = "CMag";
	private static final String KMAG_COLUMN_NAME = "KMag";
	private static final String HJD_COLUMN_NAME = "HJD";
	private static final String HQ_UNCERTAINTY_COLUMN_NAME = "HQ Uncertainty";
	private static final String MTYPE_COLUMN_NAME = "MType";
	private static final String GROUP_COLUMN_NAME = "Group";
	private static final String AFFILIATION_COLUMN_NAME = "Affiliation";
	private static final String ADS_REFERENCE_COLUMN_NAME = "ADS Reference";
	private static final String DIGITIZER_COLUMN_NAME = "Digitizer";
	private static final String CREDIT_COLUMN_NAME = "Credit";
	private static final String DISCREPANT_COLUMN_NAME = "Discrepant?";
	private static final String NAME_COLUMN_NAME = "Name";
	private static final String LINE_NUM_COLUMN_NAME = "Line";
	
	protected static final Map<String, Integer> COLUMN_NAMES = new HashMap<String, Integer>();

	static {
		COLUMN_NAMES.put(JD_COLUMN_NAME, JD_COLUMN);
		COLUMN_NAMES.put(CALENDAR_DATE_COLUMN_NAME, CALENDAR_DATE_COLUMN);
		COLUMN_NAMES.put(MAGNITUDE_COLUMN_NAME, MAGNITUDE_COLUMN);
		COLUMN_NAMES.put(UNCERTAINTY_COLUMN_NAME, UNCERTAINTY_COLUMN);
		COLUMN_NAMES.put(BAND_COLUMN_NAME, BAND_COLUMN);
		COLUMN_NAMES.put(OBSERVER_CODE_COLUMN_NAME, OBSERVER_CODE_COLUMN);
		COLUMN_NAMES.put(VALFLAG_COLUMN_NAME, VALFLAG_COLUMN);
		COLUMN_NAMES.put(COMP_STAR_1_COLUMN_NAME, COMP_STAR_1_COLUMN);
		COLUMN_NAMES.put(COMP_STAR_2_COLUMN_NAME, COMP_STAR_2_COLUMN);
		COLUMN_NAMES.put(CHARTS_COLUMN_NAME, CHARTS_COLUMN);
		COLUMN_NAMES.put(COMMENT_CODE_COLUMN_NAME, COMMENT_CODE_COLUMN);
		COLUMN_NAMES.put(COMMENTS_COLUMN_NAME, COMMENTS_COLUMN);
		COLUMN_NAMES.put(TRANSFORMED_COLUMN_NAME, TRANSFORMED_COLUMN);
		COLUMN_NAMES.put(AIRMASS_COLUMN_NAME, AIRMASS_COLUMN);
		COLUMN_NAMES.put(CMAG_COLUMN_NAME, CMAG_COLUMN);
		COLUMN_NAMES.put(KMAG_COLUMN_NAME, KMAG_COLUMN);
		COLUMN_NAMES.put(HJD_COLUMN_NAME, HJD_COLUMN);
		COLUMN_NAMES.put(HQ_UNCERTAINTY_COLUMN_NAME, HQ_UNCERTAINTY_COLUMN);
		COLUMN_NAMES.put(AFFILIATION_COLUMN_NAME, AFFILIATION_COLUMN);
		COLUMN_NAMES.put(MTYPE_COLUMN_NAME, MTYPE_COLUMN);
		COLUMN_NAMES.put(DISCREPANT_COLUMN_NAME, DISCREPANT_COLUMN);
		COLUMN_NAMES.put(NAME_COLUMN_NAME, NAME_COLUMN);
		COLUMN_NAMES.put(LINE_NUM_COLUMN_NAME, LINE_NUM_COLUMN);
	}

	protected boolean useLineNumbers;

	/**
	 * Constructor.
	 * 
	 * @param useLineNumbers
	 *            Should line numbers be used?
	 */
	public AAVSOFormatRawDataColumnInfoSource(boolean useLineNumbers) {
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

		// TODO: programmatically create a reverse map!
		
		switch (index) {
		case JD_COLUMN:
			columnName = JD_COLUMN_NAME;
			break;
		case CALENDAR_DATE_COLUMN:
			columnName = CALENDAR_DATE_COLUMN_NAME;
			break;
		case MAGNITUDE_COLUMN:
			columnName = MAGNITUDE_COLUMN_NAME;
			break;
		case UNCERTAINTY_COLUMN:
			columnName = UNCERTAINTY_COLUMN_NAME;
			break;
		case BAND_COLUMN:
			columnName = BAND_COLUMN_NAME;
			break;
		case OBSERVER_CODE_COLUMN:
			columnName = OBSERVER_CODE_COLUMN_NAME;
			break;
		case VALFLAG_COLUMN:
			columnName = VALFLAG_COLUMN_NAME;
			break;
		case COMP_STAR_1_COLUMN:
			columnName = COMP_STAR_1_COLUMN_NAME;
			break;
		case COMP_STAR_2_COLUMN:
			columnName = COMP_STAR_2_COLUMN_NAME;
			break;
		case CHARTS_COLUMN:
			columnName = CHARTS_COLUMN_NAME;
			break;
		case COMMENT_CODE_COLUMN:
			columnName = COMMENT_CODE_COLUMN_NAME;
			break;
		case COMMENTS_COLUMN:
			columnName = COMMENTS_COLUMN_NAME;
			break;
		case TRANSFORMED_COLUMN:
			columnName = TRANSFORMED_COLUMN_NAME;
			break;
		case AIRMASS_COLUMN:
			columnName = AIRMASS_COLUMN_NAME;
			break;
		case CMAG_COLUMN:
			columnName = CMAG_COLUMN_NAME;
			break;
		case KMAG_COLUMN:
			columnName = KMAG_COLUMN_NAME;
			break;
		case HJD_COLUMN:
			columnName = HJD_COLUMN_NAME;
			break;
		case HQ_UNCERTAINTY_COLUMN:
			columnName = HQ_UNCERTAINTY_COLUMN_NAME;
			break;
		case MTYPE_COLUMN:
			columnName = MTYPE_COLUMN_NAME;
			break;
		case AFFILIATION_COLUMN:
			columnName = AFFILIATION_COLUMN_NAME;
			break;
		case GROUP_COLUMN:
			columnName = GROUP_COLUMN_NAME;
			break;
		case ADS_REFERENCE_COLUMN:
			columnName = ADS_REFERENCE_COLUMN_NAME;
			break;
		case DIGITIZER_COLUMN:
			columnName = DIGITIZER_COLUMN_NAME;
			break;
		case CREDIT_COLUMN:
			columnName = CREDIT_COLUMN_NAME;
			break;
		case DISCREPANT_COLUMN:
			columnName = DISCREPANT_COLUMN_NAME;
			break;
		case NAME_COLUMN:
			columnName = NAME_COLUMN_NAME;
			break;
		case LINE_NUM_COLUMN:
			columnName = LINE_NUM_COLUMN_NAME;
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
		case UNCERTAINTY_COLUMN:
			break;
		case BAND_COLUMN:
			break;
		case OBSERVER_CODE_COLUMN:
			break;
		case VALFLAG_COLUMN:
			break;
		case COMP_STAR_1_COLUMN:
			break;
		case COMP_STAR_2_COLUMN:
			break;
		case CHARTS_COLUMN:
			break;
		case COMMENT_CODE_COLUMN:
			break;
		case COMMENTS_COLUMN:
			break;
		case TRANSFORMED_COLUMN:
			break;
		case AIRMASS_COLUMN:
			break;
		case CMAG_COLUMN:
			break;
		case KMAG_COLUMN:
			break;
		case HJD_COLUMN:
			break;
		case HQ_UNCERTAINTY_COLUMN:
			break;
		case MTYPE_COLUMN:
			break;
		case GROUP_COLUMN:
			break;
		case AFFILIATION_COLUMN:
			break;
		case ADS_REFERENCE_COLUMN:
			break;
		case DIGITIZER_COLUMN:
			break;
		case CREDIT_COLUMN:
			break;
		case DISCREPANT_COLUMN:
			clazz = Boolean.class;
			break;
		case NAME_COLUMN:
			break;
		case LINE_NUM_COLUMN:
			clazz = Integer.class;
			break;
		}

		return clazz;
	}

	public Object getTableColumnValue(int index, ValidObservation ob) {
		Object value = null;

		switch (index) {
		case JD_COLUMN:
			value = NumericPrecisionPrefs.formatTime(ob.getDateInfo()
					.getJulianDay());
			;
			break;
		case CALENDAR_DATE_COLUMN:
			value = ob.getDateInfo().getCalendarDate();
			break;
		case MAGNITUDE_COLUMN:
			value = NumericPrecisionPrefs.formatMag(ob.getMagnitude()
					.getMagValue());
			break;
		case UNCERTAINTY_COLUMN:
			value = NumericPrecisionPrefs.formatMag(ob.getMagnitude()
					.getUncertainty());
			break;
		case BAND_COLUMN:
			value = ob.getBand().getDescription();
			break;
		case OBSERVER_CODE_COLUMN:
			value = ob.getObsCode();
			break;
		case VALFLAG_COLUMN:
			value = ob.getValidationType() == null ? "" : ob
					.getValidationType().toString();
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
		case COMMENT_CODE_COLUMN:
			value = ob.getCommentCode() == null ? "" : ob.getCommentCode()
					.getOrigString();
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
		case CMAG_COLUMN:
			value = ob.getCMag();
			break;
		case KMAG_COLUMN:
			value = ob.getKMag();
			break;
		case HJD_COLUMN:
			value = ob.getHJD() == null ? "" : NumericPrecisionPrefs
					.formatTime(ob.getHJD().getJulianDay());
			break;
		case HQ_UNCERTAINTY_COLUMN:
			Double hqUncertainty = ob.getHqUncertainty();
			value = null == hqUncertainty ? "" : NumericPrecisionPrefs
					.formatMag(ob.getHqUncertainty());
			break;
		case MTYPE_COLUMN:
			value = ob.getMType() == null ? "" : ob.getMType().toString();
			break;
		case GROUP_COLUMN:
			value = ob.getGroup() == null ? "" : ob.getGroup();
			break;
		case AFFILIATION_COLUMN:
			value = ob.getAffiliation() == null ? "" : ob.getAffiliation();
			break;
		case ADS_REFERENCE_COLUMN:
			value = ob.getADSRef() == null ? "" : ob.getADSRef();
			break;
		case DIGITIZER_COLUMN:
			value = ob.getDigitizer() == null ? "" : ob.getDigitizer();
			break;
		case CREDIT_COLUMN:
			value = ob.getCredit() == null ? "" : ob.getCredit();
			break;
		case DISCREPANT_COLUMN:
			value = ob.isDiscrepant();
			break;
		case NAME_COLUMN:
			value = ob.getName();
			break;
		case LINE_NUM_COLUMN:
			value = ob.getRecordNumber();
			break;
		}
		return value;
	}

	@Override
	public int getColumnIndexByName(String name)
			throws IllegalArgumentException {
		if (name == null || !COLUMN_NAMES.containsKey(name)) {
			throw new IllegalArgumentException("No column name: " + name);
		} else {
			return COLUMN_NAMES.get(name);
		}
	}
}
