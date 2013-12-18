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

package org.aavso.tools.vstar.ui.model.list;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.stats.BinningResult;

/**
 * This class is a base table model for derived mean observation data.
 * 
 * The model is notified of wholesale mean data change.
 */
@SuppressWarnings("serial")
public abstract class AbstractMeanObservationTableModel extends
		AbstractSyntheticObservationTableModel implements Listener<BinningResult> {

	/**
	 * Constructor.
	 * 
	 * @param obs
	 *            The mean initial observation data. The mean data can be 
	 *            updated later via this class's listener interface.
	 */
	public AbstractMeanObservationTableModel(List<ValidObservation> meanObsData) {
		super(meanObsData);
	}

	/**
	 * Listen for updates to the mean data observation list, e.g.
	 * if the bin size has changed.
	 */
	public void update(BinningResult binningResult) {
		
		this.obs = binningResult.getMeanObservations();
		populateObsToRowMap();
		this.fireTableDataChanged();
	}
	
	/**
	 * @see org.aavso.tools.vstar.util.notification.Listener#canBeRemoved()
	 */
	public boolean canBeRemoved() {
		return true;
	}
}