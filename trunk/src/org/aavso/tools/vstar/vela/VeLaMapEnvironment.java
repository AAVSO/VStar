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

import java.util.Map;

import org.aavso.tools.vstar.util.Pair;

/**
 * A VeLa environment that is backed by a Map.
 */
public class VeLaMapEnvironment extends AbstractVeLaEnvironment {

	private Map<String, Operand> map;

	public VeLaMapEnvironment(Map<String, Operand> map) {
		super();
		this.map = map;
	}

	@Override
	public Pair<Boolean, Operand> lookup(String name) {
		return new Pair<Boolean, Operand>(map.containsKey(name), map.get(name));
	}
}
