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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The base class for all function executors. Each subclass must implement
 * apply().
 */
public abstract class FunctionExecutor {

	public static final List<Type> NO_FORMALS = Collections.emptyList();
	public static final List<Operand> NO_ACTUALS = new ArrayList<Operand>();

	private String funcName;
	private Method method;
	private List<Type> parameterTypes;
	private Type returnType;

	/**
	 * Apply the function to the specified operands and return a result of the
	 * specified type.
	 * 
	 * @param operands
	 *            A list if operands to which the function is to be applied.
	 * @return The return value.
	 */
	public abstract Operand apply(List<Operand> operands);

	/**
	 * Constructor for functions with a corresponding Java method to invoke.
	 * 
	 * @param funcName
	 *            The function's name.
	 * @param method
	 *            The corresponding Java method object.
	 * @param parameterTypes
	 *            The function's parameter types.
	 * @param returnType
	 *            The function's return type.
	 */
	public FunctionExecutor(String funcName, Method method,
			List<Type> parameterTypes, Type returnType) {
		this.funcName = funcName;
		this.method = method;
		this.parameterTypes = parameterTypes;
		this.returnType = returnType;
	}

	/**
	 * Constructor
	 * 
	 * @param funcName
	 *            The function's name.
	 * @param parameterTypes
	 *            The function's parameter types.
	 * @param returnType
	 *            The function's return type.
	 */
	public FunctionExecutor(String funcName, List<Type> parameterTypes,
			Type returnType) {
		this(funcName, null, parameterTypes, returnType);
	}

	/**
	 * Constructor for zero-arity functions with a corresponding Java method to
	 * invoke.
	 * 
	 * @param funcName
	 *            The function's name.
	 * @param method
	 *            The corresponding Java method object.
	 * @param parameterTypes
	 *            The function's parameter types.
	 * @param returnType
	 *            The function's return type.
	 */
	public FunctionExecutor(String funcName, Method method, Type returnType) {
		this(funcName, method, NO_FORMALS, returnType);
	}

	/**
	 * Constructor for zero-arity functions.
	 * 
	 * @param funcName
	 *            The function's name.
	 * @param parameterTypes
	 *            The function's parameter types.
	 * @param returnType
	 *            The function's return type.
	 */
	public FunctionExecutor(String funcName, Type returnType) {
		this(funcName, null, NO_FORMALS, returnType);
	}

	/**
	 * Do the specified actual parameters conform to the function's formal
	 * parameter list?
	 * 
	 * @param actualParameters
	 *            A list of actual parameters (Operands).
	 * @return Whether or not the actuals conform to the formals.
	 */
	public boolean conforms(List<Operand> actualParameters) {
		boolean result = true;

		if (actualParameters.size() != parameterTypes.size()) {
			result = false;
		} else {
			for (int i = 0; i < actualParameters.size(); i++) {
				Type requiredType = parameterTypes.get(i);
				if (actualParameters.get(i).convert(requiredType) != requiredType) {
					result = false;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * @return the funcName
	 */
	public String getFuncName() {
		return funcName;
	}

	/**
	 * @return the method
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * @return the parameterTypes
	 */
	public List<Type> getParameterTypes() {
		return parameterTypes;
	}

	/**
	 * @return the returnType
	 */
	public Type getReturnType() {
		return returnType;
	}

	public String toString() {
		return String.format("%s :: %s -> %s", funcName, parameterTypes,
				returnType);
	}
}
