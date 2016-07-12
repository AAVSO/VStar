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
import org.antlr.v4.runtime.tree.ErrorNode;

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
	public void enterRealExpression(RealExpressionContext ctx) {
		String str = ctx.getText();
		System.out.println(str);
	}

	@Override
	public void enterMultiplicativeExpression(
			MultiplicativeExpressionContext ctx) {
		String str = ctx.getText();
		System.out.println(str);
	}


	@Override
	public void exitRealExpression(RealExpressionContext ctx) {
		String str = ctx.getText();
		// TODO: if 3 children, 2nd child should be +,-
		System.out.println(str);
	}

	@Override
	public void exitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
		String str = ctx.getText();
		System.out.println(str);
	}

	@Override
	public void exitReal(RealContext ctx) {
		String str = ctx.getText();
		stack.push(VeLaInterpreter.parseDouble(str));
	}
	
	@Override
	public void visitErrorNode(ErrorNode node) {
		// TODO Auto-generated method stub
		super.visitErrorNode(node);
	}
}
