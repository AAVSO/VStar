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
import org.aavso.tools.vstar.vela.VeLaParser.FuncContext;
import org.aavso.tools.vstar.vela.VeLaParser.MultiplicativeExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.RealContext;
import org.aavso.tools.vstar.vela.VeLaParser.RealExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.StringContext;
import org.aavso.tools.vstar.vela.VeLaParser.StringExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.UnaryExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.VarContext;

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
			String op = ctx.getChild(i).getText();
			if (ctx.getChild(i).getChildCount() == 0) {
				AST right = astStack.pop();
				AST left = astStack.pop();
				if (op.contains("=") || op.contains("<") || op.contains(">")) {
					astStack.push(new AST(Operation.getBinaryOp(op), left, right));
				}
			}
		}
	}

	@Override
	public void exitRealExpression(RealExpressionContext ctx) {
		for (int i = ctx.getChildCount() - 1; i >= 0; i--) {
			String op = ctx.getChild(i).getText();
			if (ctx.getChild(i).getChildCount() == 0) {
				AST right = astStack.pop();
				AST left = astStack.pop();
				switch (op.charAt(0)) {
				case '+':
				case '-':
					astStack.push(new AST(Operation.getBinaryOp(op), left, right));
					break;
				}
			}
		}
	}

	@Override
	public void exitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
		for (int i = ctx.getChildCount() - 1; i >= 0; i--) {
			String op = ctx.getChild(i).getText();
			if (ctx.getChild(i).getChildCount() == 0) {
				AST right = astStack.pop();
				AST left = astStack.pop();
				switch (op.charAt(0)) {
				case '*':
				case '/':
					astStack.push(new AST(Operation.getBinaryOp(op), left, right));
					break;
				}
			}
		}
	}

	@Override
	public void exitUnaryExpression(UnaryExpressionContext ctx) {
		for (int i = ctx.getChildCount() - 1; i >= 0; i--) {
			String op = ctx.getChild(i).getText();
			if (ctx.getChild(i).getChildCount() == 1) {
				if (op.charAt(0) == '-') {
					AST child = astStack.pop();
					astStack.push(new AST(Operation.NEG, child));
				}
			}
		}
	}

	@Override
	public void exitStringExpression(StringExpressionContext ctx) {
		for (int i = ctx.getChildCount() - 1; i >= 0; i--) {
			String op = ctx.getChild(i).getText();
			if (ctx.getChild(i).getChildCount() == 0) {
				AST right = astStack.pop();
				AST left = astStack.pop();
				switch (op.charAt(0)) {
				case '+':
					astStack.push(new AST(Operation.getBinaryOp(op), left, right));
					break;
				}
			}
		}
	}

	@Override
	public void exitFunc(FuncContext ctx) {
		String func = ctx.getChild(0).getText();
		AST ast = new AST(func, Operation.FUNCTION);
		astStack.push(ast);
	}

	@Override
	public void exitVar(VarContext ctx) {
		String func = ctx.getChild(0).getText();
		AST ast = new AST(func, Operation.VARIABLE);
		astStack.push(ast);
	}

	@Override
	public void exitReal(RealContext ctx) {
		astStack.push(new AST(ctx.getText(), Type.DOUBLE));
	}

	@Override
	public void exitString(StringContext ctx) {
		astStack.push(new AST(ctx.getText().replace("\"", ""), Type.STRING));
	}
}
