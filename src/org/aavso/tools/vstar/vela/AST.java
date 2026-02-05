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

import org.aavso.tools.vstar.util.Pair;

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

	private static int nodeIndex = 0;

	public AST() {
		token = null;
		literal = null;
		op = null;
		children = null;
	}

	public AST(Operation op) {
		this.token = op.token();
		this.literal = null;
		this.op = op;
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

	// TODO: why in this order?
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

	public void head(AST ast) {
		addChild(ast);
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

	public boolean hasChildren() {
		return children != null;
	}

	public List<AST> getChildren() {
		return children;
	}

	public AST head() {
		assert !isLeaf();
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

	public AST lastChild() {
		assert !isLeaf();
		return children.getLast();
	}

	public boolean isLeaf() {
		return children == null;
	}

	public boolean isLiteral() {
		return literal != null;
	}

	public Type getLiteralType() {
		return literal.getType();
	}

	/**
	 * <p>
	 * Will the evaluation of this AST and that of its children yield a
	 * deterministic result?
	 * </p>
	 * 
	 * <p>
	 * If the operation is a function call then the answer must be no, either
	 * because a function may not not deterministic e.g. in the presence of
	 * random numbers or because the result will vary according to input since
	 * parameters may be bound to different values on each call. If the
	 * operation corresponds to retrieving a symbol's value, then the answer
	 * must also be no since a symbol may be bound to different values in
	 * different environments (e.g. functions, filters). The result returned
	 * from a selection varies according to its antecedent condition.
	 * </p>
	 * 
	 * @return True if so, otherwise False.
	 * See Expression visitor optimisedDyadicOpAST() and
	 *             optimisedMonadicOpAST()
	 */
	@Deprecated
	public boolean isDeterministic() {
		boolean deterministic = op != Operation.FUNCALL
				&& op != Operation.SYMBOL && op != Operation.WHEN;

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

	/**
	 * Return an SEXPR representation of the AST.
	 */
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();

		if (literal != null) {
			buf.append(literal);
		} else if (isLeaf()) {
			// e.g. variable, function definition/call
			buf.append(token);
		} else {
			buf.append("(");
			if (token != null) {
				buf.append(token);
			} else if (!isLeaf()) {
				buf.append(children.get(0));
			}
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

	/**
	 * Return the "..." of a DOT "digraph { ... }" representation of the
	 * AST."</br>
	 * 
	 * @return A pair containing the top node name of the DOT representation of
	 *         the AST and the DOT representation itself.
	 */
	public Pair<String, String> toDOT() {
		String node = null;
		StringBuffer buf = new StringBuffer();

		if (literal != null) {
			node = dotNode(buf, literal.toString(), false);
		} else if (isLeaf()) {
			// e.g. variable, function definition/call
			node = dotNode(buf, token, false);
		} else {
			if (token != null) {
				node = dotNode(buf, token, true);
			} else if (!isLeaf()) {
				node = dotNode(buf, children.get(0).toString(), true);
			}
			for (AST ast : children) {
				Pair<String, String> pair = ast.toDOT();
				String dot = pair.second;
				String childNode = pair.first;
				buf.append(dot);
				buf.append("  ");
				buf.append(node);
				buf.append(" -> ");
				buf.append(childNode);
				buf.append(";\n");
			}
		}

		return new Pair<String, String>(node, buf.toString());
	}
	
	/**
	 * Return a DOT "digraph { ... }" representation of the AST."</br>
	 * 
	 * @return A string containing the DOT AST.
	 */
	public String toFullDOT() {
		StringBuffer dot = new StringBuffer();
		
		dot.append("digraph VeLaAST {\n");
		dot.append(toDOT().second);
		dot.append("}");

		return dot.toString();
	}

	public static AST fromSEXPR(String sexpr) {
		AST ast = null;
		// TODO
		return ast;
	}

	// Helpers

	private String dotNode(StringBuffer buf, String label, boolean root) {
		++nodeIndex;
		String node = "node" + nodeIndex;

		buf.append("  ");
		buf.append(node);
		buf.append("[label=\"");
		buf.append(label.replace("\"", "\\\""));
		buf.append("\"");
		if (root) {
			buf.append(", shape=\"rectangle\"");
		}
		buf.append("];\n");

		return node;
	}
}
