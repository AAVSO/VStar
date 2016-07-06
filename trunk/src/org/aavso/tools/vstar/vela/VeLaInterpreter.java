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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * VeLa: VStar expression Language interpreter
 */
public class VeLaInterpreter {

	public VeLaInterpreter() {
		// TODO: stack
	}

	public double real(String expr) throws IllegalArgumentException {
		
		CharStream stream = new ANTLRInputStream(expr);
		VeLaLexer lexer = new VeLaLexer(stream);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		VeLaParser parser = new VeLaParser(tokens);
		VeLaParser.RealContext tree = parser.real();
		RealListener listener = new RealListener();
		ParseTreeWalker.DEFAULT.walk(listener, tree);
		
		return listener.getN();
	}
}
