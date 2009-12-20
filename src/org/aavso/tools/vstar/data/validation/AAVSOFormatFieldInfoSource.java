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
package org.aavso.tools.vstar.data.validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Field and column information source for AAVSO Download file format and
 * database sourced observations.
 */
public class AAVSOFormatFieldInfoSource implements IFieldInfoSource {

	// Singleton values for AAVSO download and database sources.
	public static final AAVSOFormatFieldInfoSource instance = new AAVSOFormatFieldInfoSource();

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

	public static final int FIELD_COUNT = MTYPE_FIELD + 1;

	private Map<String, Integer> fieldIndexMap;

	/**
	 * Constructor.
	 */
	private AAVSOFormatFieldInfoSource() {
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

	/**
	 * Return a mapping from field name to index in the text format associated
	 * with this source (for field validation).
	 */
	public Map<String, Integer> getFieldIndexMap() {
		return this.fieldIndexMap;
	}
}
