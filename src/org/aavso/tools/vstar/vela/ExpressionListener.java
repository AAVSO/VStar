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

import java.util.Stack;

import org.aavso.tools.vstar.vela.VeLaParser.BooleanExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.ConjunctiveExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.ExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.FuncContext;
import org.aavso.tools.vstar.vela.VeLaParser.IntegerContext;
import org.aavso.tools.vstar.vela.VeLaParser.ListContext;
import org.aavso.tools.vstar.vela.VeLaParser.LogicalNegationExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.MultiplicativeExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.RealContext;
import org.aavso.tools.vstar.vela.VeLaParser.RelationalExpressionContext;
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

	@Override
	public void exitBooleanExpression(BooleanExpressionContext ctx) {
		for (int i = ctx.getChildCount() - 1; i >= 0; i--) {
			if (ctx.getChild(i) instanceof TerminalNode) {
				String op = ctx.getChild(i).getText();
				AST right = astStack.pop();
				AST left = astStack.pop();
				if (op.equalsIgnoreCase("or")) {
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
				if (op.equalsIgnoreCase("and")) {
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
				if (op.equalsIgnoreCase("not")) {
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
				if (op.contains("=") || op.contains("<") || op.contains(">")) {
					astStack.push(new AST(Operation.getBinaryOp(op), left,
							right));
				}
			}
		}
	}

	@Override
	public void exitExpression(ExpressionContext ctx) {
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
					break;
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
					break;
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
	public void exitFunc(FuncContext ctx) {
		String func = ctx.getChild(0).getText().toUpperCase();
		AST ast = new AST(func, Operation.FUNCTION);
		while (!astStack.isEmpty()) {
			AST child = astStack.pop();
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
		astStack.push(new AST(ctx.getChild(0).getText(), Type.INTEGER));
	}

	@Override
	public void exitReal(RealContext ctx) {
		astStack.push(new AST(ctx.getChild(0).getText(), Type.DOUBLE));
	}

	@Override
	public void exitString(StringContext ctx) {
		astStack.push(new AST(ctx.getText().replace("\"", ""), Type.STRING));
	}

	@Override
	public void exitList(ListContext ctx) {
		AST ast = new AST("list", Operation.LIST);
		while (!astStack.isEmpty()) {
			AST child = astStack.pop();
			ast.addFirstChild(child);
		}
		astStack.push(ast);
	}
}
