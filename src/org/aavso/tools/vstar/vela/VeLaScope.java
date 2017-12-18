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
 * each correspond to one-time variable or function bindings such that binding a
 * variable or function twice is considered an error.
 */
public class VeLaScope extends VeLaEnvironment<Operand> {

	// A multi-map of names to potentially overloaded functions.
	private Map<String, List<FunctionExecutor>> functions;

	public VeLaScope() {
		super();
		functions = new HashMap<String, List<FunctionExecutor>>();
	}

	/**
	 * Bind a value to a name, iff it has not already been bound in this
	 * environment, otherwise throw an exception.
	 * 
	 * @param name
	 *            The name to which to bind the value.
	 * @param value
	 *            The value to be bound.
	 * @throws VeLaEvalError
	 *             if the name is already bound.
	 */
	@Override
	public void bind(String name, Operand value) {
//		if (value.getType() == Type.FUNCTION) {
//			addFunctionExecutor(value.functionVal());
//		} else 
		if (!lookup(name).isPresent()) {
			super.bind(name, value);
		} else {
			throw new VeLaEvalError("'" + name
					+ "' already bound in this environment.");
		}
	}

	/**
	 * Add a function executor to the multi-map.
	 * 
	 * @param executor
	 *            The function executor to be added.
	 */
	public void addFunctionExecutor(FunctionExecutor executor) {
		List<FunctionExecutor> executors = functions
				.get(executor.getFuncName().get());

		if (executors == null) {
			executors = new ArrayList<FunctionExecutor>();
			functions.put(executor.getFuncName().get(), executors);
		}

		executors.add(executor);
	}

	/**
	 * Lookup the named function, returning an optional list of functions.
	 * 
	 * @param name
	 *            The desired function name.
	 * @return An optional list of function executors.
	 */
	public Optional<List<FunctionExecutor>> lookupFunction(String name) {
		return Optional.of(functions.get(name));
	}
}
