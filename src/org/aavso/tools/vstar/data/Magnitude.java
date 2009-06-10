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

	public final static double ILLEGAL_UNCERTAINTY = -1;
	
	private double magValue;
	private boolean isFainterThan; // was the observation fainter than the specified magValue?
	private boolean isUncertain;
	private double uncertainty;

	/**
	 * Constructor.
	 * 
	 * @param isFainterThan Does this magValue constitute a fainter-than observation.
	 * @param isUncertain Is this an uncertain magValue value?
	 * @param magValue The magValue itself.
	 */
	public Magnitude(boolean isFainterThan, boolean isUncertain,
			double magnitude) {
		this.isFainterThan = isFainterThan;
		this.isUncertain = isUncertain;
		this.magValue = magnitude;
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
		return isFainterThan;
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

	@Override
	public String toString() {
		StringBuffer strBuf = new StringBuffer();
		
		if (isFainterThan) {
			strBuf.append("Fainter than ");
		}
		strBuf.append("Magnitude ");
		strBuf.append(magValue);
		if (isUncertain) {
			strBuf.append(" (uncertain");
			if (uncertainty != 0) {
				strBuf.append("ty: ");
				strBuf.append(uncertainty);
			}
			strBuf.append(")");
		}
		return strBuf.toString();
	}
}
