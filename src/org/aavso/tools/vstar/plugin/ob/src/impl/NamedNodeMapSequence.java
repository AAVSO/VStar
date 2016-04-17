/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2016  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.plugin.ob.src.impl;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class exposes a NamedNodeMap as a sequence of nodes.
 */
public class NamedNodeMapSequence implements INodeSequence {

	private NamedNodeMap nodeMap;
	
	public NamedNodeMapSequence(NamedNodeMap nodeList) {
		super();
		this.nodeMap = nodeList;
	}

	public int getLength() {
		return nodeMap.getLength();
	}
	
	public Node item(int n) {
		return nodeMap.item(n);
	}
}
