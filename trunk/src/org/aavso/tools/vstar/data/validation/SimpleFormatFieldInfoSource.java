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
 * Simple file format field information source.
 */
public class SimpleFormatFieldInfoSource implements IFieldInfoSource {

	public final static SimpleFormatFieldInfoSource simpleFormatFieldInfoSource = new SimpleFormatFieldInfoSource();

	// Text format fields.
	private static final int JD_FIELD = 0;
	private static final int MAGNITUDE_FIELD = 1;
	private static final int UNCERTAINTY_FIELD = 2;
	private static final int OBSERVER_CODE_FIELD = 3;
	private static final int VALFLAG_FIELD = 4;

	public static final int FIELD_COUNT = VALFLAG_FIELD + 1;

	private Map<String, Integer> fieldIndexMap;

	/**
	 * Constructor.
	 */
	public SimpleFormatFieldInfoSource() {
		this.fieldIndexMap = new HashMap<String, Integer>();
		this.fieldIndexMap.put("JD_FIELD", JD_FIELD);
		this.fieldIndexMap.put("MAGNITUDE_FIELD", MAGNITUDE_FIELD);
		this.fieldIndexMap.put("UNCERTAINTY_FIELD", UNCERTAINTY_FIELD);
		this.fieldIndexMap.put("OBSERVER_CODE_FIELD", OBSERVER_CODE_FIELD);
		this.fieldIndexMap.put("VALFLAG_FIELD", VALFLAG_FIELD);
	}

	/**
	 * Return a mapping from field name to index in the text format associated
	 * with this source (for field validation).
	 */
	public Map<String, Integer> getFieldIndexMap() {
		return this.fieldIndexMap;
	}
}
