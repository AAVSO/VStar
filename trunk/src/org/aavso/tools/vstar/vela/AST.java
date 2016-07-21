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

import java.util.ArrayList;
import java.util.List;

/**
 *  VeLa: VStar expression Language
 *  
 *  Abstract Syntax Tree class
 */
public class AST {

	private String token;
	private List<AST> children;
	
	public AST() {
		token = null;
		children = null;
	}
	
	public AST(String token) {
		this.token = token;
		children = null;
	}

	public AST(String token, AST left, AST right) {
		this.token = token;
		addChild(left);
		addChild(right);
	}

	public String getToken() {
		return token;
	}

	public List<AST> getChildren() {
		return children;
	}

	public void addChild(AST child) {
		if (children == null) {
			children = new ArrayList<AST>();
		}
		
		children.add(child);
	}

	public boolean isLeaf() {
		return children == null;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		if (isLeaf()) {
			buf.append(token);
		} else {
			buf.append("(");
			buf.append(token);
			buf.append(" ");
			for (AST ast : children) {
				buf.append(ast);
				buf.append(" ");
			}
			buf.deleteCharAt(buf.length()-1);
			buf.append(")");
		}
		
		return buf.toString();
	}	
}
