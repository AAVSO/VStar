/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010-2014  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.external.plugin;

import java.util.List;

import javax.swing.JOptionPane;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.CustomFilterPluginBase;

/**
* This VStar plugin can be used to create a filter for a group
* of observers. 
*
* @author Sara J. Beck (with lots of help from D. Benn!)
* @version 1.0 - 21 Feb 2011
*/

public class ObserverListFilter extends CustomFilterPluginBase {

	@Override
	protected void filter(List<ValidObservation> obs) {
		String [] observer = getObserverList();
		for (ValidObservation curr : obs) {
			for (int i = 0; i < observer.length; i++) {
				if (observer[i].equals(curr.getObsCode())) {
					addToSubset(curr);
				}
			}
		}
	}

	@Override
	public String getDescription() {
		return "Filter for list of Observers";
	}

	@Override
	public String getDisplayName() {
		return "Observer List Filter";
	}

	public String[] getObserverList() {
		String str = JOptionPane.showInputDialog("Enter observer codes separated by spaces:");
		return str.split("\\s+");
	}
}