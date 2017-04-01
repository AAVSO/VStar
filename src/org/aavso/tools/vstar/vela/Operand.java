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
package org.aavso.tools.vstar.vela;

import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * VeLa: VStar expression Language
 *
 * A class that represents typed operands. 
 */
public class Operand {

	private Type type;
	private double doubleVal;
	private String stringVal;
	private boolean booleanVal;
	
	public Operand(Type type, double value) {
		this.type = type;
		doubleVal = value;
	}

	public Operand(Type type, String value) {
		this.type = type;
		stringVal = value;
	}

	public Operand(Type type, boolean value) {
		this.type = type;
		booleanVal = value;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @param doubleVal the doubleVal to set
	 */
	public void setDoubleVal(double doubleVal) {
		this.doubleVal = doubleVal;
	}

	/**
	 * @return the doubleVal
	 */
	public double doubleVal() {
		return doubleVal;
	}

	/**
	 * @param stringVal the stringVal to set
	 */
	public void setStringVal(String stringVal) {
		this.stringVal = stringVal;
	}

	/**
	 * @return the stringVal
	 */
	public String stringVal() {
		return stringVal;
	}

	/**
	 * @param booleanVal the booleanVal to set
	 */
	public void setBooleanVal(boolean booleanVal) {
		this.booleanVal = booleanVal;
	}

	/**
	 * @return the booleanVal
	 */
	public boolean booleanVal() {
		return booleanVal;
	}

	@Override
	public String toString() {
		String str = "";
		
		switch(type) {
		case STRING:
			str = stringVal;
			break;
		case DOUBLE:
			str = NumericPrecisionPrefs.formatOther(doubleVal);
			break;
		case BOOLEAN:
			str = Boolean.toString(booleanVal);
			break;
		}
		
		str += " (" + type + ")";
		
		return str;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (booleanVal ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(doubleVal);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((stringVal == null) ? 0 : stringVal.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Operand)) {
			return false;
		}
		Operand other = (Operand) obj;
		if (booleanVal != other.booleanVal) {
			return false;
		}
		if (Double.doubleToLongBits(doubleVal) != Double
				.doubleToLongBits(other.doubleVal)) {
			return false;
		}
		if (stringVal == null) {
			if (other.stringVal != null) {
				return false;
			}
		} else if (!stringVal.equals(other.stringVal)) {
			return false;
		}
		if (type != other.type) {
			return false;
		}
		return true;
	}
}
