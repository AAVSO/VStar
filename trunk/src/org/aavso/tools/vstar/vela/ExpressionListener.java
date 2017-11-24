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

import org.aavso.tools.vstar.vela.VeLaParser.AdditiveExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.BoolContext;
import org.aavso.tools.vstar.vela.VeLaParser.BooleanExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.ConjunctiveExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.ExponentiationExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.FuncallContext;
import org.aavso.tools.vstar.vela.VeLaParser.IntegerContext;
import org.aavso.tools.vstar.vela.VeLaParser.ListContext;
import org.aavso.tools.vstar.vela.VeLaParser.LogicalNegationExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.MultiplicativeExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.RealContext;
import org.aavso.tools.vstar.vela.VeLaParser.RelationalExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.SelectionExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.StringContext;
import org.aavso.tools.vstar.vela.VeLaParser.UnaryExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.VarContext;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * VeLa: VStar expression Language interpreter
 * 
 * Expression parse tree listener.
 */
public class ExpressionListener extends VeLaBaseListener {

	private Stack<AST> astStack;

	public ExpressionListener() {
		astStack = new Stack<AST>();
	}

	public AST getAST() {
		// Peek vs pop to allow multiple non-destructive calls to this method.
		return astStack.peek();
	}

	// TODO:
	// - change the body of each class of exit methods to take a lambda
	// expression for the if-statement; what generic signature?

	@Override
	public void enterSelectionExpression(SelectionExpressionContext ctx) {
		// Mark the position on the stack where selection expressions stop.
		astStack.push(new AST(Operation.SENTINEL));
	}

	@Override
	public void exitSelectionExpression(SelectionExpressionContext ctx) {
		for (int i = ctx.getChildCount() - 1; i >= 0; i--) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				String op = ctx.getChild(i).getText();
				if ("select".equalsIgnoreCase(op)) {
					AST ast = new AST(Operation.SELECTION);
					while (!astStack.isEmpty()) {
						AST consequent = astStack.pop();
						if (consequent.getOp() == Operation.SENTINEL)
							break;
						AST antecedent = astStack.pop();
						AST pair = new AST(Operation.PAIR, antecedent,
								consequent);
						ast.addFirstChild(pair);
					}
					astStack.push(ast);
				}
			}
		}
	}

	@Override
	public void exitBooleanExpression(BooleanExpressionContext ctx) {
		for (int i = ctx.getChildCount() - 1; i >= 0; i--) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				String op = ctx.getChild(i).getText();
				AST right = astStack.pop();
				AST left = astStack.pop();
				if ("or".equalsIgnoreCase(op)) {
					astStack.push(new AST(Operation.OR, left, right));
				}
			}
		}
	}

	@Override
	public void exitConjunctiveExpression(ConjunctiveExpressionContext ctx) {
		for (int i = ctx.getChildCount() - 1; i >= 0; i--) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				String op = ctx.getChild(i).getText();
				AST right = astStack.pop();
				AST left = astStack.pop();
				if ("and".equalsIgnoreCase(op)) {
					astStack.push(new AST(Operation.AND, left, right));
				}
			}
		}
	}

	@Override
	public void exitLogicalNegationExpression(
			LogicalNegationExpressionContext ctx) {
		for (int i = ctx.getChildCount() - 1; i >= 0; i--) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				String op = ctx.getChild(i).getText();
				if ("not".equalsIgnoreCase(op)) {
					AST child = astStack.pop();
					astStack.push(new AST(Operation.NOT, child));
				}
			}
		}
	}

	@Override
	public void exitRelationalExpression(RelationalExpressionContext ctx) {
		for (int i = ctx.getChildCount() - 1; i >= 0; i--) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				String op = ctx.getChild(i).getText();
				AST right = astStack.pop();
				AST left = astStack.pop();
				if (op.contains("=") || op.contains("<") || op.contains(">")
						|| "in".equalsIgnoreCase(op)) {
					astStack.push(new AST(Operation.getBinaryOp(op), left,
							right));
				}
			}
		}
	}

	@Override
	public void exitAdditiveExpression(AdditiveExpressionContext ctx) {
		for (int i = ctx.getChildCount() - 1; i >= 0; i--) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				String op = ctx.getChild(i).getText();
				AST right = astStack.pop();
				AST left = astStack.pop();
				switch (op.charAt(0)) {
				case '+':
				case '-':
					astStack.push(new AST(Operation.getBinaryOp(op), left,
							right));
				}
			}
		}
	}

	@Override
	public void exitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
		for (int i = ctx.getChildCount() - 1; i >= 0; i--) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				String op = ctx.getChild(i).getText();
				AST right = astStack.pop();
				AST left = astStack.pop();
				switch (op.charAt(0)) {
				case '*':
				case '/':
					astStack.push(new AST(Operation.getBinaryOp(op), left,
							right));
				}
			}
		}
	}

	@Override
	public void exitUnaryExpression(UnaryExpressionContext ctx) {
		for (int i = ctx.getChildCount() - 1; i >= 0; i--) {
			if (ctx.getChild(i).getChildCount() == 1) {
				String op = ctx.getChild(i).getText();
				if (op.charAt(0) == '-') {
					AST child = astStack.pop();
					astStack.push(new AST(Operation.NEG, child));
				}
			}
		}
	}

	@Override
	public void exitExponentiationExpression(ExponentiationExpressionContext ctx) {
		for (int i = ctx.getChildCount() - 1; i >= 0; i--) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				String op = ctx.getChild(i).getText();
				AST right = astStack.pop();
				AST left = astStack.pop();
				if (op.charAt(0) == '^') {
					AST ast = new AST(Operation.getBinaryOp(op), left, right);
					astStack.push(ast);
				}
			}
		}
	}

	@Override
	public void enterFuncall(FuncallContext ctx) {
		// Mark the position on the stack where parameters stop.
		astStack.push(new AST(Operation.SENTINEL));
	}

	@Override
	public void exitFuncall(FuncallContext ctx) {
		String func = ctx.getChild(0).getText().toUpperCase();
		AST ast = new AST(func, Operation.FUNCALL);
		while (!astStack.isEmpty()) {
			AST child = astStack.pop();
			if (child.getOp() == Operation.SENTINEL)
				break;
			ast.addFirstChild(child);
		}
		astStack.push(ast);
	}

	@Override
	public void exitVar(VarContext ctx) {
		String var = ctx.getChild(0).getText().toUpperCase();
		astStack.push(new AST(var, Operation.VARIABLE));
	}

	@Override
	public void exitInteger(IntegerContext ctx) {
		String token = ctx.getChild(0).getText();
		Operand intLiteral = new Operand(Type.INTEGER, Integer.parseInt(token));
		astStack.push(new AST(token, intLiteral));
	}

	@Override
	public void exitReal(RealContext ctx) {
		String token = ctx.getChild(0).getText();
		Operand realLiteral = new Operand(Type.DOUBLE, parseDouble(token));
		astStack.push(new AST(token, realLiteral));
	}

	@Override
	public void exitBool(BoolContext ctx) {
		String token = ctx.getChild(0).getText().toUpperCase();
		boolean value = "T".equals(token) ? true : false;
		Operand booleanLiteral = new Operand(Type.BOOLEAN, value);
		astStack.push(new AST(token, booleanLiteral));
	}

	@Override
	public void exitString(StringContext ctx) {
		String token = ctx.getText().replace("\"", "");
		astStack.push(new AST(token, new Operand(Type.STRING, token)));
	}

	@Override
	public void enterList(ListContext ctx) {
		astStack.push(new AST(Operation.SENTINEL));
	}

	public void exitList(ListContext ctx) {
		AST ast = new AST(Operation.LIST);
		while (!astStack.isEmpty()) {
			AST child = astStack.pop();
			if (child.getOp() == Operation.SENTINEL)
				break;
			ast.addFirstChild(child);
		}
		astStack.push(ast);
	}

	// Helpers

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
