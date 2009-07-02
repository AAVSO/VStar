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

import java.util.List;

import javax.swing.JTabbedPane;

/**
 * This class represents the main frame's tabs.
 * @deprecated
 */
public class Tabs extends JTabbedPane {

	/**
	 * Constructor
	 * 
	 * @param namedComponents A list of components to be added to the tabbed pane.
	 */
	public Tabs(List<NamedComponent> namedComponents) {
		super();

		if (namedComponents != null) {
			for (NamedComponent namedComponent : namedComponents) {
				this.addTab(namedComponent.getName(), null, namedComponent
						.getComponent(), namedComponent.getTip());
			}
		}

		// We want scrolling tabs.
		this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}
	
	/**
	 * Constructor
	 */
	public Tabs() {
		this(null);
	}
}
