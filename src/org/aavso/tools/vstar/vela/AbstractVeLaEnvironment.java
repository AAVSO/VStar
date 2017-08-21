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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.util.Pair;

/**
 * All VeLa environments for symbol lookup must extend this abstract class.
 */
public abstract class AbstractVeLaEnvironment {

	// Named operand cache.
	private Map<String, Operand> intOperandCache;
	private Map<String, Operand> doubleOperandCache;
	private Map<String, Operand> stringOperandCache;
	private Map<String, Operand> booleanOperandCache;

	public AbstractVeLaEnvironment() {
		intOperandCache = new HashMap<String, Operand>();
		doubleOperandCache = new HashMap<String, Operand>();
		stringOperandCache = new HashMap<String, Operand>();
		booleanOperandCache = new HashMap<String, Operand>();
	}

	/**
	 * Lookup the named symbol in the environment.
	 * 
	 * @param name
	 *            The symbol's name.
	 * @return A pair of values, the first being whether or not it exists in the
	 *         environment, the second being the value as an Operand instance if
	 *         it exists, otherwise null.
	 */
	public abstract Pair<Boolean, Operand> lookup(String name);

	// Cached operator creation methods.

	protected Operand operand(String name, Integer value) {
		return operand(intOperandCache, Type.INTEGER, name, value);
	}

	protected Operand operand(String name, Double value) {
		return operand(doubleOperandCache, Type.DOUBLE, name, value);
	}

	protected Operand operand(String name, Boolean value) {
		return operand(stringOperandCache, Type.STRING, name, value);
	}

	protected Operand operand(String name, String value) {
		return operand(booleanOperandCache, Type.BOOLEAN, name, value);
	}

	// Helpers

	protected Operand operand(Map<String, Operand> cache, Type type,
			String name, Object value) {
		Operand operand = null;

		name = name.toUpperCase();

		if (cache.containsKey(name)) {
			operand = cache.get(name);
		} else {
			switch (type) {
			case INTEGER:
				operand = new Operand(type, (int) value);
				break;
			case DOUBLE:
				operand = new Operand(type, (double) value);
				break;
			case STRING:
				operand = new Operand(type, (String) value);
				break;
			case BOOLEAN:
				operand = new Operand(type, (boolean) value);
				break;
			case LIST:
				operand = new Operand(type, (List<Operand>) value);
			}

			cache.put(name, operand);
		}

		return operand;
	}
}
