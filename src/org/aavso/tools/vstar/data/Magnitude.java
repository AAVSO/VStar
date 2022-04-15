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

import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This class represents a magValue including uncertainty and fainter-than
 * information.
 */
public class Magnitude {

	public final static boolean IS_UNCERTAIN = true;

	private double magValue;
	private MagnitudeModifier magModifier;
	private boolean isUncertain;
	private double uncertainty;

	/**
	 * Parameterless constructor for web service.
	 */
	public Magnitude() {
	}

	/**
	 * Constructor.
	 * 
	 * @param magnitude          The magnitude value itself.
	 * @param brightnessModifier Does the magValue constitute a
	 *                           fainter/brighter-than observation?
	 * @param isUncertain        Is this an uncertain magValue value?
	 * @param uncertainty        The uncertainty of the magnitude.
	 */
	public Magnitude(double magnitude, MagnitudeModifier magModifier, boolean isUncertain, double uncertainty) {
		this.magValue = magnitude;
		this.magModifier = magModifier;
		this.isUncertain = isUncertain;
		this.uncertainty = uncertainty;
	}

	/**
	 * Constructor.
	 * 
	 * Uncertainty value defaults to zero; may be changed after construction.
	 * 
	 * @param magnitude          The magnitude value itself.
	 * @param brightnessModifier Does the magValue constitute a
	 *                           fainter/brighter-than observation?
	 * @param isUncertain        Is this an uncertain magValue value?
	 */
	public Magnitude(double magnitude, MagnitudeModifier magModifier, boolean isUncertain) {
		this(magnitude, magModifier, isUncertain, 0);
	}

	/**
	 * Constructor
	 * 
	 * Creates a Magnitude with magnitude and uncertainty values.
	 * 
	 * @param magnitude   The magnitude value itself.
	 * @param uncertainty The uncertainty of the magnitude.
	 */
	public Magnitude(double magnitude, double uncertainty) {
		this(magnitude, MagnitudeModifier.NO_DELTA, !IS_UNCERTAIN, uncertainty);
	}

	/**
	 * Creates and returns a copy of this magnitude.
	 * @return the copied magnitude
	 */
	public Magnitude copy() {
		return new Magnitude(this.magValue, this.magModifier,
							 this.isUncertain, this.uncertainty);
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
	 * @return the magModifier
	 */
	public MagnitudeModifier getMagModifier() {
		return magModifier;
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

	/**
	 * @param magModifier the magModifier to set
	 */
	public void setMagModifier(MagnitudeModifier magModifier) {
		this.magModifier = magModifier;
	}

	/**
	 * @param magValue the magValue to set
	 */
	public void setMagValue(double magValue) {
		this.magValue = magValue;
	}

	/**
	 * @param isUncertain the isUncertain to set
	 */
	public void setUncertain(boolean isUncertain) {
		this.isUncertain = isUncertain;
	}

	public String toString() {
		StringBuffer strBuf = new StringBuffer();

		if (isFainterThan()) {
			strBuf.append("<");
		} else if (isBrighterThan()) {
			strBuf.append(">");
		}

		strBuf.append(NumericPrecisionPrefs.formatMag(magValue));

		if (isUncertain) {
			strBuf.append(" (uncertain)");
		}

		if (uncertainty != 0) {
//			strBuf.append(" (\u00B1");
			strBuf.append(" (");
			strBuf.append(NumericPrecisionPrefs.formatMag(uncertainty));
			strBuf.append(")");
		}

		return strBuf.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isUncertain ? 1231 : 1237);
		result = prime * result + ((magModifier == null) ? 0 : magModifier.hashCode());
		long temp;
		temp = Double.doubleToLongBits(magValue);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(uncertainty);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		if (!(obj instanceof Magnitude)) {
			return false;
		}
		Magnitude other = (Magnitude) obj;
		if (isUncertain != other.isUncertain) {
			return false;
		}
		if (magModifier == null) {
			if (other.magModifier != null) {
				return false;
			}
		} else if (!magModifier.equals(other.magModifier)) {
			return false;
		}
		if (Double.doubleToLongBits(magValue) != Double.doubleToLongBits(other.magValue)) {
			return false;
		}
		if (Double.doubleToLongBits(uncertainty) != Double.doubleToLongBits(other.uncertainty)) {
			return false;
		}
		return true;
	}
}
