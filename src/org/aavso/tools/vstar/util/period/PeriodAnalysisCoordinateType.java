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
package org.aavso.tools.vstar.util.period;

import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * Period analysis coordinate type.
 */
public enum PeriodAnalysisCoordinateType {

	FREQUENCY(LocaleProps.get("FREQUENCY_COORD")), AMPLITUDE(LocaleProps
			.get("AMPLITUDE_COORD")), POWER(LocaleProps.get("POWER_COORD")), PERIOD(
			LocaleProps.get("PERIOD_COORD"));

	private String description;

	/**
	 * Constructor
	 * 
	 * @param description
	 *            A string describing this coordinate type.
	 */
	private PeriodAnalysisCoordinateType(String description) {
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Given a description, return the corresponding coordinate type.
	 */
	public static PeriodAnalysisCoordinateType getTypeFromDescription(
			String desc) {
		PeriodAnalysisCoordinateType type = null;

		if ("Period".equals(desc)) {
			type = PERIOD;
		} else if ("Power".equals(desc)) {
			type = POWER;
		} else if ("Amplitude".equals(desc)) {
			type = AMPLITUDE;
		} else if ("Frequency".equals(desc)) {
			type = FREQUENCY;
		}

		return type;
	}

	public String toString() {
		return description;
	}
}
