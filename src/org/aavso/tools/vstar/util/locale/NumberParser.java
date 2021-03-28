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
package org.aavso.tools.vstar.util.locale;

import java.util.Collections;

import org.aavso.tools.vstar.vela.Operand;
import org.aavso.tools.vstar.vela.VeLaEvalError;
import org.aavso.tools.vstar.vela.VeLaInterpreter;
import org.aavso.tools.vstar.vela.VeLaParseError;

/**
 * This class contains static method that parses numeric expressions in a
 * locale-independent way, intended as replacements for the Double method of the
 * same name.
 */
public class NumberParser {

	private final static boolean VERBOSE = false;
	
	private final static boolean ADD_VSTAR_API = false;

	private static VeLaInterpreter vela =
			new VeLaInterpreter(VERBOSE, ADD_VSTAR_API, Collections.emptyList());

	/**
	 * Parses a string, returning a double primitive value, or if no valid
	 * double value is present, throws an exception.
	 * 
	 * @param str
	 *            The string that (hopefully) contains a numeric VeLa
	 *            expression.
	 * @return The double value corresponding to the evaluated string.
	 * @throws VeLaParseError
	 *             If a parse error occurs.
	 * @throws VeLaEvalError
	 *             If an evaluation error occurs.
	 * @throws NumberFormatException
	 *             If there was no valid double value resulting from the parse
	 *             (the VeLa expression did not evaluate to a double or
	 *             integer).
	 */
	public static double parseDouble(String str) throws VeLaParseError,
			VeLaEvalError, NumberFormatException {

		Operand operand = vela.expressionToOperand(str);

		double result = Double.NaN;

		switch (operand.getType()) {
		case INTEGER:
			result = operand.intVal();
			break;

		case REAL:
			result = operand.doubleVal();
			break;

		default:
			throw new NumberFormatException();
		}

		return result;
	}

	/**
	 * Parses a string, returning an integer primitive value, or if no valid
	 * integer value is present, throws an exception.
	 * 
	 * @param str
	 *            The string that (hopefully) contains an integer VeLa
	 *            expression.
	 * @return The integer value corresponding to the evaluated string.
	 * @throws VeLaParseError
	 *             If a parse error occurs.
	 * @throws VeLaEvalError
	 *             If an evaluation error occurs.
	 * @throws NumberFormatException
	 *             If there was no valid integer value resulting from the parse
	 *             (the VeLa expression did not evaluate to an integer).
	 */
	public static int parseInteger(String str) throws VeLaParseError,
			VeLaEvalError, NumberFormatException {

		Operand operand = vela.expressionToOperand(str);

		int result = 0;

		switch (operand.getType()) {
		case INTEGER:
			result = operand.intVal();
			break;

		default:
			throw new NumberFormatException();
		}

		return result;
	}
}
