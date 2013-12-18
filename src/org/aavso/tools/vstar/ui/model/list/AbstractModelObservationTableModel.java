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

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.message.ModelSelectionMessage;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This class is a base table model for derived model data.
 * 
 * The model is notified of model data change.
 */
@SuppressWarnings("serial")
public abstract class AbstractModelObservationTableModel extends
		AbstractSyntheticObservationTableModel implements
		Listener<ModelSelectionMessage> {

	protected SeriesType seriesType;

	public AbstractModelObservationTableModel(List<ValidObservation> obs,
			SeriesType seriesType) {
		super(obs);
		assert seriesType == SeriesType.Model
				|| seriesType == SeriesType.Residuals;
		this.seriesType = seriesType;
	}

	@Override
	public void update(ModelSelectionMessage info) {
		if (seriesType == SeriesType.Model) {
			obs = info.getModel().getFit();
		} else {
			obs = info.getModel().getResiduals();
		}

		populateObsToRowMap();
		fireTableDataChanged();
	}

	@Override
	public boolean canBeRemoved() {
		return true;
	}
}