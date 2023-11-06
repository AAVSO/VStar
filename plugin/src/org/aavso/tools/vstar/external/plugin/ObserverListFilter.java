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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.JOptionPane;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.CustomFilterPluginBase;
import org.aavso.tools.vstar.util.Pair;

/**
 * This VStar plugin can be used to create a filter for a group of observers.
 * 
 * @author Sara J. Beck (with lots of help from D. Benn!)
 * @version 1.0 - 21 Feb 2011
 * @version 1.1 - 21 May 2014: fixed null-pointer exception
 * @version 1.2 - 1 May 2017: adapted to modified filter() signature
 */

public class ObserverListFilter extends CustomFilterPluginBase {

	private String[] observers;
	
	@Override
	protected Pair<String, String> filter(List<ValidObservation> obs) {
		if (!inTestMode()) {
			observers = getObserverList();
		}
		
		for (ValidObservation curr : obs) {
			for (int i = 0; i < observers.length; i++) {
				if (observers[i].equals(curr.getObsCode())) {
					addToSubset(curr);
				}
			}
		}

		StringBuffer buf = new StringBuffer();
		buf.append("Observer filter: ");
		for (String observer : observers) {
			buf.append(observer);
			buf.append(" ");
		}

		return new Pair<String, String>(buf.toString(), buf.toString());
	}

	@Override
	public String getDescription() {
		return "Filter for list of Observers";
	}

	@Override
	public String getDisplayName() {
		return "Observer List Filter";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "https://github.com/AAVSO/VStar/wiki/Custom-Filter-Plug%E2%80%90ins#filter-for-list-of-observers";
	}

	public String[] getObserverList() {
		String str = JOptionPane.showInputDialog("Enter observer codes separated by spaces:");

		String[] obsList = {};

		if (str != null) {
			obsList = str.split("\\s+");
		}

		return obsList;
	}

	@Override
	public Boolean test() {
		setTestMode(true);
		
		observers = new String[]{"FOO"};
		
		List<ValidObservation> obs = new ArrayList<ValidObservation>();
		ValidObservation fooOb = new ValidObservation();
		fooOb.setJD(2450000);
		fooOb.setMagnitude(new Magnitude(5, 0));
		fooOb.setObsCode("FOO");
		obs.add(fooOb);
		ValidObservation barOb = new ValidObservation();
		barOb.setJD(2450001);
		fooOb.setMagnitude(new Magnitude(6, 0));
		barOb.setObsCode("BAR");

		// next 2 lines normally executed in superclass apply() method
		filteredObs = new LinkedHashSet<ValidObservation>();
		Pair<String, String> idPair = filter(obs);
				
		boolean success = true;
		
		success &= filteredObs.size() == 1;
		success &= "Observer filter: FOO ".equals(idPair.first);
		success &= "Observer filter: FOO ".equals(idPair.second);
		
		return success;
	}
}