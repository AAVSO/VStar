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

import java.util.HashMap;
import java.util.Map;

/**
 * VeLa: VStar expression Language
 *
 * Operations abstracted from concrete syntax.
 */
public enum Operation {

	ADD("+", 2), SUB("-", 2), NEG("-", 1), MUL("*", 2), DIV("/", 2), POW("^", 2), AND(
			"and", 2), OR("or", 2), NOT("not", 1), EQUAL("=", 2), NOT_EQUAL(
			"<>", 2), GREATER_THAN(">", 2), LESS_THAN("<", 2), GREATER_THAN_OR_EQUAL(
			">=", 2), LESS_THAN_OR_EQUAL("<=", 2), APPROXIMATELY_EQUAL("=~", 2), IN(
			"in", 2), LIST("list"), PAIR("pair"), SEQUENCE("sequence", true), BIND(
			"bind", true), IS("is", true), SYMBOL("symbol"), FUNDEF("fundef",
			true), FUNCALL("func", true), SELECT("select", true), WHILE(
			"while", true), SENTINEL("sentinel");

	private String symbol;
	private int arity;
	private boolean specialForm;

	// Create a mapping from symbols to binary operation values for
	// fast lookup.
	private static Map<String, Operation> symbolToBinaryOp;

	static {
		symbolToBinaryOp = new HashMap<String, Operation>();
		for (Operation op : values()) {
			if (op.arity == 2) {
				symbolToBinaryOp.put(op.symbol, op);
			}
		}
	}

	public static Operation getBinaryOp(String token) {
		return symbolToBinaryOp.get(token.toUpperCase());
	}

	private Operation(String symbol, int arity, boolean specialForm) {
		this.symbol = symbol.toUpperCase();
		this.arity = arity;
		this.specialForm = specialForm;
	}

	private Operation(String symbol, int arity) {
		this(symbol, arity, false);

	}

	private Operation(String symbol, boolean specialForm) {
		this(symbol, 0, specialForm);
	}

	private Operation(String symbol) {
		this(symbol, 0);
	}

	private Operation() {
		this(null, 0);
	}

	public String token() {
		return symbol;
	}

	public int arity() {
		return arity;
	}

	public boolean isSpecialForm() {
		return specialForm;
	}
}