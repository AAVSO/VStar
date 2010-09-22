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

import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.util.locale.NumberParser;

/**
 * This class validates magnitude values (e.g. magnitude, uncertainty). 
 */
public class MagnitudeValueValidator extends AbstractStringValidator<Double> {

	public static final boolean CAN_BE_EMPTY = true;
	
	private boolean canBeEmpty;

	private static final String KIND = "magnitude";

	private final RangePredicate rangePredicate;

	/**
	 * Constructor.
	 * 
	 * @param rangePredicate
	 *            A numeric range predicate.
	 * @param canBeEmpty Can the magnitude field be empty?
	 */
	public MagnitudeValueValidator(RangePredicate rangePredicate, boolean canBeEmpty) {
		super(KIND);
		this.rangePredicate = rangePredicate;
		this.canBeEmpty = canBeEmpty;
	}

	/**
	 * Constructor.
	 * 
	 * @param rangePredicate
	 *            A numeric range predicate.
	 */
	public MagnitudeValueValidator(RangePredicate rangePredicate) {
		super(KIND);
		this.rangePredicate = rangePredicate;
		this.canBeEmpty = false;
	}

	public Double validate(String str) throws ObservationValidationError {
		if (this.isLegallyEmpty(str)) return null;

		double value = 0;
		
		try {
			value = NumberParser.parseDouble(str);
			if (!rangePredicate.holds(value)) {
				throw new ObservationValidationError("The " + kind + " '" + str
						+ "' falls outside of the range " + rangePredicate);
			}
		} catch (NumberFormatException e) {
			throw new ObservationValidationError("The " + kind + " '" + str
					+ "' is not a real number.");
		}
		
		return value;
	}	

	protected boolean canBeEmpty() {
		return this.canBeEmpty;
	}
}
