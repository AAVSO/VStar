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
import java.util.Map;

import org.aavso.tools.vstar.util.Pair;

/**
 * All VeLa environments for symbol lookup must extend this abstract class.
 */
public abstract class AbstractVeLaEnvironment {

	// Named operand cache.
	private Map<String, Operand> operandCache;

	public AbstractVeLaEnvironment() {
		operandCache = new HashMap<String, Operand>();
	}

	/**
	 * Lookup the named symbol exist in the environment.
	 * 
	 * @param name
	 *            The symbol's name.
	 * @return A pair of values, the first being whether or not it exists in the
	 *         environment, the second being the value as an Operand instance if
	 *         it exists, otherwise null.
	 */
	public abstract Pair<Boolean, Operand> lookup(String name);

	// Cached operator creation methods.
	
	protected Operand operand(String name, double value) {
		Operand operand;

		if (operandCache.containsKey(name)) {
			operand = operandCache.get(name);
		} else {
			operand = new Operand(Type.DOUBLE, value);
		}

		return operand;
	}

	protected Operand operand(String name, boolean value) {
		Operand operand;

		if (operandCache.containsKey(name)) {
			operand = operandCache.get(name);
		} else {
			operand = new Operand(Type.BOOLEAN, value);
		}

		return operand;
	}

	protected Operand operand(String name, String value) {
		Operand operand;

		if (operandCache.containsKey(name)) {
			operand = operandCache.get(name);
		} else {
			operand = new Operand(Type.STRING, value);
		}

		return operand;
	}
}
