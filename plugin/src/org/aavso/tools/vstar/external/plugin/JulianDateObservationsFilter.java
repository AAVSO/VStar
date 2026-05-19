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

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidObservation.JDflavour;
import org.aavso.tools.vstar.plugin.CustomFilterPluginBase;
import org.aavso.tools.vstar.util.Pair;

public class JulianDateObservationsFilter extends CustomFilterPluginBase {
	
	@Override
	protected Pair<String, String> filter(List<ValidObservation> obs) {
		for (ValidObservation curr : obs) {
			if (curr.getJDflavour() == JDflavour.JD) {
				addToSubset(curr);
			}
		}

		return new Pair<String, String>("Julian Date Observations", "Julian Date Observations");
	}
	
	@Override
	public String getDescription() {
		return "Filter for Julian Date Observations";
	}

	@Override
	public String getDisplayName() {
		return "Julian Date Observations Filter";
	}
	
	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "https://github.com/AAVSO/VStar/wiki/Custom-Filter-Plug%E2%80%90ins#julian-date-observations-filter";
	}

	@Override
	public Boolean test() {
		setTestMode(true);

		List<ValidObservation> obs = new ArrayList<ValidObservation>();

		ValidObservation jdOb = new ValidObservation();
		jdOb.setJD(2450000);
		jdOb.setMagnitude(new Magnitude(5, 0));
		jdOb.setJDflavour(JDflavour.JD);
		obs.add(jdOb);

		ValidObservation bjdOb = new ValidObservation();
		bjdOb.setJD(2450001);
		bjdOb.setMagnitude(new Magnitude(6, 0));
		bjdOb.setJDflavour(JDflavour.BJD);
		obs.add(bjdOb);

		filteredObs = new LinkedHashSet<ValidObservation>();
		Pair<String, String> idPair = filter(obs);

		boolean success = filteredObs.size() == 1;
		success &= filteredObs.contains(jdOb);
		success &= "Julian Date Observations".equals(idPair.first);
		success &= "Julian Date Observations".equals(idPair.second);

		setTestMode(false);
		return success;
	}

}
