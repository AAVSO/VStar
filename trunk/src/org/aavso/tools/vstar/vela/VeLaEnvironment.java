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
import java.util.Optional;

/**
 * An unrestrictive symbol binding and lookup environment implementation.
 */
public class VeLaEnvironment<T> {

	// Named operand cache
	protected Map<String, T> cache;

	public VeLaEnvironment() {
		cache = new HashMap<String, T>();
	}

	public VeLaEnvironment(Map<String, T> map) {
		cache = map;
	}

	/**
	 * Lookup the named symbol in the environment.
	 * 
	 * @param name
	 *            The symbol's name.
	 * @return An optional Operand instance if it exists.
	 */
	public Optional<T> lookup(String name) {
		return Optional.ofNullable(cache.get(name.toUpperCase()));
	}

	/**
	 * Bind a value to a name.
	 * 
	 * @param name
	 *            The name to which to bind the value.
	 * @param value
	 *            The value to be bound.
	 */
	public void bind(String name, T value) {
		if (!cache.containsKey(name)) {
			cache.put(name, value);
		}
	}
	
	/**
	 * Is this VeLa environment empty?
	 */
	public boolean isEmpty() {
		return cache.isEmpty();
	}
}
