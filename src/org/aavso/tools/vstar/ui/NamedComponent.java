/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.ui;

import java.awt.Component;

/**
 * A component with an associated name and optional tool-tip.
 */
public class NamedComponent {
	
	private String name;
	private Component component;
	private String tip;
	
	/**
	 * Constructor
	 */
	public NamedComponent(String name, Component component) {
		this.name = name;
		this.component = component;
		this.tip = "";
	}

	/**
	 * Constructor
	 */
	public NamedComponent(String name, Component component, String tip) {
		this.name = name;
		this.component = component;
		this.tip = tip;
	}
	
	public String getName() {
		return name;
	}

	public Component getComponent() {
		return component;
	}

	public String getTip() {
		return tip;
	}
}