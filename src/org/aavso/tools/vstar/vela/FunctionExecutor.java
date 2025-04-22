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
import java.util.List;
import java.util.Optional;

/**
 * The base class for all function executors. Each subclass must implement
 * apply(). TODO: Note that there should really be a JavaFunctionExecutor
 * subclass as well, since we refer to Java methods in this class and have to
 * subclass it in VeLaInterpreter. parameterNames should also be part of the
 * base class as well, not just in UserDefinedFunction; that will also make
 * help() output better.
 */
public abstract class FunctionExecutor {

    // Note: this can go away when union types are implemented, especially since ANY
    // is not actually a type in VeLa.
    public static final List<Type> ANY_FORMALS = new ArrayList<Type>();

    public static final List<Type> NO_FORMALS = new ArrayList<Type>();
    public static final List<Operand> NO_ACTUALS = new ArrayList<Operand>();

    protected Optional<String> funcName;
    protected List<Type> parameterTypes;
    protected Optional<Type> returnType;
    protected Optional<String> helpString;

    protected Method method;

    /**
     * Apply the function to the specified operands and return a result of the
     * specified type.
     * 
     * @param operands A list if operands to which the function is to be applied.
     * @return The optional return value.
     * @throws A VeLaEvalError if an error occurred during function evaluation.
     */
    public abstract Optional<Operand> apply(List<Operand> operands) throws VeLaEvalError;

    /**
     * Constructor for functions with a corresponding Java method to invoke.
     * 
     * @param funcName       The function's name.
     * @param method         The corresponding Java method object.
     * @param parameterTypes The function's parameter types.
     * @param returnType     The function's optional return type.
     * @param helpString     The optional help string.
     */
    public FunctionExecutor(Optional<String> funcName, Method method, List<Type> parameterTypes,
            Optional<Type> returnType, Optional<String> helpString) {
        this.funcName = funcName;
        this.method = method;
        this.parameterTypes = parameterTypes;
        this.returnType = returnType;
        this.helpString = helpString;
    }

    /**
     * Constructor for functions with no Java method to invoke.
     *
     * @param funcName       The function's name.
     * @param parameterTypes The function's parameter types.
     * @param returnType     The function's return type.
     * @param helpString     The optional help string.
     */
    public FunctionExecutor(Optional<String> funcName, List<Type> parameterTypes, Optional<Type> returnType,
            Optional<String> helpString) {
        this(funcName, null, parameterTypes, returnType, helpString);
    }

    /**
     * Constructor for functions with no Java method to invoke.
     * 
     * @param funcName       The function's name.
     * @param parameterTypes The function's parameter types.
     * @param returnType     The function's return type.
     */
    public FunctionExecutor(Optional<String> funcName, List<Type> parameterTypes, Optional<Type> returnType) {
        this(funcName, null, parameterTypes, returnType, Optional.empty());
    }

    /**
     * Constructor for zero-arity functions with a corresponding Java method to
     * invoke.
     * 
     * @param funcName       The function's name.
     * @param method         The corresponding Java method object.
     * @param parameterTypes The function's parameter types.
     * @param returnType     The function's return type.
     * @param helpString     The optional help string.
     */
    public FunctionExecutor(Optional<String> funcName, Method method, Optional<Type> returnType,
            Optional<String> helpString) {
        this(funcName, method, NO_FORMALS, returnType, helpString);
    }

    /**
     * Constructor for zero-arity functions.
     * 
     * @param funcName       The function's name.
     * @param parameterTypes The function's parameter types.
     * @param returnType     The function's return type.
     * @param helpString     The optional help string.
     */
    public FunctionExecutor(Optional<String> funcName, Optional<Type> returnType, Optional<String> helpString) {
        this(funcName, null, NO_FORMALS, returnType, helpString);
    }

    /**
     * Do the specified actual parameters conform to the function's formal parameter
     * list? If any operands are converted to a different type, they will be
     * replaced with a new operand in the actual parameter list to prevent side
     * effects.
     * 
     * @param actualParameters A list of actual parameters (Operands).
     * @return Whether or not the actuals conform to the formals.
     */
    public boolean conforms(List<Operand> actualParameters) {
        boolean result = true;

        if (parameterTypes == ANY_FORMALS) {
            result = true;
        } else if (actualParameters.size() != parameterTypes.size()) {
            result = false;
        } else {
            for (int i = 0; i < actualParameters.size(); i++) {
                Type requiredType = parameterTypes.get(i);
                Operand originalVal = actualParameters.get(i);
                Operand convertedVal = originalVal.convert(requiredType);
                if (convertedVal.getType() != requiredType) {
                    result = false;
                    break;
                } else if (convertedVal != originalVal) {
                    actualParameters.set(i, convertedVal);
                }
            }
        }

        return result;
    }

    /**
     * @return the funcName
     */
    public Optional<String> getFuncName() {
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
    public Optional<Type> getReturnType() {
        return returnType;
    }

    /**
     * @param returnType the returnType to set
     */
    public void setReturnType(Optional<Type> returnType) {
        this.returnType = returnType;
    }

    public Optional<String> getHelpString() {
        return helpString;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        String paramsStr;

        if (parameterTypes == ANY_FORMALS) {
            paramsStr = "ANY";
        } else {
            paramsStr = parameterTypes.toString().replace("[", "").replace("]", "").replace(",", "");
        }

        buf.append(String.format("%s (%s)", funcName.isPresent() ? funcName.get() : "λ", paramsStr));

        if (returnType.isPresent()) {
            buf.append(String.format(" : %s", returnType.get()));
        }

        return buf.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((funcName == null) ? 0 : funcName.hashCode());
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        result = prime * result + ((parameterTypes == null) ? 0 : parameterTypes.hashCode());
        result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
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
        if (!(obj instanceof FunctionExecutor)) {
            return false;
        }
        FunctionExecutor other = (FunctionExecutor) obj;
        if (funcName == null) {
            if (other.funcName != null) {
                return false;
            }
        } else if (!funcName.equals(other.funcName)) {
            return false;
        }
        if (method == null) {
            if (other.method != null) {
                return false;
            }
        } else if (!method.equals(other.method)) {
            return false;
        }
        if (parameterTypes == null) {
            if (other.parameterTypes != null) {
                return false;
            }
        } else if (!parameterTypes.equals(other.parameterTypes)) {
            return false;
        }
        if (returnType != other.returnType) {
            return false;
        }
        return true;
    }
}
