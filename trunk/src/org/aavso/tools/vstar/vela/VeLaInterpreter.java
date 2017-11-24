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
	private Stack<AbstractVeLaEnvironment> environments;

	// AST and result caches.
	private static Map<String, AST> exprToAST = new HashMap<String, AST>();

	// TODO: make use of AST => Operand caching at each level of eval()! AST
	// needs to be key not VeLa string
	private static Map<String, Operand> exprToResult = new HashMap<String, Operand>();

	// Regular expression pattern cache.
	private static Map<String, Pattern> regexPatterns = new HashMap<String, Pattern>();

	// A multi-map of names to potentially overloaded functions.
	private Map<String, List<FunctionExecutor>> functions = new HashMap<String, List<FunctionExecutor>>();

	private VeLaErrorListener errorListener;

	/**
	 * Construct a VeLa interpreter with an environments and a verbosity flag.
	 */
	public VeLaInterpreter(AbstractVeLaEnvironment environment, boolean verbose) {
		errorListener = new VeLaErrorListener();

		stack = new Stack<Operand>();
		environments = new Stack<AbstractVeLaEnvironment>();

		initFunctionExecutors();

		if (environment != null) {
			this.environments.push(environment);
		} else {
			this.environments.push(new EmptyVeLaEnvironment());
		}

		this.verbose = verbose;
	}

	/**
	 * Construct a VeLa interpreter with an environments.
	 */
	public VeLaInterpreter(AbstractVeLaEnvironment environment) {
		this(environment, false);
	}

	/**
	 * Construct a VeLa interpreter without an environments.
	 */
	public VeLaInterpreter(boolean verbose) {
		this(null, verbose);
	}

	public VeLaInterpreter() {
		this(null, false);
	}

	/**
	 * Push an environment onto the stack.
	 * @param environment The environment to be pushed.
	 */
	public void pushEnvironment(AbstractVeLaEnvironment environment) {
		this.environments.push(environment);
	}

	/**
	 * Pop the top-most environment from the stack and return it.
	 * @return The top-most environment.
	 */
	public AbstractVeLaEnvironment popEnvironment() {
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

		VeLaParser.ProgramContext tree = getParser(prog).program();
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
			return result.get().doubleVal();
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

			ast = listener.getAST();
			exprToAST.put(prog, ast);
		}

		if (verbose) {
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

		AST ast = commonParseTreeWalker(prog, tree);

		Optional<Operand> result;

		// TODO: we should map from AST to Operand not String to Operand;
		// then we really can cache within eval() at every level, not
		// not just at the top level! Need to add equals() and hashCode() to AST

		if (ast.isDeterministic() && exprToResult.containsKey(prog)) {
			// For deterministic expressions, we can also use cached results.
			// Note: a better description may be constant rather than
			// deterministic.
			result = Optional.of(exprToResult.get(prog));
			if (verbose) {
				System.out.println(String.format("%s [result cached: %s]", ast,
						result));
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
	 * 
	 * @param ast
	 *            The abstract syntax tree.
	 * @throws VeLaEvalError
	 *             If an evaluation error occurs.
	 */
	private void eval(AST ast) throws VeLaEvalError {
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
			} else if (ast.getOp() == Operation.VARIABLE) {
				// Look up variable in the environment stack, pushing it onto
				// the operand stack if it exists, looking for and evaluating a
				// function if not, throwing an exception otherwise.
				String varName = ast.getToken().toUpperCase();
				Optional<Operand> result = lookup(varName);
				if (result.isPresent()) {
					stack.push(result.get());
				} else {
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
			} else if (ast.getOp() == Operation.SELECTION) {
				// Evaluate each antecedent in turn, pushing the value
				// of the first consequent whose antecedent is true and stop
				// antecedent evaluation.
				for (AST pair : ast.getChildren()) {
					eval(pair.left());
					if (stack.peek().booleanVal()) {
						eval(pair.right());
						break;
					}
				}
			} else if (ast.getOp() == Operation.FUNCALL) {
				// Evaluate actual parameters, if any.
				List<Operand> params = new ArrayList<Operand>();

				if (ast.hasChildren()) {
					for (int i = ast.getChildren().size() - 1; i >= 0; i--) {
						eval(ast.getChildren().get(i));
					}

					// Prepare actual parameter list.
					for (int i = 1; i <= ast.getChildren().size(); i++) {
						params.add(stack.pop());
					}
				}

				// Apply function to actual parameters.
				applyFunction(ast.getToken(), params);
			}
		}
	}

	/**
	 * Given a variable name, search for it in the stack of environments, return
	 * a pair consisting of a Boolean value indicating whether the symbol was
	 * found, and the value bound to the symbol as an Operand instance. If the
	 * symbol was not found, this second value in the pair will be null. The
	 * search proceeds from the top to the bottom of the stack, maintaining the
	 * natural stack ordering.
	 * 
	 * @param name
	 *            The name of the variable to look up.
	 * @return The boolean/operand pair.
	 */
	private Optional<Operand> lookup(String name) {
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

		// TODO Refactor to N methods; type unification is not relevant to all
		// operations, e.g. IN; define functions for each in Operation

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
			if (operand2.getType().isComposite()) {
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
		// TODO: user defined functions should be added to/retreieved from the
		// functions multi-map rather than the environment, but does this imply
		// the need to add this multi-map to the environment on top of the stack
		// both due to the need for scoping of all let bindings and in order to
		// be able to capture a function within a closure along with all other
		// bindings?
		// As an aside, a closure need not capture the zeroth environment since
		// this is effectively the global environment
		String canonicalFuncName = funcName.toUpperCase();

		// Iterate over all variations of each potentially overloaded function,
		// asking whether each conforms.

		if (functions.containsKey(canonicalFuncName)) {
			boolean match = false;

			for (FunctionExecutor function : functions.get(canonicalFuncName)) {
				if (function.conforms(params)) {
					stack.push(function.apply(params));
					match = true;
					break;
				}
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
	 * Apply the parameterless function leaving the result on the stack.
	 * 
	 * @param funcName
	 *            The name of the function.
	 * @throws VeLaEvalError
	 *             If a function evaluation error occurs.
	 */
	private void applyFunction(String funcName) throws VeLaEvalError {
		applyFunction(funcName, FunctionExecutor.NO_ACTUALS);
	}

	/**
	 * Initialise function executors
	 */
	private void initFunctionExecutors() {

		addZeroArityFunctions();

		// TODO: map, reduce, foreach once we have functions
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
		addFunctionExecutor(new FunctionExecutor("TODAY", Type.DOUBLE) {
			@Override
			public Operand apply(List<Operand> operands) {
				Calendar cal = Calendar.getInstance();
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH) + 1; // 0..11 -> 1..12
				int day = cal.get(Calendar.DAY_OF_MONTH);
				double jd = AbstractDateUtil.getInstance().calendarToJD(year,
						month, day);
				return new Operand(Type.DOUBLE, jd);
			}
		});
	}

	private void addListHeadFunction() {
		List<Type> paramTypes = new ArrayList<Type>();
		paramTypes.add(Type.LIST);
		// Return type could be any but we use LIST here arbitrarily.
		// TODO: perhaps we need Type.ANY
		addFunctionExecutor(new FunctionExecutor("HEAD", paramTypes, Type.LIST) {
			@Override
			public Operand apply(List<Operand> operands) {
				List<Operand> list = operands.get(0).listVal();
				Operand result;
				if (!list.isEmpty()) {
					result = list.get(0);
				} else {
					result = Operand.EMPTY_LIST;
				}
				return result;
			}
		});
	}

	private void addListTailFunction() {
		List<Type> paramTypes = new ArrayList<Type>();
		paramTypes.add(Type.LIST);
		// Return type will always be a list.
		addFunctionExecutor(new FunctionExecutor("TAIL", paramTypes, Type.LIST) {
			@Override
			public Operand apply(List<Operand> operands) {
				List<Operand> list = operands.get(0).listVal();
				Operand result;
				if (!list.isEmpty()) {
					List<Operand> tail = new ArrayList<Operand>(list);
					tail.remove(0);
					result = new Operand(Type.LIST, tail);
				} else {
					result = Operand.EMPTY_LIST;
				}
				return result;
			}
		});
	}

	private void addListNthFunction() {
		List<Type> paramTypes = new ArrayList<Type>();
		paramTypes.add(Type.LIST);
		paramTypes.add(Type.INTEGER);
		// Return type could be any but we use LIST here arbitrarily.
		addFunctionExecutor(new FunctionExecutor("NTH", paramTypes, Type.LIST) {
			@Override
			public Operand apply(List<Operand> operands) {
				List<Operand> list = operands.get(0).listVal();
				Operand result;
				if (!list.isEmpty()) {
					result = list.get(operands.get(1).intVal());
				} else {
					result = Operand.EMPTY_LIST;
				}
				return result;
			}
		});
	}

	private void addListLengthFunction() {
		List<Type> paramTypes = new ArrayList<Type>();
		paramTypes.add(Type.LIST);
		// Return type will always be integer.
		addFunctionExecutor(new FunctionExecutor("LENGTH", paramTypes,
				Type.INTEGER) {
			@Override
			public Operand apply(List<Operand> operands) {
				return new Operand(Type.INTEGER, operands.get(0).listVal()
						.size());
			}
		});
	}

	private void addListConcatFunction() {
		List<Type> paramTypes = new ArrayList<Type>();
		paramTypes.add(Type.LIST);
		paramTypes.add(Type.LIST);
		// Return type will always be LIST here.
		addFunctionExecutor(new FunctionExecutor("CONCAT", paramTypes,
				Type.LIST) {
			@Override
			public Operand apply(List<Operand> operands) {
				List<Operand> list1 = operands.get(0).listVal();
				List<Operand> list2 = operands.get(1).listVal();
				List<Operand> newList = new ArrayList<Operand>();
				newList.addAll(list1);
				newList.addAll(list2);
				return new Operand(Type.LIST, newList);
			}
		});
	}

	private void addListAppendFunction(Type secondParameterType) {
		List<Type> paramTypes = new ArrayList<Type>();
		paramTypes.add(Type.LIST);
		paramTypes.add(secondParameterType);
		// Return type will always be LIST here.
		addFunctionExecutor(new FunctionExecutor("APPEND", paramTypes,
				Type.LIST) {
			@Override
			public Operand apply(List<Operand> operands) {
				List<Operand> newList = new ArrayList<Operand>();
				newList.addAll(operands.get(0).listVal());
				newList.add(operands.get(1));
				return new Operand(Type.LIST, newList);
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

				FunctionExecutor function = new FunctionExecutor(funcName,
						declaredMethod, types, Type.java2Vela(returnType)) {

					@Override
					public Operand apply(List<Operand> operands) {
						Method method = getMethod();
						Operand result = null;
						try {
							// Note that this is the first use of Java 8
							// lambdas in VStar!
							Object obj = null;
							if (!Modifier.isStatic(method.getModifiers())) {
								obj = operands.get(0).toObject();
								operands.remove(0);
							}

							result = Operand.object2Operand(
									getReturnType(),
									method.invoke(
											obj, // null for static methods
											operands.stream()
													.map(op -> op.toObject())
													.toArray()));

						} catch (InvocationTargetException e) {
							throw new VeLaEvalError(e.getLocalizedMessage());
						} catch (IllegalAccessException e) {
							throw new VeLaEvalError(e.getLocalizedMessage());
						}

						return result;
					}
				};

				addFunctionExecutor(function);

				// System.out.println(function.toString());
			}
		}
	}

	/**
	 * Add a function executor to the multi-map.
	 * 
	 * @param executor
	 *            The function executor to be added.
	 */
	private void addFunctionExecutor(FunctionExecutor executor) {
		List<FunctionExecutor> executors = functions
				.get(executor.getFuncName());

		if (executors == null) {
			executors = new ArrayList<FunctionExecutor>();
			functions.put(executor.getFuncName(), executors);
		}

		executors.add(executor);
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
