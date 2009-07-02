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

/**
 * This class represents a magValue including uncertainty 
 * and fainter-than information.
 */
public class Magnitude {

	public final static boolean IS_UNCERTAIN = true;
	public final static double ILLEGAL_UNCERTAINTY = -1;
	
	private double magValue;
	private MagnitudeModifier magModifier;
	private boolean isUncertain;
	private double uncertainty;

	/**
	 * Constructor.
	 * 
	 * @param magValue The magValue itself.
	 * @param brightnessModifier Does the magValue constitute a fainter/brighter-than observation?
	 * @param isUncertain Is this an uncertain magValue value?
	 */
	public Magnitude(double magnitude, MagnitudeModifier magModifier, boolean isUncertain) {
		this.magValue = magnitude;
		this.magModifier = magModifier;
		this.isUncertain = isUncertain;
		this.uncertainty = ILLEGAL_UNCERTAINTY; // this must be set after construction
	}
	
	/**
	 * @return the magValue
	 */
	public double getMagValue() {
		return magValue;
	}

	/**
	 * @return whether the magValue is fainter than the specified value
	 */
	public boolean isFainterThan() {
		return magModifier == MagnitudeModifier.FAINTER_THAN;
	}

	/**
	 * @return whether the magValue is brighter than the specified value
	 */
	public boolean isBrighterThan() {
		return magModifier == MagnitudeModifier.BRIGHTER_THAN;
	}

	/**
	 * @return whether this magValue is uncertain
	 */
	public boolean isUncertain() {
		return isUncertain;
	}

	/**
	 * @return the optional, quantitative uncertainty value of the magValue
	 */
	public double getUncertainty() {
		return uncertainty;
	}

	/**
	 * @param uncertainty The quantitative uncertainty value to set
	 */
	public void setUncertainty(double uncertainty) {
		this.uncertainty = uncertainty;
	}

	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		
		if (isFainterThan()) {
			strBuf.append("<");
		} else if (isBrighterThan()) {
			strBuf.append(">");
		}
		
		strBuf.append(magValue);

		if (isUncertain) {
			strBuf.append(" (uncertain)");
		}

		if (uncertainty != 0) {
			strBuf.append(" (\u00B1");
			strBuf.append(uncertainty);
			strBuf.append(")");
		}
		
		return strBuf.toString();
	}
}
