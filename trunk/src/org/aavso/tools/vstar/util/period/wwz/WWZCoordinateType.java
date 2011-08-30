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
 */
package org.aavso.tools.vstar.util.period.wwz;

/**
 * Period analysis coordinate type.
 */
public enum WWZCoordinateType {

	TAU(0), FREQUENCY(1), PERIOD(2), WWZ(3), SEMI_AMPLITUDE(4), MEAN_MAG(5), EFFECTIVE_NUM_DATA(
			6);

	private int id;

	private WWZCoordinateType(int id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Given an index, return the corresponding type.
	 * 
	 * @param id
	 *            The index.
	 * @return The corresponding type.
	 */
	public static WWZCoordinateType getTypeFromId(int id) {
		WWZCoordinateType type = null;
		switch (id) {
		case 0:
			type = TAU;
			break;
		case 1:
			type = FREQUENCY;
			break;
		case 2:
			type = PERIOD;
			break;
		case 3:
			type = WWZ;
			break;
		case 4:
			type = SEMI_AMPLITUDE;
			break;
		case 5:
			type = MEAN_MAG;
			break;
		case 6:
			type = EFFECTIVE_NUM_DATA;
			break;
		}

		assert type != null;

		return type;
	}

	@Override
	public String toString() {
		String str = null;

		switch (this) {
		case TAU:
			str = "Time";
			break;
		case FREQUENCY:
			str = "Frequency";
			break;
		case PERIOD:
			str = "Period";
			break;
		case WWZ:
			str = "WWZ";
			break;
		case SEMI_AMPLITUDE:
			str = "Semi-amplitude";
			break;
		case MEAN_MAG:
			str = "Mean Magnitude";
			break;
		case EFFECTIVE_NUM_DATA:
			str = "Effective number of data";
			break;
		}

		return str;
	}

	public String getDescription() {
		String str = null;

		switch (this) {
		case TAU:
			str = "Time being examined";
			break;
		case FREQUENCY:
			str = "Frequency being tested at time tau (cycles per time unit)";
			break;
		case PERIOD:
			str = "Period being tested at time tau (1/frequency)";
			break;
		case WWZ:
			str = "Weighted Wavelet Z statistic";
			break;
		case SEMI_AMPLITUDE:
			str = "Semi-amplitude of best-fit sinusoid (if signal periodic at frequency)";
			break;
		case MEAN_MAG:
			str = "Mean Magnitude at tau";
			break;
		case EFFECTIVE_NUM_DATA:
			str = "Effective number of data for test tau & frequency";
			break;
		}

		return str;
	}
}
