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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class represents a VeLa scope assumed to exist within a stack of scopes,
 * with a global scope at the bottom and function scopes thereafter. Symbols in
 * each correspond to variable or function bindings.
 */
public class VeLaScope extends VeLaEnvironment<Operand> {

	// A multi-map of names to potentially overloaded functions.
	private Map<String, List<FunctionExecutor>> functions;

	public VeLaScope() {
		super();
		functions = new HashMap<String, List<FunctionExecutor>>();
	}

	/**
	 * @return the functions
	 */
	public Map<String, List<FunctionExecutor>> getFunctions() {
		return functions;
	}

	/**
	 * Add a function executor to the multi-map.
	 * 
	 * @param executor
	 *            The function executor to be added.
	 */
	public void addFunctionExecutor(FunctionExecutor executor) {
		List<FunctionExecutor> executors = functions.get(executor.getFuncName()
				.get());

		if (executors == null) {
			executors = new ArrayList<FunctionExecutor>();
			functions.put(executor.getFuncName().get(), executors);
		}

		executors.add(executor);
	}

	/**
	 * Add all symbol bindings and functions from another scope to this one.
	 * 
	 * @param other
	 *            The scope to be added to this one.
	 */
	public void addAll(VeLaScope other) {
		cache.putAll(other.cache);
		functions.putAll(other.functions);
	}

	/**
	 * Lookup the named function, returning an optional list of functions.
	 * 
	 * @param name
	 *            The desired function name.
	 * @return An optional list of function executors.
	 */
	public Optional<List<FunctionExecutor>> lookupFunction(String name) {
		Optional<List<FunctionExecutor>> funList = Optional.empty();

		if (functions.containsKey(name)) {
			funList = Optional.of(functions.get(name));
		}

		return funList;
	}
	
	/**
	 * Is this scope empty?
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && functions.isEmpty();
	}
}
