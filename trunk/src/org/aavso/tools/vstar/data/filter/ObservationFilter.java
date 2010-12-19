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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * A filter for valid observations.
 * 
 * An observation will be passed via this filter if the conjunction of all its
 * sub-filters (field matchers) match the observation.
 */
public class ObservationFilter {

	// Potential matchers for any observation filter.
	public static final Map<String, IObservationFieldMatcher> MATCHERS;

	static {
		MATCHERS = new TreeMap<String, IObservationFieldMatcher>();

		IObservationFieldMatcher obsCodeMatcher = new ObsCodeFieldMatcher();
		MATCHERS.put(obsCodeMatcher.getDisplayName(), obsCodeMatcher);

		IObservationFieldMatcher magnitudeMatcher = new MagnitudeFieldMatcher();
		MATCHERS.put(magnitudeMatcher.getDisplayName(), magnitudeMatcher);
		
		IObservationFieldMatcher jdMatcher = new JDFieldMatcher();
		MATCHERS.put(jdMatcher.getDisplayName(), jdMatcher);

		IObservationFieldMatcher transformedMatcher = new TransformedFieldMatcher();
		MATCHERS.put(transformedMatcher.getDisplayName(), transformedMatcher);
		
		IObservationFieldMatcher seriesTypeMatcher = new SeriesTypeFieldMatcher();
		MATCHERS.put(seriesTypeMatcher.getDisplayName(), seriesTypeMatcher);
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
	 * @return the matchers
	 */
	public List<IObservationFieldMatcher> getMatchers() {
		return matchers;
	}

	/**
	 * Reset this filter's state.
	 */
	public void reset() {
		matchers.clear();
	}

	/**
	 * Filter the supplied list of observations.
	 * 
	 * @param obs
	 *            The observation list to be filtered.
	 * @return The ordered (by insertion) set of filtered observations.
	 */
	public Set<ValidObservation> getFilteredObservations(
			List<ValidObservation> obs) {
		// We use a LinkedHashSet to maintain addition and lookup efficiency
		// while maintaining insertion order.
		Set<ValidObservation> matchingObs = new LinkedHashSet<ValidObservation>();

		for (int i = 0; i < obs.size(); i++) {
			ValidObservation ob = obs.get(i);
			if (matches(ob)) {
				matchingObs.add(ob);
			}
		}

		return matchingObs;
	}

	/**
	 * Does the specified observation satisfy all this filter's matchers? The
	 * matching process is short-circuited when one sub-filter fails.
	 * 
	 * @param ob
	 *            The observation under test.
	 * @return True or false.
	 * @precondition It only makes sense to call this method when 'matchers' is
	 *               non-empty, otherwise all observations will be filtered in.
	 */
	protected boolean matches(ValidObservation ob) {
		boolean matching = true;

		for (IObservationFieldMatcher matcher : matchers) {
			matching &= matcher.matches(ob);
			if (!matching)
				break;
		}

		return matching;
	}
}
