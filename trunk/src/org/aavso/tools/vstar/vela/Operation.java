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


/**
 * VeLa: VStar expression Language
 *
 * Operations abstracted from concrete syntax.
 */
public enum Operation {

	ADD("+", 2), SUB("-", 2), NEG("-", 1), MUL("*", 2), DIV("/", 2), FUNCTION("func");

	private String symbol;
	private int arity;
	
	private Operation(String symbol, int arity) {
		this.symbol = symbol;
		this.arity = arity;
	}

	private Operation(String symbol) {
		this(symbol, 0);
	}

	private Operation() {
		this("", 0);
	}

	public String token() {
		return symbol;
	}

	public int arity() {
		return arity;
	}
}
