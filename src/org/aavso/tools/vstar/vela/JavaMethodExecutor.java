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
package org.aavso.tools.vstar.vela;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

/**
 * This class executes Java methods.
 */
public class JavaMethodExecutor extends FunctionExecutor {

    private Object instance;
    private Method method;

    /**
     * Construct a Java method executor.
     * 
     * @param instance       The instance of this class on which to invoke the
     *                       method.
     * @param funcName       The function's name.
     * @param method         The corresponding Java method object.
     * @param parameterNames The function's formal parameter names.
     * @param parameterTypes The function's parameter types.
     * @param returnType     The function's optional return type.
     * @param helpString     The optional help string.
     */
    public JavaMethodExecutor(Object instance, Method method, Optional<String> funcName, List<String> parameterNames,
            List<Type> parameterTypes, Optional<Type> returnType, Optional<String> helpString) {
        super(funcName, parameterNames, parameterTypes, returnType, helpString);
        this.instance = instance;
        this.method = method;
    }

    @Override
    public Optional<Operand> apply(List<Operand> operands) throws VeLaEvalError {
        return invokeJavaMethod(method, instance, operands, getReturnType());
    }

    private static Optional<Operand> invokeJavaMethod(Method method, Object instance, List<Operand> operands,
            Optional<Type> retType) {
        Operand result = null;

        try {
            Object obj = null;

            if (!Modifier.isStatic(method.getModifiers())) {
                // For non-static methods, if instance is null, assume the first
                // operand is an object instance.
                if (instance == null) {
                    obj = operands.get(0).toObject();
                    operands.remove(0);
                } else {
                    // ...otherwise, use what's been passed in.
                    obj = instance;
                }
            }

            // Note that this is the first use of Java 8
            // lambda expressions in VStar!

            // obj is null for static methods
            result = Operand.object2Operand(retType.get(),
                    method.invoke(obj, operands.stream().map(op -> op.toObject()).toArray()));

            Optional<Operand> retVal = null;

            if (result != null) {
                retVal = Optional.of(result);
            } else {
                retVal = Optional.of(Operand.NO_VALUE);
            }

            return retVal;

        } catch (InvocationTargetException e) {
            throw new VeLaEvalError(e.getLocalizedMessage());
        } catch (IllegalAccessException e) {
            throw new VeLaEvalError(e.getLocalizedMessage());
        }
    }
}
