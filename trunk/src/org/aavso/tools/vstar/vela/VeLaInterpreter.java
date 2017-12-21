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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.aavso.tools.vstar.util.date.AbstractDateUtil;
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

	private boolean verbose;

	private Stack<Operand> stack;

	private Stack<VeLaEnvironment<Operand>> environments;

	// AST and result caches.
	private static Map<String, AST> exprToAST = new HashMap<String, AST>();

	// TODO: make use of AST => Operand caching at each level of eval()! AST
	// needs to be key not VeLa string; need equals, hashCode on AST

	private static Map<String, Operand> exprToResult = new HashMap<String, Operand>();

	// Regular expression pattern cache.
	private static Map<String, Pattern> regexPatterns = new HashMap<String, Pattern>();

	private VeLaErrorListener errorListener;

	/**
	 * Construct a VeLa interpreter with an initial scope and intrinsic
	 * functions.
	 * 
	 * @param verbose
	 *            Verbose mode?
	 */
	public VeLaInterpreter(boolean verbose) {
		errorListener = new VeLaErrorListener();

		stack = new Stack<Operand>();
		environments = new Stack<VeLaEnvironment<Operand>>();

		environments.push(new VeLaScope());

		initBindings();
		initFunctionExecutors();

		this.verbose = verbose;
	}

	/**
	 * Construct a VeLa interpreter with verbose mode set to false.
	 */
	public VeLaInterpreter() {
		this(false);
	}

	/**
	 * Push an environment onto the stack.
	 * 
	 * @param environment
	 *            The environment to be pushed.
	 */
	public void pushEnvironment(VeLaEnvironment<Operand> environment) {
		this.environments.push(environment);
	}

	/**
	 * Pop the top-most environment from the stack and return it.
	 * 
	 * @return The top-most environment.
	 */
	public VeLaEnvironment<Operand> popEnvironment() {
		return this.environments.pop();
	}

	/**
	 * VeLa program interpreter entry point.
	 * 
	 * @param prog
	 *            The VeLa program string to be interpreted.
	 * @return An optional result, depending upon whether a value was left on
	 *         the stack..
	 * @throws VeLaParseError
	 *             If a parse error occurs.
	 * @throws VeLaEvalError
	 *             If an evaluation error occurs.
	 */
	public Optional<Operand> program(String prog) throws VeLaParseError {
		VeLaParser.SequenceContext tree = getParser(prog).sequence();
		return commonInterpreter(prog, tree);
	}

	/**
	 * Real expression interpreter entry point.
	 * 
	 * @param expr
	 *            The expression string to be interpreted.
	 * @return A real value result.
	 * @throws VeLaParseError
	 *             If a parse error occurs.
	 * @throws VeLaEvalError
	 *             If an evaluation error occurs.
	 */
	public double realExpression(String expr) throws VeLaEvalError {

		VeLaParser.AdditiveExpressionContext tree = getParser(expr)
				.additiveExpression();

		Optional<Operand> result = commonInterpreter(expr, tree);

		if (result.isPresent()) {
			if (result.get().getType() == Type.DOUBLE) {
				return (double) result.get().doubleVal();
			} else if (result.get().getType() == Type.INTEGER) {
				return result.get().intVal();
			} else {
				throw new VeLaEvalError("Numeric value expected as result");
			}
		} else {
			throw new VeLaEvalError("Numeric value expected as result");
		}
	}

	/**
	 * Real expression interpreter entry point.
	 * 
	 * @param expr
	 *            The expression string to be interpreted.
	 * @return An operand.
	 * @throws VeLaParseError
	 *             If a parse error occurs.
	 * @throws VeLaEvalError
	 *             If an evaluation error occurs.
	 */
	public Operand expressionToOperand(String expr) throws VeLaParseError {

		VeLaParser.AdditiveExpressionContext tree = getParser(expr)
				.additiveExpression();

		Optional<Operand> result = commonInterpreter(expr, tree);

		if (result.isPresent()) {
			return result.get();
		} else {
			throw new VeLaEvalError("Result expected");
		}
	}

	/**
	 * VeLa boolean expression interpreter entry point.
	 * 
	 * @param expr
	 *            The VeLa expression string to be interpreted.
	 * @return A Boolean value result.
	 * @throws VeLaParseError
	 *             If a parse error occurs.
	 * @throws VeLaEvalError
	 *             If an evaluation error occurs.
	 */
	public boolean booleanExpression(String expr) throws VeLaParseError {

		VeLaParser.BooleanExpressionContext tree = getParser(expr)
				.booleanExpression();

		Optional<Operand> result = commonInterpreter(expr, tree);

		if (result.isPresent()) {
			return result.get().booleanVal();
		} else {
			throw new VeLaEvalError("Numeric value expected as result");
		}
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
	 * Common parse tree walker and AST generator.
	 * 
	 * @param prog
	 *            The VeLa program to be interpreted.
	 * @param tree
	 *            The parse tree resulting from parsing the VeLa expression.
	 * @return The abstract syntax tree created by walking the parse tree.
	 * @throws VeLaParseError
	 *             If a parse error occurs.
	 */
	public AST commonParseTreeWalker(String prog, ParserRuleContext tree)
			throws VeLaParseError {

		AST ast = null;

		// Remove whitespace and change to uppercase to ensure a canonical
		// expression string for caching purposes.
		prog = prog.replace(" ", "").replace("\t", "").toUpperCase();

		// We cache abstract syntax trees by expression to improve performance.
		boolean astCached = false;
		if (exprToAST.containsKey(prog)) {
			ast = exprToAST.get(prog);
			astCached = true;
		} else {
			ExpressionListener listener = new ExpressionListener();
			ParseTreeWalker.DEFAULT.walk(listener, tree);

			if (listener.isASTPresent()) {
				ast = listener.getAST();
				// This relates a VeLa program or expression to a sequence of
				// ASTs.
				exprToAST.put(prog, ast);
			}
		}

		if (verbose && ast != null) {
			if (astCached) {
				System.out.println(String.format("%s [AST cached]", ast));
			} else {
				System.out.println(ast);
			}
		}

		return ast;
	}

	/**
	 * Common VeLa evaluation entry point. This will be most effective where
	 * prog is an often used expression.
	 * 
	 * @param prog
	 *            The VeLa program string to be interpreted.
	 * @param tree
	 *            The result of parsing the VeLa expression.
	 * @return An optional result depending upon whether a value is left on the
	 *         stack.
	 * @throws VeLaEvalError
	 *             If an evaluation error occurs.
	 */
	public Optional<Operand> commonInterpreter(String prog,
			ParserRuleContext tree) throws VeLaParseError {

		Optional<Operand> result = Optional.empty();

		AST ast = commonParseTreeWalker(prog, tree);

		if (ast != null) {

			// TODO: we should map from AST to Operand not String to Operand;
			// then we really can cache within eval() at every level, not
			// not just at the top level! Need to add equals() and hashCode() to
			// AST; this is especially so now that we are dealing with lists
			// (sequences; an implicit special form) of ASTs.

			if (ast.isDeterministic() && exprToResult.containsKey(prog)) {

				// For deterministic expressions, we can also use cached
				// results.
				// Note: a better description may be constant rather than
				// deterministic.
				result = Optional.of(exprToResult.get(prog));
				if (verbose) {
					System.out.println(String.format("%s [result cached: %s]",
							ast, result));
				}
			} else {
				// Evaluate the abstract syntax tree and cache the result.
				eval(ast);
				if (!stack.isEmpty()) {
					result = Optional.of(stack.pop());
					exprToResult.put(prog, result.get());
				} else {
					result = Optional.empty();
				}
			}
		}

		return result;
	}

	/**
	 * <p>
	 * Given an AST representing a real expression, interpret this via a depth
	 * first traversal, leaving the result of evaluation on the stack.
	 * </p>
	 * <p>
	 * The name "eval" is used in deference to Lisp and John McCarthy's eval
	 * function, the equivalent of Maxwell's equations in Computer Science.
	 * </p>
	 * <p>
	 * I've also just noticed that VeLa is an anagram of eval! :)
	 * </p>
	 * 
	 * @param ast
	 *            The abstract syntax tree.
	 * @throws VeLaEvalError
	 *             If an evaluation error occurs.
	 */
	public void eval(AST ast) throws VeLaEvalError {
		if (ast.isLiteral()) {
			stack.push(ast.getOperand());
		} else {
			Operation op = ast.getOp();

			if (op.arity() == 2) {
				// Binary
				eval(ast.left());
				eval(ast.right());

				applyBinaryOperation(op);

			} else if (op.arity() == 1) {
				// Unary
				eval(ast.child());

				Operand operand = stack.pop();

				switch (op) {
				case NEG:
					switch (operand.getType()) {
					case INTEGER:
						stack.push(new Operand(Type.INTEGER, -operand.intVal()));
						break;
					case DOUBLE:
						stack.push(new Operand(Type.DOUBLE, -operand
								.doubleVal()));
						break;
					default:
					}
					break;
				case NOT:
					stack.push(new Operand(Type.BOOLEAN, !operand.booleanVal()));
					break;
				default:
					break;
				}
			} else if (ast.getOp() == Operation.SYMBOL) {
				// Look up variable in the environment stack, pushing it onto
				// the operand stack if it exists, looking for and evaluating a
				// function if not, throwing an exception otherwise.
				String varName = ast.getToken().toUpperCase();
				// TODO: lookupBinding also needs to call lookupFunctions() and
				// create an operand from a function name; crucial for HOFs!
				// The first function will in the list will have to be chosen in
				// the absence of parameter type information
				Optional<Operand> result = lookupBinding(varName);
				if (result.isPresent()) {
					stack.push(result.get());
				} else {
					// TODO: or function...
					throw new VeLaEvalError("Unknown variable: \""
							+ ast.getToken() + "\"");
				}
			} else if (ast.getOp() == Operation.LIST) {
				// Evaluate list elements.
				List<Operand> elements = new ArrayList<Operand>();

				if (ast.hasChildren()) {
					for (int i = ast.getChildren().size() - 1; i >= 0; i--) {
						eval(ast.getChildren().get(i));
					}

					// Create and push list of operands.
					for (int i = 1; i <= ast.getChildren().size(); i++) {
						elements.add(stack.pop());
					}
				}

				stack.push(new Operand(Type.LIST, elements));
			} else if (ast.getOp().isSpecialForm()) {
				specialForm(ast);
			}
		}
	}

	/**
	 * Handle special forms.
	 * 
	 * @param ast
	 *            The special form's AST.
	 */
	private void specialForm(AST ast) {
		switch (ast.getOp()) {
		case SEQUENCE:
			// Evaluate each child AST in turn. No children means an empty
			// program or one consisting only of whitespace or comments.
			if (ast.hasChildren()) {
				for (AST child : ast.getChildren()) {
					eval(child);
				}
			}
			break;

		case BIND:
			eval(ast.right());
			environments.peek().bind(ast.left().getToken(), stack.pop());
			break;

		case FUNDEF:
			// Does this function have a name or is it anonymous?
			Optional<String> name = Optional.empty();
			if (ast.getChildren().get(0).getOp() == Operation.SYMBOL) {
				name = Optional.of(ast.getChildren().get(0).getToken());
			}

			// Extract components from AST in order to create a function
			// executor.
			List<String> parameterNames = new ArrayList<String>();
			List<Type> parameterTypes = new ArrayList<Type>();
			Optional<Type> returnType = Optional.empty();
			Optional<AST> functionBody = Optional.empty();

			for (int i = name.isPresent() ? 1 : 0; i < ast.getChildren().size(); i++) {
				AST child = ast.getChildren().get(i);
				switch (child.getOp()) {
				case PAIR:
					parameterNames.add(child.left().getToken());
					parameterTypes
							.add(Type.name2Vela(child.right().getToken()));
					break;

				case SYMBOL:
					returnType = Optional.of(Type.name2Vela(child.getToken()));
					break;

				case SEQUENCE:
					functionBody = Optional.of(child);
					break;

				default:
					break;
				}
			}

			// Add the named function to the top-most scope's function namespace
			// or the push the anonymous function to the operand stack.
			UserDefinedFunctionExecutor function = new UserDefinedFunctionExecutor(
					this, name, parameterNames, parameterTypes, returnType,
					functionBody);

			if (name.isPresent()) {
				addFunctionExecutor(function);
			} else {
				stack.push(new Operand(Type.FUNCTION, function));
			}

			break;

		case FUNCALL:
			List<Operand> params = new ArrayList<Operand>();

			FunctionExecutor anon = null;

			if (ast.hasChildren()) {
				for (int i = ast.getChildren().size() - 1; i >= 0; i--) {
					eval(ast.getChildren().get(i));
				}

				// Prepare actual parameter list.
				for (int i = 1; i <= ast.getChildren().size(); i++) {
					Operand value = stack.pop();
					if (value.getType() != Type.FUNCTION) {
						params.add(value);
					} else {
						anon = value.functionVal();
					}
				}
			}

			// Apply function to actual parameters.
			if (anon == null) {
				applyFunction(ast.getToken(), params);
			} else {
				if (!applyFunction(anon, params)) {
					throw new VeLaEvalError(
							"Invalid parameters for function: \"" + anon + "\"");
				}
			}
			break;

		case SELECT:
			// Evaluate each antecedent in turn, pushing the value
			// of the first consequent whose antecedent is true and stop
			// antecedent evaluation.
			for (AST pair : ast.getChildren()) {
				eval(pair.left());
				if (stack.pop().booleanVal()) {
					eval(pair.right());
					break;
				}
			}
			break;

		case OUT:
			// Evaluate and print each AST.
			for (AST child : ast.getChildren()) {
				eval(child);
				System.out.print(stack.pop().toHumanReadableString());
			}
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

		// TODO Refactor to N methods or define functions for each in
		// Operation/Operand;
		// type unification is not relevant to all operations, e.g. IN;

		Type type = unifyTypes(operand1, operand2);

		switch (op) {
		case ADD:
			switch (type) {
			case INTEGER:
				stack.push(new Operand(Type.INTEGER, operand1.intVal()
						+ operand2.intVal()));
				break;
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
			switch (type) {
			case INTEGER:
				stack.push(new Operand(Type.INTEGER, operand1.intVal()
						- operand2.intVal()));
				break;
			case DOUBLE:
				stack.push(new Operand(Type.DOUBLE, operand1.doubleVal()
						- operand2.doubleVal()));
				break;
			default:
			}
			break;
		case MUL:
			switch (type) {
			case INTEGER:
				stack.push(new Operand(Type.INTEGER, operand1.intVal()
						* operand2.intVal()));
				break;
			case DOUBLE:
				stack.push(new Operand(Type.DOUBLE, operand1.doubleVal()
						* operand2.doubleVal()));
				break;
			default:
			}
			break;
		case DIV:
			switch (type) {
			case INTEGER:
				if (operand2.intVal() != 0) {
					stack.push(new Operand(Type.INTEGER, operand1.intVal()
							/ operand2.intVal()));
				} else {
					throw new VeLaEvalError(String.format(
							"%s/%s: division by zero error", operand1.intVal(),
							operand2.intVal()));
				}
				break;
			case DOUBLE:
				Double result = operand1.doubleVal() / operand2.doubleVal();
				if (!result.isInfinite()) {
					stack.push(new Operand(Type.DOUBLE, result));
				} else {
					throw new VeLaEvalError(String.format(
							"%s/%s: division by zero error",
							operand1.doubleVal(), operand2.doubleVal()));
				}
				break;
			default:
			}
			break;
		case POW:
			switch (type) {
			case INTEGER:
				int result = operand1.intVal();
				for (int i = 2; i <= operand2.intVal(); i++) {
					result *= operand1.intVal();
				}
				stack.push(new Operand(Type.INTEGER, result));
				break;
			case DOUBLE:
				stack.push(new Operand(Type.DOUBLE, Math.pow(
						operand1.doubleVal(), operand2.doubleVal())));
				break;
			default:
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
			case INTEGER:
				stack.push(new Operand(Type.BOOLEAN,
						operand1.intVal() == operand2.intVal()));
				break;
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
			case INTEGER:
				stack.push(new Operand(Type.BOOLEAN,
						operand1.intVal() != operand2.intVal()));
				break;
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
			case INTEGER:
				stack.push(new Operand(Type.BOOLEAN,
						operand1.intVal() > operand2.intVal()));
				break;
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
			case INTEGER:
				stack.push(new Operand(Type.BOOLEAN,
						operand1.intVal() < operand2.intVal()));
				break;
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
			case INTEGER:
				stack.push(new Operand(Type.BOOLEAN,
						operand1.intVal() >= operand2.intVal()));
				break;
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
			case INTEGER:
				stack.push(new Operand(Type.BOOLEAN,
						operand1.intVal() <= operand2.intVal()));
				break;
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
		case APPROXIMATELY_EQUAL:
			Pattern pattern;
			String regex = operand2.stringVal();
			if (!regexPatterns.containsKey(regex)) {
				pattern = Pattern.compile(regex);
				regexPatterns.put(regex, pattern);
			}
			pattern = regexPatterns.get(regex);
			stack.push(new Operand(Type.BOOLEAN, pattern.matcher(
					operand1.stringVal()).matches()));
			break;
		case IN:
			if (operand2.getType() == Type.LIST) {
				// Is a value contained within a list?
				stack.push(new Operand(Type.BOOLEAN, operand2.listVal()
						.contains(operand1)));
			} else if (type == Type.STRING) {
				// Is one string contained within another?
				stack.push(new Operand(Type.BOOLEAN, operand2.stringVal()
						.contains(operand1.stringVal())));
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Unify operand types by converting both operands to strings if only one is
	 * a string or both operands to double if only one is an integer. We change
	 * nothing if either type is composite or Boolean.
	 * 
	 * @param a
	 *            The first operand.
	 * @param b
	 *            The second operand.
	 * @return The final type of the unified operands.
	 */
	private Type unifyTypes(Operand a, Operand b) {
		Type type = a.getType();

		if (!a.getType().isComposite() && !b.getType().isComposite()) {
			if (a.getType() != Type.STRING && b.getType() == Type.STRING) {
				a.convertToString();
				type = Type.STRING;
			} else if (a.getType() == Type.STRING && b.getType() != Type.STRING) {
				b.convertToString();
				type = Type.STRING;
			} else if (a.getType() == Type.INTEGER
					&& b.getType() == Type.DOUBLE) {
				a.setDoubleVal(a.intVal());
				a.setType(Type.DOUBLE);
				type = Type.DOUBLE;
			} else if (a.getType() == Type.DOUBLE
					&& b.getType() == Type.INTEGER) {
				b.setDoubleVal(b.intVal());
				b.setType(Type.DOUBLE);
				type = Type.DOUBLE;
			}
		}

		return type;
	}

	// ** Variable related methods **

	/**
	 * Bind a name to a value in the top-most scope.
	 * 
	 * @param name
	 *            The name to which to bind the value.
	 * @param value
	 *            The value to be bound.
	 */
	public void bind(String name, Operand value) {
		environments.peek().bind(name, value);
	}

	/**
	 * Given a variable name, search for it in the stack of environments, return
	 * an optional Operand instance. The search proceeds from the top to the
	 * bottom of the stack, maintaining the natural stack ordering.
	 * 
	 * @param name
	 *            The name of the variable to look up.
	 * @return The optional operand.
	 */
	private Optional<Operand> lookupBinding(String name) {
		Optional<Operand> result = Optional.empty();

		// Note: could use recursion or a reversed stream iterator instead

		for (int i = environments.size() - 1; i >= 0; i--) {
			result = environments.get(i).lookup(name);
			if (result.isPresent()) {
				break;
			}
		}

		return result;
	}

	/**
	 * Add useful/important bindings
	 */
	private void initBindings() {
		environments.peek().bind("PI", new Operand(Type.DOUBLE, Math.PI));
		environments.peek().bind("E", new Operand(Type.DOUBLE, Math.E));
	}

	// ** Function related methods *

	/**
	 * Given a function name, search for it in the stack of environments, return
	 * an optional list of function executors. The search proceeds from the top
	 * to the bottom of the stack, maintaining the natural stack ordering.
	 * 
	 * @param name
	 *            The name of the variable to look up.
	 * @return The optional function executor list.
	 */
	private Optional<List<FunctionExecutor>> lookupFunctions(String name) {
		Optional<List<FunctionExecutor>> functions = Optional.empty();

		for (int i = environments.size() - 1; i >= 0; i--) {
			VeLaEnvironment<Operand> environment = environments.get(i);
			if (environment instanceof VeLaScope) {
				functions = ((VeLaScope) environment).lookupFunction(name);
				if (functions.isPresent()) {
					break;
				}
			}
		}

		return functions;
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

		String canonicalFuncName = funcName.toUpperCase();

		// Iterate over all variations of each potentially overloaded function,
		// asking whether each conforms.

		Optional<List<FunctionExecutor>> functions = lookupFunctions(canonicalFuncName);

		if (functions.isPresent()) {
			boolean match = false;

			for (FunctionExecutor function : functions.get()) {
				match = applyFunction(function, params);
				if (match)
					break;
			}

			if (!match) {
				throw new VeLaEvalError("Invalid parameters for function: \""
						+ funcName + "\"");
			}
		} else {
			throw new VeLaEvalError("Unknown function: \"" + funcName + "\"");
		}
	}

	/**
	 * Apply the function to the supplied parameter list if it conforms to them,
	 * leaving the result on the stack.
	 * 
	 * @param function
	 *            The function executor to be applied to the supplied
	 *            parameters.
	 * @param params
	 *            The parameter list.
	 * @return Does the function conform to the actual parameters?
	 * @throws VeLaEvalError
	 *             If a function evaluation error occurs.
	 */
	private boolean applyFunction(FunctionExecutor function,
			List<Operand> params) throws VeLaEvalError {

		boolean conforms = function.conforms(params);

		if (conforms) {
			Optional<Operand> result = function.apply(params);
			if (result.isPresent()) {
				stack.push(result.get());
			}
		}

		return conforms;
	}

	/**
	 * Add a function executor to the current scope.
	 * 
	 * @param executor
	 *            The function executor to be added.
	 */
	public void addFunctionExecutor(FunctionExecutor executor) {
		// It's possible that the top-most environment is not a scope, so find
		// the top-most scope and add the function executor to it.
		for (int i = environments.size() - 1; i >= 0; i--) {
			VeLaEnvironment<Operand> environment = environments.get(i);
			if (environment instanceof VeLaScope) {
				VeLaScope scope = (VeLaScope) environment;
				scope.addFunctionExecutor(executor);
			}
		}
	}

	/**
	 * Initialise function executors
	 */
	private void initFunctionExecutors() {

		addZeroArityFunctions();

		// TODO: add map, reduce and foreach, especially once we have
		// user-defined functions
		addListHeadFunction();
		addListTailFunction();
		addListNthFunction();
		addListLengthFunction();
		addListConcatFunction();
		addListAppendFunction(Type.LIST);
		addListAppendFunction(Type.STRING);
		addListAppendFunction(Type.INTEGER);
		addListAppendFunction(Type.DOUBLE);
		addListAppendFunction(Type.BOOLEAN);

		// Functions from reflection over Math and String classes.
		Set<Class<?>> permittedTypes = new HashSet<Class<?>>();
		permittedTypes.add(int.class);
		permittedTypes.add(double.class);
		permittedTypes.add(boolean.class);
		permittedTypes.add(String.class);
		permittedTypes.add(CharSequence.class);

		addFunctionExecutors(Math.class, permittedTypes, Collections.emptySet());

		addFunctionExecutors(String.class, permittedTypes, new HashSet<String>(
				Arrays.asList("JOIN")));
	}

	private void addZeroArityFunctions() {
		addFunctionExecutor(new FunctionExecutor(Optional.of("TODAY"),
				Optional.of(Type.DOUBLE)) {
			@Override
			public Optional<Operand> apply(List<Operand> operands) {
				Calendar cal = Calendar.getInstance();
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH) + 1; // 0..11 -> 1..12
				int day = cal.get(Calendar.DAY_OF_MONTH);
				double jd = AbstractDateUtil.getInstance().calendarToJD(year,
						month, day);
				return Optional.of(new Operand(Type.DOUBLE, jd));
			}
		});
	}

	private void addListHeadFunction() {
		List<Type> paramTypes = new ArrayList<Type>();
		paramTypes.add(Type.LIST);
		// Return type could be any but we use LIST here arbitrarily.
		// TODO: perhaps we need Type.ANY
		addFunctionExecutor(new FunctionExecutor(Optional.of("HEAD"),
				paramTypes, Optional.of(Type.LIST)) {
			@Override
			public Optional<Operand> apply(List<Operand> operands) {
				List<Operand> list = operands.get(0).listVal();
				Operand result;
				if (!list.isEmpty()) {
					result = list.get(0);
				} else {
					result = Operand.EMPTY_LIST;
				}
				return Optional.of(result);
			}
		});
	}

	private void addListTailFunction() {
		List<Type> paramTypes = new ArrayList<Type>();
		paramTypes.add(Type.LIST);
		// Return type will always be a list.
		addFunctionExecutor(new FunctionExecutor(Optional.of("TAIL"),
				paramTypes, Optional.of(Type.LIST)) {
			@Override
			public Optional<Operand> apply(List<Operand> operands) {
				List<Operand> list = operands.get(0).listVal();
				Operand result;
				if (!list.isEmpty()) {
					List<Operand> tail = new ArrayList<Operand>(list);
					tail.remove(0);
					result = new Operand(Type.LIST, tail);
				} else {
					result = Operand.EMPTY_LIST;
				}
				return Optional.of(result);
			}
		});
	}

	private void addListNthFunction() {
		List<Type> paramTypes = new ArrayList<Type>();
		paramTypes.add(Type.LIST);
		paramTypes.add(Type.INTEGER);
		// Return type could be any but we use LIST here arbitrarily.
		addFunctionExecutor(new FunctionExecutor(Optional.of("NTH"),
				paramTypes, Optional.of(Type.LIST)) {
			@Override
			public Optional<Operand> apply(List<Operand> operands) {
				List<Operand> list = operands.get(0).listVal();
				Operand result;
				if (!list.isEmpty()) {
					result = list.get(operands.get(1).intVal());
				} else {
					result = Operand.EMPTY_LIST;
				}
				return Optional.of(result);
			}
		});
	}

	private void addListLengthFunction() {
		List<Type> paramTypes = new ArrayList<Type>();
		paramTypes.add(Type.LIST);
		// Return type will always be integer.
		addFunctionExecutor(new FunctionExecutor(Optional.of("LENGTH"),
				paramTypes, Optional.of(Type.INTEGER)) {
			@Override
			public Optional<Operand> apply(List<Operand> operands) {
				return Optional.of(new Operand(Type.INTEGER, operands.get(0)
						.listVal().size()));
			}
		});
	}

	private void addListConcatFunction() {
		List<Type> paramTypes = new ArrayList<Type>();
		paramTypes.add(Type.LIST);
		paramTypes.add(Type.LIST);
		// Return type will always be LIST here.
		addFunctionExecutor(new FunctionExecutor(Optional.of("CONCAT"),
				paramTypes, Optional.of(Type.LIST)) {
			@Override
			public Optional<Operand> apply(List<Operand> operands) {
				List<Operand> list1 = operands.get(0).listVal();
				List<Operand> list2 = operands.get(1).listVal();
				List<Operand> newList = new ArrayList<Operand>();
				newList.addAll(list1);
				newList.addAll(list2);
				return Optional.of(new Operand(Type.LIST, newList));
			}
		});
	}

	private void addListAppendFunction(Type secondParameterType) {
		List<Type> paramTypes = new ArrayList<Type>();
		paramTypes.add(Type.LIST);
		paramTypes.add(secondParameterType);
		// Return type will always be LIST here.
		addFunctionExecutor(new FunctionExecutor(Optional.of("APPEND"),
				paramTypes, Optional.of(Type.LIST)) {
			@Override
			public Optional<Operand> apply(List<Operand> operands) {
				List<Operand> newList = new ArrayList<Operand>();
				newList.addAll(operands.get(0).listVal());
				newList.add(operands.get(1));
				return Optional.of(new Operand(Type.LIST, newList));
			}
		});
	}

	/**
	 * Given a class, add non zero-arity VeLa type compatible functions to the
	 * functions map.
	 * 
	 * @param clazz
	 *            The class from which to add function executors.
	 * @param permittedTypes
	 *            The set of Java types that are compatible with VeLa.
	 * @param exclusions
	 *            Names of functions to exclude.
	 */
	private void addFunctionExecutors(Class<?> clazz,
			Set<Class<?>> permittedTypes, Set<String> exclusions) {
		Method[] declaredMethods = clazz.getDeclaredMethods();

		for (Method declaredMethod : declaredMethods) {
			String funcName = declaredMethod.getName().toUpperCase();
			Class<?> returnType = declaredMethod.getReturnType();
			List<Class<?>> paramTypes = getParameterTypes(declaredMethod,
					permittedTypes);

			if (!exclusions.contains(funcName)
					&& permittedTypes.contains(returnType)
					&& !paramTypes.isEmpty()) {
				// If the method is non-static, we need to include a parameter
				// type for the object on which the method will be invoked.
				if (!Modifier.isStatic(declaredMethod.getModifiers())) {
					List<Class<?>> newParamTypes = new ArrayList<Class<?>>();
					newParamTypes.add(clazz);
					newParamTypes.addAll(paramTypes);
					paramTypes = newParamTypes;
				}

				List<Type> types = paramTypes.stream()
						.map(t -> Type.java2Vela(t))
						.collect(Collectors.toList());

				FunctionExecutor function = new FunctionExecutor(
						Optional.of(funcName), declaredMethod, types,
						Optional.of(Type.java2Vela(returnType))) {

					@Override
					public Optional<Operand> apply(List<Operand> operands) {
						Method method = getMethod();
						Operand result = null;
						try {
							Object obj = null;
							if (!Modifier.isStatic(method.getModifiers())) {
								obj = operands.get(0).toObject();
								operands.remove(0);
							}

							// Note that this is the first use of Java 8
							// lambdas in VStar!
							result = Operand.object2Operand(getReturnType()
									.get(), method.invoke(obj, // null for
																// static
																// methods
									operands.stream().map(op -> op.toObject())
											.toArray()));

						} catch (InvocationTargetException e) {
							throw new VeLaEvalError(e.getLocalizedMessage());
						} catch (IllegalAccessException e) {
							throw new VeLaEvalError(e.getLocalizedMessage());
						}

						return Optional.of(result);
					}
				};

				addFunctionExecutor(function);

				// System.out.println(function.toString());
			}
		}
	}

	private static List<Class<?>> getParameterTypes(Method method,
			Set<Class<?>> targetTypes) {
		Parameter[] parameters = method.getParameters();
		List<Class<?>> parameterTypes = new ArrayList<Class<?>>();

		for (Parameter parameter : parameters) {
			Class<?> type = parameter.getType();
			if (targetTypes.contains(type)) {
				parameterTypes.add(type);
			}
		}

		return parameterTypes;
	}
}
