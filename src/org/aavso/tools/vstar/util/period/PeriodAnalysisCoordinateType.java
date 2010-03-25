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

/**
 * Period analysis coordinate type.
 * 
 * TODO: using strings rather than these enums and distinguishing
 * frequency (or whatever the x coordinate is) from y coordinates
 * would make the plugin architecture more flexible.
 */
public enum PeriodAnalysisCoordinateType {

	FREQUENCY("Frequency"), PERIOD("Period",
			PeriodAnalysisCoordinateType.PERIOD_INDEX), POWER("Power",
			PeriodAnalysisCoordinateType.POWER_INDEX), AMPLITUDE("Amplitude",
			PeriodAnalysisCoordinateType.AMPLITUDE_INDEX);

	private final static int POWER_INDEX = 0;
	private final static int PERIOD_INDEX = 1;
	private final static int AMPLITUDE_INDEX = 2;
	private final static int FREQUENCY_INDEX = 3;

	private String description;
	private int index;

	/**
	 * Constructor
	 * 
	 * @param description
	 *            A string describing this coordinate type.
	 * @param index
	 *            An arbitrary unique index number for this coordinate type.
	 */
	private PeriodAnalysisCoordinateType(String description, int index) {
		this.description = description;
		this.index = index;
	}

	/**
	 * Constructor
	 * 
	 * @param description
	 *            A string describing this coordinate type.
	 */
	private PeriodAnalysisCoordinateType(String description) {
		this(description, FREQUENCY_INDEX);
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Given an index, return the corresponding coordinate type.
	 */
	public static PeriodAnalysisCoordinateType getTypeFromIndex(int index) {
		PeriodAnalysisCoordinateType type = null;

		switch (index) {
		case PERIOD_INDEX:
			type = PERIOD;
			break;
		case POWER_INDEX:
			type = POWER;
			break;
		case AMPLITUDE_INDEX:
			type = AMPLITUDE;
			break;
		case FREQUENCY_INDEX:
			type = FREQUENCY;
			break;
		}

		assert type != null;

		return type;
	}

	public String toString() {
		return description;
	}
}
