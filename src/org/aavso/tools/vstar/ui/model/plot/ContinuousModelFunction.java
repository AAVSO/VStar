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
package org.aavso.tools.vstar.ui.model.plot;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.apache.commons.math.analysis.UnivariateRealFunction;

/**
 * This class represents a continuous model function, the synthetic
 * ValidObservations resulting from the model and any context required to apply
 * it such as coordinate source.
 */
public class ContinuousModelFunction {

	private UnivariateRealFunction function;
	private ICoordSource coordSrc;
	private List<ValidObservation> fit;
	private double zeroPoint;
	
	private PhasedObservationAndMeanPlotModel ppModel;

	/**
	 * Constructor
	 * 
	 * @param function
	 *            The univariate real function.
	 * @param fit
	 *            The synthetic observations resulting from the fit.
	 * @param zeroPoint
	 *            The zero point to be subtracted from applications of the
	 *            function, i.e. calls to UnivariateRealFunction.value().
	 * @param coordSrc
	 *            The coordinate source (JD, phase).
	 */
	public ContinuousModelFunction(UnivariateRealFunction function,
			List<ValidObservation> fit, double zeroPoint, ICoordSource coordSrc) {
		super();
		this.function = function;
		this.coordSrc = coordSrc;
		this.fit = fit;
		this.zeroPoint = zeroPoint;
	}

	/**
	 * Constructor: defaults to JD coordinate source.
	 * 
	 * @param function
	 *            The univariate real function.
	 * @param fit
	 *            The synthetic observations resulting from the fit.
	 * @param zeroPoint
	 *            The zero point to be subtracted from applications of the
	 *            function, i.e. calls to UnivariateRealFunction.value().
	 */
	public ContinuousModelFunction(UnivariateRealFunction function,
			List<ValidObservation> fit, double zeroPoint) {
		this(function, fit, zeroPoint, JDCoordSource.instance);
	}

	/**
	 * Constructor: defaults to JD coordinate source and no (0) zero point.
	 * 
	 * @param function
	 *            The univariate real function.
	 * @param fit
	 *            The synthetic observations resulting from the fit.
	 */
	public ContinuousModelFunction(UnivariateRealFunction function,
			List<ValidObservation> fit) {
		this(function, fit, 0, JDCoordSource.instance);
	}

	/**
	 * @param ppModel
	 *            the ppModel to set
	 */
	public void setPpModel(PhasedObservationAndMeanPlotModel ppModel) {
		this.ppModel = ppModel;
	}

	/**
	 * @return the ppModel
	 */
	public PhasedObservationAndMeanPlotModel getPpModel() {
		return ppModel;
	}

	/**
	 * @return the function
	 */
	public UnivariateRealFunction getFunction() {
		return function;
	}

	/**
	 * @return the coordSrc
	 */
	public ICoordSource getCoordSrc() {
		return coordSrc;
	}

	/**
	 * @return the fit
	 */
	public List<ValidObservation> getFit() {
		return fit;
	}

	/**
	 * @return the zeroPoint
	 */
	public double getZeroPoint() {
		return zeroPoint;
	}
}
