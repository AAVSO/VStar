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
package org.aavso.tools.vstar.ui.pane;

import javax.swing.RowFilter;

import org.aavso.tools.vstar.ui.mediator.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.model.list.ValidObservationTableModel;

/**
 * An observation table row filter class.
 */
public class ObservationTableRowFilter extends
		RowFilter<ValidObservationTableModel, Integer> {
	
	private FilteredObservationMessage filteredObsMsg;

	ObservationTableRowFilter(FilteredObservationMessage msg) {
		filteredObsMsg = msg;
	}

	// TODO: Instead, create an interface IObservationSource with getObservations()
	// method that we can pull ValidObservations from and check for presence in a
	// HashSet of filtered ValidObservations keyed on reference for O(1) lookup.
	
	// Note that this won't help mean observation list filtering since the
	// filtered set will only contain raw valid obs. So, we won't include mean
	// observations in table or on plots for filtering. Either that or we have to
	// add the means in with the obs being filtered, recalculating the set when 
	// the mean series changes.
	
	// The code changes are still worth it from the efficiency and genericity angles.
	
	@Override
	public boolean include(
			RowFilter.Entry<? extends ValidObservationTableModel, ? extends Integer> entry) {
		int id = entry.getIdentifier();
		return filteredObsMsg.getFilteredObsMap().keySet().contains(id);
	}
}
