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
package org.aavso.tools.vstar.ui.model.plot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.jfree.data.xy.AbstractXYDataset;

/**
 * This class is the basis of all 2D period analysis plot models.
 * 
 * TODO: may later need a PlotType enum for 2D XY, 3D XY etc
 */
public class PeriodAnalysis2DPlotModel extends AbstractXYDataset {

	private Map<PeriodAnalysisCoordinateType, List<Double>> coordinates;

	// TODO: instead, pass in freq array and one of period, power, amplitude
	// and also PeriodAnalysisCoordinateType.
	
	/**
	 * Constructor
	 * 
	 * @param coordinates
	 *            A mapping from period analysis coordinates type to lists of
	 *            data values.
	 */
	public PeriodAnalysis2DPlotModel(
			Map<PeriodAnalysisCoordinateType, List<Double>> coordinates) {
		super();
		this.coordinates = coordinates;
	}

	/**
	 * @see org.jfree.data.general.AbstractSeriesDataset#getSeriesCount()
	 * @return The number of series to plot, which is the number of coordinates
	 *         less 2 since the frequency coordinate is used as the x coordinate
	 *         to make (x,y) coordinate pairs (where y is period, power), and
	 *         we exclude amplitude.
	 */
	public int getSeriesCount() {
		return PeriodAnalysisCoordinateType.values().length - 1;
	}

	/**
	 * @see org.jfree.data.general.AbstractSeriesDataset#getSeriesKey(int)
	 */
	public Comparable getSeriesKey(int series) {
		return PeriodAnalysisCoordinateType.getTypeFromIndex(series)
				.getDescription();
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getItemCount(int)
	 */
	public int getItemCount(int series) {
		PeriodAnalysisCoordinateType type = PeriodAnalysisCoordinateType
				.getTypeFromIndex(series);
		return this.coordinates.get(type).size();
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getX(int, int)
	 */
	public Number getX(int series, int item) {
		// TODO: this is really a special case, as per enum type comment
		return this.coordinates.get(PeriodAnalysisCoordinateType.FREQUENCY)
				.get(item);
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getY(int, int)
	 */
	public Number getY(int series, int item) {
		PeriodAnalysisCoordinateType type = PeriodAnalysisCoordinateType
				.getTypeFromIndex(series);
		return this.coordinates.get(type).get(item);
	}
}
