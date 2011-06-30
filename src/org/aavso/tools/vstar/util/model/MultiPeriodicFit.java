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

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

// TODO:
// - multiple period fit UT against TS
// - PeriodFitParameters.toString() that shows coefficients and display in table panes
// - Modelling task
// - work through Grant's BZ Uma example
// code clean-up

/**
 * This class creates a multi-periodic fit model for the specified observations.
 */
public class MultiPeriodicFit implements IModel {

	private List<Double> periods;
	private String desc;
	private List<ValidObservation> fit;
	private List<ValidObservation> residuals;
	private List<PeriodFitParameters> parameters;

	/**
	 * Constructor.
	 */
	public MultiPeriodicFit(List<Double> periods, List<ValidObservation> fit,
			List<ValidObservation> residuals,
			List<PeriodFitParameters> parameters) {
		this.periods = periods;
		this.fit = fit;
		this.residuals = residuals;
		this.parameters = parameters;
		desc = null;
	}

	/**
	 * @see org.aavso.tools.vstar.util.model.IModel#getDescription()
	 */
	@Override
	public String getDescription() {
		if (desc == null) {
			desc = "Fit from periods: ";
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
		// Nothing to do.
	}
}
