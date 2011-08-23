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
import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisAlgorithm;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This class creates a multi-periodic fit model for the specified observations.
 */
public class PeriodAnalysisDerivedMultiPeriodicModel implements IModel {

	private List<Double> periods;
	private IPeriodAnalysisAlgorithm algorithm;

	private List<ValidObservation> fit;
	private List<ValidObservation> residuals;
	private List<PeriodFitParameters> parameters;

	private String desc;
	private String strRepr;

	/**
	 * Constructor.
	 * 
	 * @param periods
	 *            The periods to be used to create the fit.
	 * @param algorithm
	 *            The algorithm to be executed to generate a fit.
	 */
	public PeriodAnalysisDerivedMultiPeriodicModel(List<Double> periods,
			IPeriodAnalysisAlgorithm algorithm) {
		this.periods = periods;
		this.algorithm = algorithm;

		this.fit = new ArrayList<ValidObservation>();
		this.residuals = new ArrayList<ValidObservation>();
		this.parameters = new ArrayList<PeriodFitParameters>();

		desc = null;
		strRepr = null;
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
		return "Multi-periodic fit";
	}

	/**
	 * @return the periods
	 */
	public List<Double> getPeriods() {
		return periods;
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

	/**
	 * @return the parameters
	 */
	public List<PeriodFitParameters> getParameters() {
		return parameters;
	}

	/**
	 * @see org.aavso.tools.vstar.util.IAlgorithm#execute()
	 */
	@Override
	public void execute() throws AlgorithmError {
		algorithm.multiPeriodicFit(periods, this);
	}

	public String toString() {
		if (strRepr == null) {
			String paramStr = "";
			for (int i = 0; i < parameters.size(); i++) {
				PeriodFitParameters params = parameters.get(i);
				paramStr += params.toString();
				if (i < parameters.size() - 1) {
					paramStr += "\n";
				}
			}

			strRepr = getDescription() + "\n" + paramStr;
		}
		return strRepr;
	}
}
