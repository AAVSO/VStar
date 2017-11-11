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

import java.util.LinkedList;
import java.util.List;

/**
 * VeLa: VStar expression Language
 * 
 * Abstract Syntax Tree class
 */
public class AST {

	private String token;
	private Operand literal;
	private Operation op;
	private LinkedList<AST> children;

	public AST() {
		// TODO: use Optional.empty()
		token = null;
		literal = null;
		op = null;
		children = null;
	}

	public AST(String token, Operand literal) {
		this.token = token;
		this.literal = literal;
		op = null;
		children = null;
	}

	public AST(Operation op, AST child) {
		token = op.token();
		literal = null;
		this.op = op;
		addChild(child);
	}

	public AST(String token, Operation op) {
		this.token = token;
		literal = null;
		this.op = op;
	}

	public AST(Operation op, AST left, AST right) {
		this(op.token(), op, left, right);
	}

	public AST(String token, Operation op, AST left, AST right) {
		this.token = token;
		this.op = op;
		literal = null;
		addChild(left);
		addChild(right);
	}

	public Operation getOp() {
		return op;
	}

	public String getToken() {
		return token;
	}

	public Operand getOperand() {
		return literal;
	}

	public void addChild(AST child) {
		if (children == null) {
			children = new LinkedList<AST>();
		}

		children.add(child);
	}

	public void addFirstChild(AST child) {
		if (children == null) {
			children = new LinkedList<AST>();
		}

		children.addFirst(child);
	}

	public boolean isLiteral() {
		return literal != null;
	}
	
	public Type getLiteralType() {
		return literal.getType();
	}

	public boolean hasChildren() {
		return children != null;
	}

	public List<AST> getChildren() {
		return children;
	}

	public AST child() {
		assert !isLeaf();
		assert children.size() == 1;
		return children.get(0);
	}

	public AST left() {
		assert !isLeaf();
		return children.get(0);
	}

	public AST right() {
		assert !isLeaf();
		return children.get(1);
	}

	public boolean isLeaf() {
		return children == null;
	}

	/**
	 * <p>
	 * Will the evaluation of this AST and that of its children yield a
	 * deterministic result?
	 * </p>
	 * 
	 * <p>
	 * If the operation a is function or variable then the answer must be no,
	 * either because a parameterless function itself is not deterministic or
	 * because the result will vary according to input or because a variable's
	 * value can change over time.
	 * </p>
	 * 
	 * @return True if so, otherwise False.
	 */
	public boolean isDeterministic() {
		boolean deterministic = op != Operation.FUNCTION
				&& op != Operation.VARIABLE;

		if (deterministic && !isLeaf()) {
			for (AST child : children) {
				if (!child.isDeterministic()) {
					deterministic = false;
					break;
				}
			}
		}

		return deterministic;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();

		if (isLeaf()) {
			// TODO: list should be an operand
			if (op == Operation.LIST && !hasChildren()) {
				buf.append("empty ");
			} else if (op == Operation.VARIABLE){
				buf.append(token);
			} else {
				if (literal.getType() == Type.STRING) {
					buf.append("\"");
				}
				buf.append(token);
				if (literal.getType() == Type.STRING) {
					buf.append("\"");
				}
			}
		} else {
			buf.append("(");
			buf.append(token);
			buf.append(" ");
			for (AST ast : children) {
				buf.append(ast);
				buf.append(" ");
			}
			buf.deleteCharAt(buf.length() - 1);
			buf.append(")");
		}

		return buf.toString();
	}

	public String toDOT() {
		StringBuffer buf = new StringBuffer();

		buf.append("digraph VeLa AST {\n");
		// TODO
		buf.append("}");

		return buf.toString();
	}

	public AST fromSEXPR(String sexpr) {
		AST ast = null;
		// TODO
		return ast;
	}
}
