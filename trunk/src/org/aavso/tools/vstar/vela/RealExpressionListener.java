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

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import org.aavso.tools.vstar.vela.VeLaParser.MultiplicativeExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.RealContext;
import org.aavso.tools.vstar.vela.VeLaParser.RealExpressionContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * VeLa: VStar expression Language interpreter
 * 
 * Real expression listener.
 */
public class RealExpressionListener extends VeLaBaseListener {

	// TODO: Make this an external enum
	enum Operator {
		ADD, SUB, MUL, DIV
	}

	private Stack<Double> operandStack;
	private Stack<Operator> operatorStack;
	private Stack<AST> astStack;
	private Queue<AST> astQ;

	// Notes:
	// - what works so far is traversing children in reverse order pushing ASTs,
	// popping them in R,L order
	// - consider a Q and normal traversal order

	public RealExpressionListener(Stack<Double> stack) {
		this.operandStack = stack;
		this.operatorStack = new Stack<Operator>();
		astStack = new Stack<AST>();
		astQ = new ArrayDeque<AST>();
	}

	public void interpret() {
		while (!operatorStack.isEmpty()) {
			double n2 = operandStack.pop();
			double n1 = operandStack.pop();

			switch (operatorStack.pop()) {
			case ADD:
				operandStack.push(n1 + n2);
				break;
			case SUB:
				operandStack.push(n1 - n2);
				break;
			case MUL:
				operandStack.push(n1 * n2);
				break;
			case DIV:
				operandStack.push(n1 / n2);
				break;
			}
		}
	}

	public AST getAST() {
		return astStack.pop();
	}

	@Override
	public void exitRealExpression(RealExpressionContext ctx) {
		List<TerminalNode> plusNodes = ctx.PLUS();
		List<TerminalNode> minusNodes = ctx.MINUS();

		// TODO: can parent/child relationship be used to help discern
		// info for use in AST, e.g. 2+3-5*6;
		// we want to stop processing when a node is not a number or +,-;
		// perhaps the key here is just walk order; we need to go to
		// (+ 2 (- 3 (* 5 6))) or 2 3 5 6 * - +
		// => use an operand and operator operandStack and after walk, call
		// interpret method
		// or get AST method
		 for (int i = ctx.getChildCount()-1; i>=0; i--) {
//		for (int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree tree = ctx.getChild(i);
			int ccount = tree.getChildCount();
			String op = ctx.getChild(i).getText();
			if (ccount == 0) {
				System.out.println(op + " " + ccount);

				double n1, n2;
				AST left, right;
				// String op = ctx.getChild(i).getText();
				switch (op.charAt(0)) {
				case '+':
					// n2 = operandStack.pop();
					// n1 = operandStack.pop();
					// operandStack.push(n1 + n2);
					operatorStack.push(Operator.ADD);
					right = astStack.pop();
					left = astStack.pop();
					astStack.push(new AST(op, left, right));
					left = astQ.remove();
					right = astQ.remove();
					astQ.add(new AST(op, left, right));
					break;
				case '-':
					// n2 = operandStack.pop();
					// n1 = operandStack.pop();
					// operandStack.push(n1 - n2);
					right = astStack.pop();
					left = astStack.pop();
					astStack.push(new AST(op, left, right));
					left = astQ.remove();
					right = astQ.remove();
					astQ.add(new AST(op, left, right));
					break;
				}
			}
		}
	}

	@Override
	public void exitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
		List<TerminalNode> multNodes = ctx.MULT();
		List<TerminalNode> divNodes = ctx.DIV();

		int count = ctx.getChildCount();
		 for (int i = ctx.getChildCount()-1; i>=0; i--) {
			// for (int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree tree = ctx.getChild(i);
			int ccount = tree.getChildCount();
			String op = ctx.getChild(i).getText();
			if (ccount == 0) {
				System.out.println(op + " " + ccount);

				double n1, n2;
				AST left, right;
				// String op = ctx.getChild(i).getText();
				switch (op.charAt(0)) {
				case '*':
					// n2 = operandStack.pop();
					// n1 = operandStack.pop();
					// operandStack.push(n1 * n2);
					operatorStack.push(Operator.MUL);
					right = astStack.pop();
					left = astStack.pop();
					astStack.push(new AST(op, left, right));
					left = astQ.remove();
					right = astQ.remove();
					astQ.add(new AST(op, left, right));
					break;
				case '/':
					// n2 = operandStack.pop();
					// n1 = operandStack.pop();
					// operandStack.push(n1 / n2);
					operatorStack.push(Operator.DIV);
					right = astStack.pop();
					left = astStack.pop();
					astStack.push(new AST(op, left, right));
					left = astQ.remove();
					right = astQ.remove();
					astQ.add(new AST(op, left, right));
					break;
				}
			}
		}
	}

	@Override
	public void exitReal(RealContext ctx) {
		int ccount = ctx.getChild(0).getChildCount();
		if (ccount == 0) {
			System.out.println(ctx.getText() + " " + ccount);
		}
		operandStack.push(VeLaInterpreter.parseDouble(ctx.getText()));
		astStack.push(new AST(ctx.getText()));
		astQ.add(new AST(ctx.getText()));		
	}
}
