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
package org.aavso.tools.vstar.external.plugin;

import java.awt.Color;
import java.util.List;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ObservationPlotModel;

/**
 * This plug-in is an additive source of B-V observations if such bands exist in
 * the current dataset.
 */
public class BMinusVObservationSource extends ObservationSourcePluginBase {

	private SeriesType bvSeries;
	
	public BMinusVObservationSource() {
		super();
		isAdditive = true;
		bvSeries = SeriesType.create("B-V", "B-V", Color.MAGENTA, true, false);
	}

	@Override
	public String getDisplayName() {
		return "Create B-V series...";
	}

	@Override
	public String getDescription() {
		return "B-V series creator";
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new BMinusVRetriever();
	}

	@Override
	public InputType getInputType() {
		return InputType.NONE;
	}

	class BMinusVRetriever extends AbstractObservationRetriever {

		private List<ValidObservation> bObs, vObs;
		private int records;

		//@Override
		public Integer getNumberOfRecords() throws ObservationReadError {
			Mediator mediator = Mediator.getInstance();

			ObservationPlotModel model = mediator
					.getObservationPlotModel(AnalysisType.RAW_DATA);

			bObs = model.getObservations(SeriesType.Johnson_B);
			vObs = model.getObservations(SeriesType.Johnson_V);

			records = bObs != null && vObs != null ? Math.min(bObs.size(),
					vObs.size()) : 0;

			return records;
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			if (records > 0) {
				for (int i = 0; i < records; i++) {
					// Note: simplifying assumption: B and V elements correspond
					// in time!
					double deltaMag = bObs.get(i).getMag() - vObs.get(i).getMag();
					double meanJD = (bObs.get(i).getJD() + vObs.get(i).getJD()) / 2;
					ValidObservation bvOb = new ValidObservation();
					bvOb.setDateInfo(new DateInfo(meanJD));
					bvOb.setMagnitude(new Magnitude(deltaMag, 0));
					bvOb.setBand(bvSeries);
					collectObservation(bvOb);
				}
			}
		}

		@Override
		public String getSourceType() {
			return "B-V";
		}

		@Override
		public String getSourceName() {
			return "B-V";
		}
	}
}
