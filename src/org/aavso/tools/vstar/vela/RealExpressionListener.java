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

/**
 * VeLa: VStar expression Language interpreter
 * 
 * Real expression listener.
 */
public class RealExpressionListener extends VeLaBaseListener {

	private Stack<Double> stack;

	public RealExpressionListener(Stack<Double> stack) {
		this.stack = stack;
	}

	@Override
	public void exitRealExpression(RealExpressionContext ctx) {
		if (ctx.getChildCount() == 3) {
			String op = ctx.getChild(1).getText();
			double n2 = stack.pop();
			double n1 = stack.pop();
			switch(op.charAt(0)) {
			case '+':
				stack.push(n1+n2);
				break;
			case '-':
				stack.push(n1-n2);
				break;				
			}
		}
	}

	@Override
	public void exitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
		if (ctx.getChildCount() == 3) {
			String op = ctx.getChild(1).getText();
			double n2 = stack.pop();
			double n1 = stack.pop();
			switch(op.charAt(0)) {
			case '*':
				stack.push(n1*n2);
				break;
			case '/':
				stack.push(n1/n2);
				break;				
			}
		}
	}

	@Override
	public void exitReal(RealContext ctx) {
		stack.push(VeLaInterpreter.parseDouble(ctx.getText()));
	}
}
