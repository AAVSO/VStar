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
package org.aavso.tools.vstar.util.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.util.comparator.JDComparator;
import org.aavso.tools.vstar.util.period.wwz.WWZStatistic;
import org.aavso.tools.vstar.util.period.wwz.WeightedWaveletZTransform;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This class represents a WWZ derived multi-period fit model.
 */
public class WWZMultiperiodicModel implements IModel {

	private WeightedWaveletZTransform wwt;
	private List<Double> periods;
	private List<ValidObservation> obs;

	private List<ValidObservation> fit;
	private List<ValidObservation> residuals;

	private String desc;

	private boolean interrupted;
	
	/**
	 * Constructor
	 * 
	 * @param wwt
	 *            The WWZ algorithm object.
	 * @param periods
	 *            A list of periods from which to extract (from the stats) the
	 *            best-fit sinusoid values.
	 */
	public WWZMultiperiodicModel(WeightedWaveletZTransform wwt,
			List<Double> periods) {
		super();
		this.wwt = wwt;
		this.periods = periods;
		obs = wwt.getObs();

		fit = new ArrayList<ValidObservation>();
		residuals = new ArrayList<ValidObservation>();
		
		interrupted = false;
	}

	/**
	 * @see org.aavso.tools.vstar.util.IAlgorithm#execute()
	 */
	@Override
	public void execute() throws AlgorithmError {
		interrupted = false;

		// TODO: For multiple periods, do we need instead to average the
		// best-fit and residual values?

		// TODO: pass in wwt object so we can always use the full stats list and
		// get observations

		for (double period : periods) {
			// Iterate over each statistic, looking for entries for which the
			// period is the same as our target. When one is found, create a fit
			// observation. For all observations since the previous period was
			// found, create residuals from the observations in that range.
			int i = 0;

			// Always use all statistcs vs maximal statistics to create model.
			// TODO: sanity check that!
			for (WWZStatistic stat : wwt.getStats()) {
				
				if (stat.getPeriod() == period) {
					
					String comment = "From WWZ, period "
							+ String.format(NumericPrecisionPrefs
									.getOtherOutputFormat(), period);

					// Create a fit observation from the average magnitude for
					// this time-frequency/period combination.
					ValidObservation fitOb = new ValidObservation();
					fitOb.setDateInfo(new DateInfo(stat.getTau()));
					fitOb.setMagnitude(new Magnitude(stat.getMave(), 0));
					fitOb.setBand(SeriesType.Model);
					fitOb.setComments(comment);
					fit.add(fitOb);

					// Create a residual observation for each observation since
					// the previous period. TODO: sanity check this approach,
					// i.e. does it actually make sense for a tau value to
					// represent a range of observation time values for the
					// purpose of residual creation?
					while (i < obs.size()
							&& obs.get(i).getJD() <= stat.getTau()) {
						double residual = obs.get(i).getMag() - stat.getMave();
						ValidObservation residualOb = new ValidObservation();
						residualOb
								.setDateInfo(new DateInfo(obs.get(i).getJD()));
						residualOb.setMagnitude(new Magnitude(residual, 0));
						residualOb.setBand(SeriesType.Residuals);
						residualOb.setComments(comment);
						residuals.add(residualOb);
						i++;
					}
				}
				
				if (interrupted) {
					return;
				}
			}
		}

		// For multiple periods, observations will be out of time order, so sort
		// by JD.
		Collections.sort(fit, JDComparator.instance);
		Collections.sort(residuals, JDComparator.instance);
	}

	/**
	 * @see org.aavso.tools.vstar.util.model.IModel#getDescription()
	 */
	@Override
	public String getDescription() {
		if (desc == null) {
			desc = getKind() + " from periods: ";
			for (Double period : periods) {
				desc += String.format(NumericPrecisionPrefs
						.getOtherOutputFormat(), period)
						+ " ";
			}
		}

		return desc;
	}

	/**
	 * @see org.aavso.tools.vstar.util.model.IModel#getKind()
	 */
	@Override
	public String getKind() {
		return "Multi-periodic fit from WWZ";
	}

	/**
	 * @see org.aavso.tools.vstar.util.model.IModel#getFit()
	 */
	@Override
	public List<ValidObservation> getFit() {
		return fit;
	}

	/**
	 * @see org.aavso.tools.vstar.util.model.IModel#getResiduals()
	 */
	@Override
	public List<ValidObservation> getResiduals() {
		return residuals;
	}

	@Override
	public List<PeriodFitParameters> getParameters() {
		return null;
	}

	@Override
	public boolean hasFuncDesc() {
		return false;
	}
	
	public String toString() {
		return getDescription();
	}

	@Override
	public void interrupt() {
		interrupted = true;
	}

	@Override
	public Map<String, String> getFunctionStrings() {
		return null;
	}
}
