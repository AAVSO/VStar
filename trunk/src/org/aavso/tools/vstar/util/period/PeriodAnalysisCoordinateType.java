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

import java.util.Set;
import java.util.TreeSet;

import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * Period analysis coordinate type.
 */
public class PeriodAnalysisCoordinateType implements
		Comparable<PeriodAnalysisCoordinateType> {

	// Common instances
	public final static PeriodAnalysisCoordinateType FREQUENCY = new PeriodAnalysisCoordinateType(
			LocaleProps.get("FREQUENCY_COORD"));
	public final static PeriodAnalysisCoordinateType PERIOD = new PeriodAnalysisCoordinateType(
			LocaleProps.get("PERIOD_COORD"));
	public final static PeriodAnalysisCoordinateType AMPLITUDE = new PeriodAnalysisCoordinateType(
			LocaleProps.get("AMPLITUDE_COORD"));
	public final static PeriodAnalysisCoordinateType SEMI_AMPLITUDE = new PeriodAnalysisCoordinateType(
			LocaleProps.get("SEMI_AMPLITUDE_COORD"));
	public final static PeriodAnalysisCoordinateType POWER = new PeriodAnalysisCoordinateType(
			LocaleProps.get("POWER_COORD"));

	private String description;

	private static Set<PeriodAnalysisCoordinateType> values;

	/**
	 * Constructor
	 * 
	 * @param description
	 *            A string describing this coordinate type.
	 */
	private PeriodAnalysisCoordinateType(String description) {
		this.description = description;
		updateStaticCollections(this);
	}

	public static PeriodAnalysisCoordinateType create(String description) {
		// Create the type of interest.
		PeriodAnalysisCoordinateType newCoordType = new PeriodAnalysisCoordinateType(
				description);

		// Find which ever one now exists in the values set. That may be the
		// new instance or a previously created instance.
		for (PeriodAnalysisCoordinateType type : values()) {
			// One type is equal to another if their descriptions are the
			// same. We can't have 2 types with the same name!
			if (newCoordType.equals(type)) {
				newCoordType = type;
				break;
			}
		}

		return newCoordType;
	}

	/**
	 * Delete the specified coordinate type.
	 * 
	 * @param type
	 *            The coordinate type to delete.
	 */
	public static void delete(PeriodAnalysisCoordinateType type) {
		if (values().contains(type)) {
			values.remove(type);
		}
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Return the value set, creating an empty set first if necessary. This
	 * method should be called rather than accessing the data member directly to
	 * ensure the collection exists. This method is intended for use within this
	 * class.
	 * 
	 * @return the values
	 */
	private static Set<PeriodAnalysisCoordinateType> values() {
		if (values == null) {
			values = new TreeSet<PeriodAnalysisCoordinateType>();
		}
		return values;
	}

	/**
	 * Given a description, return the corresponding coordinate type.
	 */
	public static PeriodAnalysisCoordinateType getTypeFromDescription(
			String desc) {
		PeriodAnalysisCoordinateType type = null;

		for (PeriodAnalysisCoordinateType value : values()) {
			if (value.getDescription().equals(desc)) {
				type = value;
				break;
			}
		}

		return type;
	}

	public String toString() {
		return description;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PeriodAnalysisCoordinateType)) {
			return false;
		}
		PeriodAnalysisCoordinateType other = (PeriodAnalysisCoordinateType) obj;
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(PeriodAnalysisCoordinateType other) {
		return description.compareTo(other.description);
	}

	// Helpers

	/**
	 * Adds a period analysis coordinate type instance to the values collection
	 * if it does not already exist.
	 * 
	 * @param type
	 *            The period analysis coordinate type to be added.
	 */
	private static void updateStaticCollections(
			PeriodAnalysisCoordinateType type) {
		if (!values().contains(type)) {
			values.add(type);
		}
	}
}
