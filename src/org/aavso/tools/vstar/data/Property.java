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
 * This class represents property of a particular type. The intention is to use
 * instances of this class in the context of ValidObservation where string
 * properties (details) would once have been used.
 */
public class Property {

	public static enum propType {
		INTEGER, REAL, BOOLEAN, STRING
	};

	private propType type;

	private int intVal;
	private double realVal;
	private boolean boolVal;
	private String strVal;

	public Property(int val) {
		type = propType.INTEGER;
		intVal = val;
	}

	public Property(double val) {
		type = propType.REAL;
		realVal = val;
	}

	public Property(boolean val) {
		type = propType.BOOLEAN;
		boolVal = val;
	}

	public Property(String val) {
		type = propType.STRING;
		strVal = val;
	}

	public propType getType() {
		return type;
	}

	public int getIntVal() {
		return intVal;
	}

	public double getRealVal() {
		return realVal;
	}

	public boolean getBoolVal() {
		return boolVal;
	}

	public String getStrVal() {
		return strVal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (boolVal ? 1231 : 1237);
		result = prime * result + intVal;
		long temp;
		temp = Double.doubleToLongBits(realVal);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((strVal == null) ? 0 : strVal.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Property other = (Property) obj;
		if (boolVal != other.boolVal)
			return false;
		if (intVal != other.intVal)
			return false;
		if (Double.doubleToLongBits(realVal) != Double.doubleToLongBits(other.realVal))
			return false;
		if (strVal == null) {
			if (other.strVal != null)
				return false;
		} else if (!strVal.equals(other.strVal))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		String str = null;
		
		switch(type) {
		case INTEGER:
			str = String.valueOf(intVal);
			break;
		case REAL:
			str = String.valueOf(realVal);
			break;
		case BOOLEAN:
			str = String.valueOf(boolVal);
			break;
		case STRING:
			str = strVal;
			break;
		}
		
		return str;
	}
}
