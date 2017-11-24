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

import java.util.Collections;
import java.util.List;

import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * VeLa: VStar expression Language
 *
 * A class that represents typed operands.
 * 
 * Note: should cache Operand instances
 */
public class Operand {

	private Type type;
	private int intVal;
	private double doubleVal;
	private String stringVal;
	private boolean booleanVal;
	private List<Operand> listVal;

	public static Operand EMPTY_LIST = new Operand(Type.LIST,
			Collections.emptyList());

	public Operand(Type type, int value) {
		this.type = type;
		intVal = value;
	}

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

	public Operand(Type type, List<Operand> value) {
		this.type = type;
		listVal = value;
	}

	/**
	 * Given a VeLa type and a Java object, return an Operand instance.
	 * 
	 * @param type
	 *            The VeLa type.
	 * @param obj
	 *            The Java object.
	 * @return A corresponding Operand instance.
	 */
	public static Operand object2Operand(Type type, Object obj) {
		Operand operand = null;

		switch (type) {
		case INTEGER:
			operand = new Operand(Type.INTEGER, (int) obj);
			break;
		case DOUBLE:
			operand = new Operand(Type.DOUBLE, (double) obj);
			break;
		case STRING:
			operand = new Operand(Type.STRING, (String) obj);
			break;
		case BOOLEAN:
			operand = new Operand(Type.BOOLEAN, (boolean) obj);
			break;
		case LIST:
			operand = new Operand(Type.LIST, (List<Operand>) obj);
			break;
		}

		return operand;
	}

	/**
	 * Return a Java object corresponding to this Operand instance.
	 * 
	 * @return A corresponding Java object.
	 */
	public Object toObject() {
		Object obj = null;

		switch (type) {
		case INTEGER:
			obj = intVal;
			break;
		case DOUBLE:
			obj = doubleVal;
			break;
		case STRING:
			obj = stringVal;
			break;
		case BOOLEAN:
			obj = booleanVal;
			break;
		case LIST:
			obj = listVal;
			break;
		}

		return obj;
	}

	/**
	 * Convert this operand to the required type, if possible.
	 * 
	 * @param operand
	 *            The operand to be converted.
	 * @param requiredType
	 *            The required type.
	 * @return The converted type; will be unchanged if it matches the required
	 *         type or can't be converted.
	 */
	public Type convert(Type requiredType) {
		if (type != requiredType) {
			// Integer to double
			if (type == Type.INTEGER && requiredType == Type.DOUBLE) {
				setType(Type.DOUBLE);
				setDoubleVal((double) intVal);
			}
		}

		return type;
	}

	/**
	 * Convert this operand to a string.
	 */
	public void convertToString() {
		assert type != Type.STRING;

		switch (type) {
		case INTEGER:
			setStringVal(Integer.toString(intVal));
			setType(Type.STRING);
			break;
		case DOUBLE:
			setStringVal(NumericPrecisionPrefs.formatOther(doubleVal));
			setType(Type.STRING);
			break;
		case BOOLEAN:
			setStringVal(Boolean.toString(booleanVal));
			setType(Type.STRING);
			break;
		default:
			break;
		}
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @param intVal
	 *            the intVal to set
	 */
	public void setIntegerVal(int intVal) {
		this.intVal = intVal;
	}

	/**
	 * @param doubleVal
	 *            the doubleVal to set
	 */
	public void setDoubleVal(double doubleVal) {
		this.doubleVal = doubleVal;
	}

	/**
	 * @return the intVal
	 */
	public int intVal() {
		return intVal;
	}

	/**
	 * @return the doubleVal
	 */
	public double doubleVal() {
		return doubleVal;
	}

	/**
	 * @param stringVal
	 *            the stringVal to set
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
	 * @param booleanVal
	 *            the booleanVal to set
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

	/**
	 * @return the listVal
	 */
	public List<Operand> listVal() {
		return listVal;
	}

	/**
	 * @param listVal
	 *            the listVal to set
	 */
	public void setListVal(List<Operand> listVal) {
		this.listVal = listVal;
	}

	@Override
	public String toString() {
		String str = "";

		switch (type) {
		case INTEGER:
			str = Integer.toString(intVal);
			break;
		case DOUBLE:
			str = NumericPrecisionPrefs.formatOther(doubleVal);
			break;
		case BOOLEAN:
			str = Boolean.toString(booleanVal);
			break;
		case STRING:
			str = "\"" + stringVal + "\"";
			break;
		case LIST:
			str = listVal.toString().replace(",", "").replace("[", "'(")
					.replace("]", ")");
			break;
		}

		// str += " (" + type + ")";

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
		result = prime * result + intVal;
		result = prime * result + ((listVal == null) ? 0 : listVal.hashCode());
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
		if (intVal != other.intVal) {
			return false;
		}
		if (listVal == null) {
			if (other.listVal != null) {
				return false;
			}
		} else if (!listVal.equals(other.listVal)) {
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
