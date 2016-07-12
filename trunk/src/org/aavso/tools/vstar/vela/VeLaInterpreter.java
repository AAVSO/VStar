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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.Stack;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * VeLa: VStar expression Language interpreter
 */
public class VeLaInterpreter {

	// The number format for the locale with which the JVM was started.
	private static final NumberFormat FORMAT = NumberFormat
			.getNumberInstance(Locale.getDefault());

	// TODO: change to stack of Operand, templated on type vs Double?
	private Stack<Double> stack;

	public VeLaInterpreter() {
		stack = new Stack<Double>();
	}

	public double realExpression(String expr) throws VeLaParseError {

		// TODO: probably want a Flyweight pattern here:
		//       expression => double
		
		VeLaErrorListener errorListener = new VeLaErrorListener();

		CharStream stream = new ANTLRInputStream(expr);
		VeLaLexer lexer = new VeLaLexer(stream);
		lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
		lexer.addErrorListener(errorListener);
		CommonTokenStream tokens = new CommonTokenStream(lexer);

		VeLaParser parser = new VeLaParser(tokens);
		parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
		parser.addErrorListener(errorListener);
		VeLaParser.RealExpressionContext tree = parser.realExpression();

		RealExpressionListener listener = new RealExpressionListener(stack);
		ParseTreeWalker.DEFAULT.walk(listener, tree);

		return stack.pop();
	}

	/**
	 * Parse a string, returning a double primitive value, or if no valid double
	 * value is present, throw a NumberFormatException. The string is first
	 * trimmed of leading and trailing whitespace.
	 * 
	 * @param str
	 *            The string that (hopefully) contains a number.
	 * @return The double value corresponding to the initial parseable portion
	 *         of the string.
	 * @throws NumberFormatException
	 *             If no valid double value is present.
	 * 
	 */
	public static double parseDouble(String str) throws NumberFormatException {
		if (str == null) {
			throw new NumberFormatException("String was null");
		} else {
			try {
				str = str.trim();

				if (str.startsWith("+")) {
					// Leading "+" causes an exception to be thrown.
					str = str.substring(1);
				}

				if (str.contains("e")) {
					// Convert exponential indicator to parsable form.
					str = str.toUpperCase();
				}

				return FORMAT.parse(str).doubleValue();
			} catch (ParseException e) {
				throw new NumberFormatException(e.getLocalizedMessage());
			}
		}
	}
}
