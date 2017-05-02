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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import org.aavso.tools.vstar.util.date.AbstractDateUtil;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * VeLa: VStar expression Language interpreter
 */
public class VeLaInterpreter {

	private Stack<Operand> stack;
	private Map<String, Operand> environment;

	// AST and result caches.
	private static Map<String, AST> exprToAST = new HashMap<String, AST>();
	private static Map<String, Operand> exprToResult = new HashMap<String, Operand>();

	private VeLaErrorListener errorListener;

	public VeLaInterpreter(Map<String, Operand> environment) {
		errorListener = new VeLaErrorListener();

		stack = new Stack<Operand>();

		if (environment != null) {
			this.environment = environment;
		} else {
			this.environment = Collections.emptyMap();
		}
	}

	public VeLaInterpreter() {
		this(null);
	}

	public void setEnvironment(Map<String, Operand> environment) {
		this.environment = environment;
	}

	/**
	 * Generic expression evaluation entry point.
	 * 
	 * @param expr
	 *            The expression string to be interpreted.
	 * @param verbose
	 *            Whether to output information messages.
	 * @return A result of the specified type.
	 * @throws VeLaParseError
	 *             If a parse error occurs.
	 */
	public Operand expression(String expr, ParserRuleContext tree,
			boolean verbose) throws VeLaParseError {

		AST ast = null;

		// Remove whitespace and change to lowercase to ensure a canonical
		// expression string for caching purposes.
		expr = expr.replace(" ", "").replace("\t", "").toLowerCase();

		// We cache abstract syntax trees by expression to improve performance.
		boolean astCached = false;
		if (exprToAST.containsKey(expr)) {
			ast = exprToAST.get(expr);
			astCached = true;
		} else {
			ExpressionListener listener = new ExpressionListener();
			ParseTreeWalker.DEFAULT.walk(listener, tree);

			ast = listener.getAST();
			exprToAST.put(expr, ast);
		}

		if (verbose) {
			System.out
					.println(String.format("%s [AST cached? %s]", ast, astCached));
		}

		Operand result;

		if (ast.isDeterministic() && exprToResult.containsKey(expr)) {
			// For deterministic expressions, we can also use cached results.
			// Note: a better description may be constant rather than
			// deterministic.
			result = exprToResult.get(expr);
			if (verbose) {
				System.out.println(String.format(
						"Result for AST '%s' in cache: %s", ast, result));
			}
		} else {
			// Evaluate the abstract syntax tree and cache the result.
			eval(ast);
			result = stack.pop();
			exprToResult.put(expr, result);
		}

		return result;
	}

	/**
	 * Real expression interpreter entry point with verbose parameter set false.
	 * 
	 * @param expr
	 *            The expression string to be interpreted.
	 * @return A real value result.
	 * @throws VeLaParseError
	 *             If a parse error occurs.
	 */
	public double realExpression(String expr) throws VeLaParseError {
		return realExpression(expr, false);
	}

	/**
	 * Real expression interpreter entry point.
	 * 
	 * @param expr
	 *            The expression string to be interpreted.
	 * @param verbose
	 *            Whether to output information messages.
	 * @return A real value result.
	 * @throws VeLaParseError
	 *             If a parse error occurs.
	 */
	public double realExpression(String expr, boolean verbose)
			throws VeLaParseError {

		VeLaParser.RealExpressionContext tree = getParser(expr)
				.realExpression();
		return expression(expr, tree, verbose).doubleVal();
	}

	/**
	 * Boolean expression interpreter entry point with verbose parameter set
	 * false.
	 * 
	 * @param expr
	 *            The expression string to be interpreted.
	 * @return A Boolean value result.
	 * @throws VeLaParseError
	 *             If a parse error occurs.
	 */
	public boolean booleanExpression(String expr) throws VeLaParseError {
		return booleanExpression(expr, false);
	}

	/**
	 * Boolean expression interpreter entry point.
	 * 
	 * @param expr
	 *            The expression string to be interpreted.
	 * @param verbose
	 *            Whether to output information messages.
	 * @return A Boolean value result.
	 * @throws VeLaParseError
	 *             If a parse error occurs.
	 */
	public boolean booleanExpression(String expr, boolean verbose)
			throws VeLaParseError {

		VeLaParser.BooleanExpressionContext tree = getParser(expr)
				.booleanExpression();
		return expression(expr, tree, verbose).booleanVal();
	}

	// Helpers

	/**
	 * Given an expression string, return a VeLa parser object.
	 * 
	 * @param expr
	 *            The expression string.
	 * @return The parser object.
	 */
	private VeLaParser getParser(String expr) {
		CharStream stream = new ANTLRInputStream(expr);

		VeLaLexer lexer = new VeLaLexer(stream);
		lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
		lexer.addErrorListener(errorListener);

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		return new VeLaParser(tokens);
	}

	/**
	 * <p>
	 * Given an AST representing a real expression, interpret this via a depth
	 * first traversal, leaving the result of evaluation on the stack.
	 * </p>
	 * <p>
	 * The name "eval" is used in deference to Lisp and John McCarthy's eval
	 * function, the equivalent of Maxwell's equations in Computer Science.
	 * 
	 * @param ast
	 *            The abstract syntax tree.
	 * @throws VeLaEvalError
	 *             If an evaluation error occurs.
	 */
	private void eval(AST ast) throws VeLaEvalError {
		if (ast.isLeaf() && ast.getOp() != Operation.FUNCTION
				&& ast.getOp() != Operation.VARIABLE) {
			switch (ast.getLiteralType()) {
			case DOUBLE:
				stack.push(new Operand(Type.DOUBLE, parseDouble(ast.getToken())));
				break;
			case STRING:
				stack.push(new Operand(Type.STRING, ast.getToken()));
				break;
			default:
				break;
			}
		} else {
			Operation op = ast.getOp();

			if (op.arity() == 2) {
				// Binary
				eval(ast.left());
				eval(ast.right());

				applyBinaryOperation(op);

			} else if (op.arity() == 1) {
				// Unary
				eval(ast.leaf());

				switch (op) {
				case NEG:
					stack.push(new Operand(Type.DOUBLE, -stack.pop()
							.doubleVal()));
					break;
				case NOT:
					stack.push(new Operand(Type.BOOLEAN, !stack.pop()
							.booleanVal()));
					break;
				default:
					break;
				}
			} else if (ast.getOp() == Operation.VARIABLE) {
				// Look up variable in the environment, pushing it on the stack
				// if it exists, throwing an exception if not.
				String varName = ast.getToken().toLowerCase();
				if (environment.containsKey(varName)) {
					stack.push(environment.get(varName));
				} else {
					throw new VeLaEvalError("Unknown variable: "
							+ ast.getToken());
				}
			} else if (ast.getOp() == Operation.FUNCTION) {
				// Evaluate parameters, if any.
				if (ast.getChildren() != null) {
					for (int i = ast.getChildren().size() - 1; i >= 0; i--) {
						eval(ast.getChildren().get(i));
					}
				}

				// Prepare parameter list.
				List<Operand> params = new ArrayList<Operand>();
				// TODO: check order
				while (!stack.isEmpty()) {
					params.add(stack.pop());
				}

				// Apply function to parameters.
				applyFunction(ast.getToken(), params);
			}
		}
	}

	/**
	 * Unify operand types by converting both operands to strings if only one is
	 * a string.
	 * 
	 * @param a
	 *            The first operand.
	 * @param b
	 *            The second operand.
	 * @return The final type of the unified operands.
	 */
	private Type unifyTypes(Operand a, Operand b) {
		Type type = a.getType();

		if (a.getType() != Type.STRING && b.getType() == Type.STRING) {
			convertToString(a);
		} else if (a.getType() == Type.STRING && b.getType() != Type.STRING) {
			convertToString(b);
		}

		return type;
	}

	/**
	 * Convert the specified operand to a string.
	 * 
	 * @param o
	 *            The operand to be converted to a string.
	 */
	private void convertToString(Operand o) {
		assert o.getType() != Type.STRING;

		switch (o.getType()) {
		case DOUBLE:
			o.setStringVal(NumericPrecisionPrefs.formatOther(o.doubleVal()));
			o.setType(Type.STRING);
			break;
		case BOOLEAN:
			o.setStringVal(Boolean.toString(o.booleanVal()));
			o.setType(Type.STRING);
			break;
		default:
			break;
		}
	}

	/**
	 * Apply a binary operation to the values on the stack, consuming them and
	 * leaving a result on the stack.
	 * 
	 * @param op
	 *            The operation to be applied.
	 */
	private void applyBinaryOperation(Operation op) {
		Operand operand2 = stack.pop();
		Operand operand1 = stack.pop();

		Type type = unifyTypes(operand1, operand2);

		switch (op) {
		case ADD:
			switch (type) {
			case DOUBLE:
				stack.push(new Operand(Type.DOUBLE, operand1.doubleVal()
						+ operand2.doubleVal()));
				break;
			case STRING:
				stack.push(new Operand(Type.STRING, operand1.stringVal()
						+ operand2.stringVal()));
			default:
			}
			break;
		case SUB:
			stack.push(new Operand(Type.DOUBLE, operand1.doubleVal()
					- operand2.doubleVal()));
			break;
		case MUL:
			stack.push(new Operand(Type.DOUBLE, operand1.doubleVal()
					* operand2.doubleVal()));
			break;
		case DIV:

			Double result = operand1.doubleVal() / operand2.doubleVal();
			if (!result.isInfinite()) {
				stack.push(new Operand(Type.DOUBLE, result));
			} else {
				throw new VeLaEvalError(String.format(
						"%s/%s: division by zero error", operand1.doubleVal(),
						operand2.doubleVal()));
			}
			break;
		case AND:
			stack.push(new Operand(Type.BOOLEAN, operand1.booleanVal()
					& operand2.booleanVal()));
			break;
		case OR:
			stack.push(new Operand(Type.BOOLEAN, operand1.booleanVal()
					| operand2.booleanVal()));
			break;
		case EQUAL:
			switch (type) {
			case DOUBLE:
				stack.push(new Operand(Type.BOOLEAN,
						operand1.doubleVal() == operand2.doubleVal()));
				break;
			case STRING:
				stack.push(new Operand(Type.BOOLEAN, operand1.stringVal()
						.equals(operand2.stringVal())));
				break;
			default:
			}
			break;
		case NOT_EQUAL:
			switch (type) {
			case DOUBLE:
				stack.push(new Operand(Type.BOOLEAN,
						operand1.doubleVal() != operand2.doubleVal()));
				break;
			case STRING:
				stack.push(new Operand(Type.BOOLEAN, !operand1.stringVal()
						.equals(operand2.stringVal())));
				break;
			default:
			}
			break;
		case GREATER_THAN:
			switch (type) {
			case DOUBLE:
				stack.push(new Operand(Type.BOOLEAN,
						operand1.doubleVal() > operand2.doubleVal()));
				break;
			case STRING:
				stack.push(new Operand(Type.BOOLEAN, operand1.stringVal()
						.compareTo(operand2.stringVal()) > 0));
				break;
			default:
			}
			break;
		case LESS_THAN:
			switch (type) {
			case DOUBLE:
				stack.push(new Operand(Type.BOOLEAN,
						operand1.doubleVal() < operand2.doubleVal()));
				break;
			case STRING:
				stack.push(new Operand(Type.BOOLEAN, operand1.stringVal()
						.compareTo(operand2.stringVal()) < 0));
				break;
			default:
			}
			break;
		case GREATER_THAN_OR_EQUAL:
			switch (type) {
			case DOUBLE:
				stack.push(new Operand(Type.BOOLEAN,
						operand1.doubleVal() >= operand2.doubleVal()));
				break;
			case STRING:
				stack.push(new Operand(Type.BOOLEAN, operand1.stringVal()
						.compareTo(operand2.stringVal()) >= 0));
				break;
			default:
			}
			break;
		case LESS_THAN_OR_EQUAL:
			switch (type) {
			case DOUBLE:
				stack.push(new Operand(Type.BOOLEAN,
						operand1.doubleVal() <= operand2.doubleVal()));
				break;
			case STRING:
				stack.push(new Operand(Type.BOOLEAN, operand1.stringVal()
						.compareTo(operand2.stringVal()) <= 0));
				break;
			default:
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Apply the function to the supplied parameter list, leaving the result on
	 * the stack.
	 * 
	 * @param funcName
	 *            The name of the function.
	 * @param params
	 *            The parameter list.
	 * @throws VeLaEvalError
	 *             If a function evaluation error occurs.
	 */
	private void applyFunction(String funcName, List<Operand> params)
			throws VeLaEvalError {
		String canonicalFuncName = funcName.toLowerCase();

		// TODO: create function executor objects (Strategy pattern)
		if (canonicalFuncName.equals("today")) {
			Calendar cal = Calendar.getInstance();
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH) + 1; // 0..11 -> 1..12
			int day = cal.get(Calendar.DAY_OF_MONTH);
			double jd = AbstractDateUtil.getInstance().calendarToJD(year,
					month, day);
			stack.push(new Operand(Type.DOUBLE, jd));
		} else {
			throw new VeLaEvalError("Unknown function: " + funcName);
		}
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
	 */
	private double parseDouble(String str) throws NumberFormatException {
		NumberFormat FORMAT = NumberFormat.getNumberInstance(Locale
				.getDefault());

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
