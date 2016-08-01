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

import org.aavso.tools.vstar.vela.VeLaParser.MultiplicativeExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.RealContext;
import org.aavso.tools.vstar.vela.VeLaParser.RealExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.UnaryExpressionContext;

/**
 * VeLa: VStar expression Language interpreter
 * 
 * Real expression parse tree listener.
 */
public class RealExpressionListener extends VeLaBaseListener {

	private Stack<AST> astStack;

	public RealExpressionListener(Stack<Double> stack) {
		astStack = new Stack<AST>();
	}

	public AST getAST() {
		// Peek vs pop to allow multiple non-destructive calls to this method.
		return astStack.peek();
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
					astStack.push(new AST(Operation.ADD, left, right));
					break;
				case '-':
					astStack.push(new AST(Operation.SUB, left, right));
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
					astStack.push(new AST(Operation.MUL, left, right));
					break;
				case '/':
					astStack.push(new AST(Operation.DIV, left, right));
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
	public void exitReal(RealContext ctx) {
		astStack.push(new AST(ctx.getText()));
	}
}
