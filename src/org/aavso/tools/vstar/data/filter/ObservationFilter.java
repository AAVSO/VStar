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
package org.aavso.tools.vstar.data.filter;

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * A filter for valid observations.
 */
public class ObservationFilter {

	// Potential matchers for any observation filter.
	public static final IObservationFieldMatcher[] MATCHERS;
	
	static {
		MATCHERS = new IObservationFieldMatcher[]{
				new ObsCodeFieldMatcher()
		};
	}
	
	// Actual matchers for this observation filter instance.
	private List<IObservationFieldMatcher> matchers;
	
	public ObservationFilter() {
		matchers = new ArrayList<IObservationFieldMatcher>();
	}
	
	public void addMatcher(IObservationFieldMatcher matcher) {
		matchers.add(matcher);
	}
	
	/**
	 * Does the specified observation satisfy all this 
	 * filter's matchers?
	 * 
	 * @param ob The observation under test.
	 * @return True or false.
	 */
	public boolean matches(ValidObservation ob) {
		boolean matching = true;
		
		for (IObservationFieldMatcher matcher : matchers) {
			matching &= matcher.matches(ob);
			if (!matching) break;
		}
		
		return matching;
	}
}
