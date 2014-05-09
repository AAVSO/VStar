/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2014  AAVSO (http://www.aavso.org/)
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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.NumberField;
import org.aavso.tools.vstar.ui.dialog.series.SingleSeriesSelectionDialog;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.SeriesCreationMessage;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This plug-in allows a new series to be created from an existing series by
 * taking one series and shifting the magnitudes of each observation by a
 * specified amount.
 */
public class MagnitudeShiftTool extends ObservationToolPluginBase {

	private Double magDelta;

	@Override
	public void invoke(ISeriesInfoProvider seriesInfo) {
		SingleSeriesSelectionDialog seriesSelector = new SingleSeriesSelectionDialog(
				Mediator.getInstance().getObservationPlotModel(
						AnalysisType.RAW_DATA));

		if (!seriesSelector.isCancelled()) {
			// Get magnitude shift.
			List<NumberField> inputFields = new ArrayList<NumberField>();
			NumberField magDeltaField = new NumberField("Magnitude Change",
					null, null, magDelta);
			inputFields.add(magDeltaField);
			MultiEntryComponentDialog magDeltaDlg = new MultiEntryComponentDialog(
					"Magnitude Change Input", null, inputFields);

			// Create a new series with the adjusted magnitude.
			if (!magDeltaDlg.isCancelled()) {
				magDelta = magDeltaField.getValue();
				
				SeriesType type = seriesSelector.getSeries();
				List<ValidObservation> obs = seriesInfo.getObservations(type);
				
				String description = String.format("%s shifted by "
						+ NumericPrecisionPrefs.getMagOutputFormat(), type
						.getDescription(), magDelta);
				String shortName = String.format("%s + "
						+ NumericPrecisionPrefs.getMagOutputFormat(), type
						.getShortName(), magDelta);
				Color color = type.getColor();
				
//				SeriesType newType = SeriesType.create(description, shortName,
//						color, true, true);

				// TODO: why does plot zoom cause a shift series to disappear?
				
				SeriesType newType = type;
				
				List<ValidObservation> newObs = new ArrayList<ValidObservation>(
						obs);
				for (ValidObservation newOb : newObs) {
					Magnitude newMag = new Magnitude(newOb.getMag() + magDelta, newOb
							.getMagnitude().getUncertainty());
					newOb.setMagnitude(newMag);
				}
				
				SeriesCreationMessage msg = new SeriesCreationMessage(this,
						newType, newObs);

				Mediator.getInstance().getSeriesCreationNotifier()
						.notifyListeners(msg);
			}
		}
	}

	@Override
	public String getDescription() {
		return "Shifts the magnitude of observations in one series, creating another.";
	}

	@Override
	public String getDisplayName() {
		return "Magnitude shift tool";
	}
}
