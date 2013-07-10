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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.apache.commons.math.FunctionEvaluationException;
import org.jfree.data.xy.AbstractXYDataset;

/**
 * This class represents a continuous plot of a Model series.
 */
@SuppressWarnings("serial")
public class ContinuousModelPlotModel extends AbstractXYDataset {

	private Map<Integer, List<ValidObservation>> seriesNumToObSrcListMap;
	private ContinuousModelFunction modelFunction;
	private double step;
	private double nextX;

	/**
	 * Constructor
	 * 
	 * @param coordSrc
	 *            The coordinate source.
	 * @param model
	 *            The model.
	 */
	public ContinuousModelPlotModel(ContinuousModelFunction modelFunction) {
		super();
		seriesNumToObSrcListMap = new HashMap<Integer, List<ValidObservation>>();
		seriesNumToObSrcListMap.put(0, modelFunction.getFit());
		this.modelFunction = modelFunction;
		step = modelFunction.getFit().size() / 100;
		nextX = modelFunction.getFit().get(0).getJD();
	}

	@Override
	public int getSeriesCount() {
		return 1;
	}

	@Override
	public Comparable getSeriesKey(int series) {
		return SeriesType.ModelFunction;
	}

	@Override
	public int getItemCount(int series) {
		return modelFunction.getFit().size();
		// return (int) (model.getFit().size() / step);
	}

	@Override
	public Number getX(int series, int item) {
		return modelFunction.getCoordSrc().getXCoord(series, item,
				seriesNumToObSrcListMap);
	}

	@Override
	public Number getY(int series, int item) {
		double y = 0;

		if (this.modelFunction.getCoordSrc() == StandardPhaseCoordSource.instance) {
			// System.out.println("***");
		}

		if (this.modelFunction.getCoordSrc() == PreviousCyclePhaseCoordSource.instance) {
			// System.out.println("***");
		}

		try {
			// double x = modelFunction.getCoordSrc().getXCoord(series, item,
			// seriesNumToObSrcListMap);

			// Note: The function must be computed with JD not phase since
			// that's what was used initially.
			double x = modelFunction.getFit().get(item).getJD()
					- modelFunction.getZeroPoint();

			y = modelFunction.getFunction().value(x);
		} catch (FunctionEvaluationException e) {
			// TODO
		}

		// nextX += step;

		return y;
	}
}
