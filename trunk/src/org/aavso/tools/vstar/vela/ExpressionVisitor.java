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

import org.aavso.tools.vstar.vela.VeLaParser.AdditiveExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.AnonFundefContext;
import org.aavso.tools.vstar.vela.VeLaParser.BindingContext;
import org.aavso.tools.vstar.vela.VeLaParser.BlockContext;
import org.aavso.tools.vstar.vela.VeLaParser.BoolContext;
import org.aavso.tools.vstar.vela.VeLaParser.BooleanExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.ConjunctiveExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.ExponentiationExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.ExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.FactorContext;
import org.aavso.tools.vstar.vela.VeLaParser.FormalParameterContext;
import org.aavso.tools.vstar.vela.VeLaParser.FuncallContext;
import org.aavso.tools.vstar.vela.VeLaParser.FunobjContext;
import org.aavso.tools.vstar.vela.VeLaParser.IntegerContext;
import org.aavso.tools.vstar.vela.VeLaParser.ListContext;
import org.aavso.tools.vstar.vela.VeLaParser.LogicalNegationExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.MultiplicativeExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.NamedFundefContext;
import org.aavso.tools.vstar.vela.VeLaParser.RealContext;
import org.aavso.tools.vstar.vela.VeLaParser.RelationalExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.SelectionExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.SequenceContext;
import org.aavso.tools.vstar.vela.VeLaParser.SignContext;
import org.aavso.tools.vstar.vela.VeLaParser.StringContext;
import org.aavso.tools.vstar.vela.VeLaParser.SymbolContext;
import org.aavso.tools.vstar.vela.VeLaParser.TypeContext;
import org.aavso.tools.vstar.vela.VeLaParser.UnaryExpressionContext;
import org.aavso.tools.vstar.vela.VeLaParser.WhileLoopContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * VeLa: VStar expression Language interpreter
 * 
 * Expression parse tree visitor that generates an Abstract Syntax Tree.
 */
public class ExpressionVisitor extends VeLaBaseVisitor<AST> {

	@Override
	public AST visitSequence(SequenceContext ctx) {
		AST ast = new AST(Operation.SEQUENCE);

		for (int i = 0; i < ctx.getChildCount(); i++) {
			ParseTree tree = ctx.getChild(i);
			ast.addChild(tree.accept(this));
		}

		return ast;
	}

	@Override
	public AST visitBinding(BindingContext ctx) {
		AST symbol = ctx.symbol().accept(this);
		AST value = ctx.expression().accept(this);
		return new AST(Operation.BIND, symbol, value);
	}

	@Override
	public AST visitWhileLoop(WhileLoopContext ctx) {
		return new AST(Operation.WHILE, ctx.booleanExpression().accept(this),
				ctx.block().accept(this));
	}

	@Override
	public AST visitNamedFundef(NamedFundefContext ctx) {
		AST ast = new AST(Operation.FUNDEF);
		ast.addChild(ctx.symbol().accept(this));
		ctx.formalParameter()
				.forEach(param -> ast.addChild(param.accept(this)));
		if (ctx.type() != null) {
			// Optional return type
			ast.addChild(ctx.type().accept(this));
		}
		ast.addChild(ctx.block().accept(this));
		return ast;
	}

	@Override
	public AST visitAnonFundef(AnonFundefContext ctx) {
		AST ast = new AST(Operation.FUNDEF);
		ctx.formalParameter()
				.forEach(param -> ast.addChild(param.accept(this)));
		if (ctx.type() != null) {
			// Optional return type
			ast.addChild(ctx.type().accept(this));
		}
		ast.addChild(ctx.block().accept(this));
		return ast;
	}

	@Override
	public AST visitFormalParameter(FormalParameterContext ctx) {
		AST pName = ctx.symbol().accept(this);
		AST pType = ctx.type().accept(this);
		return new AST(Operation.PAIR, pName, pType);
	}

	@Override
	public AST visitType(TypeContext ctx) {
		String symbol = ctx.getChild(0).getText().toUpperCase();
		return new AST(symbol, Operation.SYMBOL);
	}

	@Override
	public AST visitExpression(ExpressionContext ctx) {
		return ctx.getChild(0).accept(this);
	}

	@Override
	public AST visitSelectionExpression(SelectionExpressionContext ctx) {
		AST ast = new AST(Operation.SELECT);

		// Iterate over the antecedent-consequent pairs.
		for (int i = 0; i < ctx.booleanExpression().size(); i++) {
			AST antecedent = ctx.booleanExpression(i).accept(this);
			AST consequent = ctx.consequent(i).accept(this);
			ast.addChild(new AST(Operation.PAIR, antecedent, consequent));
		}

		return ast;
	}

	@Override
	public AST visitBooleanExpression(BooleanExpressionContext ctx) {
		return dyadicRule(ctx, ctx.conjunctiveExpression(0).accept(this));
	}

	@Override
	public AST visitConjunctiveExpression(ConjunctiveExpressionContext ctx) {
		return dyadicRule(ctx, ctx.logicalNegationExpression(0).accept(this));
	}

	@Override
	public AST visitLogicalNegationExpression(
			LogicalNegationExpressionContext ctx) {
		AST ast = ctx.relationalExpression().accept(this);

		if ("not".equalsIgnoreCase(ctx.getChild(0).getText())) {
			ast = new AST(Operation.NOT, ast);
		}

		return ast;
	}

	@Override
	public AST visitRelationalExpression(RelationalExpressionContext ctx) {
		return dyadicRule(ctx, ctx.additiveExpression(0).accept(this));
	}

	@Override
	public AST visitAdditiveExpression(AdditiveExpressionContext ctx) {
		return dyadicRule(ctx, ctx.multiplicativeExpression(0).accept(this));
	}

	@Override
	public AST visitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
		return dyadicRule(ctx, ctx.unaryExpression(0).accept(this));
	}

	@Override
	public AST visitUnaryExpression(UnaryExpressionContext ctx) {
		AST ast = ctx.exponentiationExpression().accept(this);

		if ("-".equals(ctx.getChild(0).getText())) {
			ast = new AST(Operation.NEG, ast);
		}

		return ast;
	}

	@Override
	public AST visitSign(SignContext ctx) {
		// Nothing to do; could visit this and return AST(Operation.NEG)
		return null;
	}

	@Override
	public AST visitExponentiationExpression(ExponentiationExpressionContext ctx) {
		AST right = null;

		if (ctx.getChildCount() == 1) {
			right = ctx.factor(0).accept(this);
		} else {
			String op = null;
			right = ctx.getChild(ctx.getChildCount() - 1).accept(this);

			for (int i = ctx.getChildCount() - 2; i >= 0; i--) {
				ParseTree child = ctx.getChild(i);

				if (child instanceof TerminalNode) {
					op = child.getText();
				} else {
					AST left = child.accept(this);
					right = new AST(Operation.getBinaryOp(op), left, right);
				}
			}
		}

		return right;
	}

	@Override
	public AST visitFactor(FactorContext ctx) {
		AST ast = null;

		if (ctx.getChild(0).equals(ctx.LPAREN())) {
			ast = ctx.expression().accept(this);
		} else {
			ast = ctx.getChild(0).accept(this);
		}

		return ast;
	}

	@Override
	public AST visitFuncall(FuncallContext ctx) {
		AST funObj = ctx.funobj().accept(this);

		AST ast = null;

		if (funObj.getOp() == Operation.SYMBOL) {
			// Named function call
			ast = new AST(funObj.getToken(), Operation.FUNCALL);
		} else {
			// Anonymous function call
			ast = new AST(null, Operation.FUNCALL);
			ast.addChild(funObj);
		}

		// Add actual parameters
		for (ExpressionContext expr : ctx.expression()) {
			ast.addChild(expr.accept(this));
		}

		return ast;
	}

	@Override
	public AST visitFunobj(FunobjContext ctx) {
		AST ast = null;

		if (ctx.getChild(0).equals(ctx.IDENT())) {
			ast = new AST(ctx.IDENT().getText().toUpperCase(), Operation.SYMBOL);
		} else {
			ast = ctx.anonFundef().accept(this);
		}

		return ast;
	}

	@Override
	public AST visitBlock(BlockContext ctx) {
		return ctx.sequence().accept(this);
	}

	@Override
	public AST visitInteger(IntegerContext ctx) {
		String token = ctx.INTEGER().getText();
		Operand intLiteral = new Operand(Type.INTEGER, Integer.parseInt(token));
		return new AST(token, intLiteral);
	}

	@Override
	public AST visitReal(RealContext ctx) {
		String token = ctx.REAL().getText();
		Operand realLiteral = new Operand(Type.REAL, parseDouble(token));
		return new AST(token, realLiteral);
	}

	@Override
	public AST visitBool(BoolContext ctx) {
		String token = ctx.BOOLEAN().getText().toUpperCase();
		boolean value = "TRUE".equalsIgnoreCase(token) ? true : false;
		Operand booleanLiteral = new Operand(Type.BOOLEAN, value);
		return new AST(token, booleanLiteral);
	}

	@Override
	public AST visitString(StringContext ctx) {
		String token = ctx.STRING().getText().replace("\"", "");
		return new AST(token, new Operand(Type.STRING, token));
	}

	@Override
	public AST visitList(ListContext ctx) {
		AST ast = new AST(Operation.LIST);
		ctx.expression().forEach(expr -> ast.addChild(expr.accept(this)));
		return ast;
	}

	@Override
	public AST visitSymbol(SymbolContext ctx) {
		String name = ctx.getText().toUpperCase();
		return new AST(name, Operation.SYMBOL);
	}

	// Helpers

	/**
	 * A general method to handle dyadic productions.
	 * 
	 * @param ctx
	 *            The rule's context.
	 * @param left
	 *            The initial left AST.
	 * @return The final AST.
	 */
	private AST dyadicRule(RuleContext ctx, AST left) {
		String op = null;

		for (int i = 1; i < ctx.getChildCount(); i++) {
			ParseTree child = ctx.getChild(i);

			if (child instanceof TerminalNode) {
				op = child.getText();
			} else {
				AST right = child.accept(this);
				left = new AST(Operation.getBinaryOp(op), left, right);
			}
		}

		return left;
	}

	// TODO: move into a numeric utils class with a static import

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
