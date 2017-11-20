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
	private static Map<String, Operand> exprToResult = new HashMap<String, Operand>();

	// Regular expression pattern cache.
	private static Map<String, Pattern> regexPatterns = new HashMap<String, Pattern>();

	// A map of names to functions
	private Map<String, FunctionExecutor> functions = new HashMap<String, FunctionExecutor>();

	private VeLaErrorListener errorListener;

	static {
		//initFunctionExecutors();
	}

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

	public void setEnvironment(AbstractVeLaEnvironment environment) {
		this.environments.push(environment);
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
	public double expression(String expr) throws VeLaEvalError {

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

	// wrapper for booleanExpr

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
	 * Common VeLa evaluation entry point.
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
				} else if (functions.containsKey(varName)) {
					// Parameterless function call
					applyFunction(varName);
				} else {
					throw new VeLaEvalError("Unknown variable: \""
							+ ast.getToken() + "\"");
				}
			} else if (ast.getOp() == Operation.FUNCALL) {
				// Evaluate actual parameters, if any.
				if (ast.hasChildren()) {
					for (int i = ast.getChildren().size() - 1; i >= 0; i--) {
						eval(ast.getChildren().get(i));
					}
				}

				// Prepare actual parameter list.
				List<Operand> params = new ArrayList<Operand>();
				for (int i = 1; i <= ast.getChildren().size(); i++) {
					params.add(stack.pop());
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
		String canonicalFuncName = funcName.toUpperCase();

		// TODO: this does not account for overloaded functions!
		// Need names to be more like signatures, e.g.
		// LASTINDEXOF :: [STRING, STRING, INTEGER] -> INTEGER
		// LASTINDEXOF :: [STRING, INTEGER] -> INTEGER
		// or a name that maps to a list of executors, each of
		// which must be examined; mapping from string signature to executor may
		// be faster, but then the number of overloaded functions is likely to
		// be few compared with the number of overall functions
		if (functions.containsKey(canonicalFuncName)) {
			FunctionExecutor function = functions.get(canonicalFuncName);
			if (function.conforms(params)) {
				stack.push(function.apply(params));
			} else {
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
		functions.put("TODAY", new FunctionExecutor("TODAY", Type.DOUBLE) {
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
											obj,
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

				functions.put(funcName, function);

				System.out.println(function.toString());
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
