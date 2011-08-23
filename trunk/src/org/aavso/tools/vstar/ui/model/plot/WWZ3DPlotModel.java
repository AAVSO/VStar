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

import org.aavso.tools.vstar.util.period.wwz.WWZCoordinateType;
import org.aavso.tools.vstar.util.period.wwz.WWZStatistic;
import org.jfree.data.xy.XYZDataset;

/**
 * This class represents a plot model for three coordinates of Weighted Wavelet
 * Z-Transform statistics, e.g. Tau vs Frequency vs WWZ. The result may be a
 * "contour plot" where the 3rd dimension is represented by a colour, or it may
 * be a 3D plot, i.e. one with three axes.
 * 
 * We extend our existing WWZ 2D plot model with the additional Z coordinate.
 */
public class WWZ3DPlotModel extends WWZ2DPlotModel implements XYZDataset {

	private WWZCoordinateType zType; // e.g. wwz

	/**
	 * Constructor
	 * 
	 * @param stats
	 *            A list of WWZ statistics.
	 * @param domainType
	 *            The domain (X) WWZ statistic coordinate type (e.g. tau).
	 * @param rangeType
	 *            The range (Y) WWZ statistic coordinate type (e.g. period).
	 * @param zType
	 *            The Z WWZ statistic coordinate type (e.g. wwz).
	 */
	public WWZ3DPlotModel(List<WWZStatistic> stats,
			WWZCoordinateType domainType, WWZCoordinateType rangeType,
			WWZCoordinateType zType) {
		super(stats, domainType, rangeType);
		this.zType = zType;
	}

	/**
	 * @return the Z coordinate type
	 */
	public WWZCoordinateType getZType() {
		return zType;
	}

	@Override
	public Comparable getSeriesKey(int series) {
		return rangeType.toString() + " vs " + domainType.toString() + " vs "
				+ zType.toString();
	}

	// AbstractXYZDataset implements getZValue() in terms of getZ() but since we
	// have to implement both here (since they're both on the XYZDataset
	// interface), we have both call straight to the base class getValue()
	// method for efficiency.

	/**
	 * @see org.jfree.data.xy.XYZDataset#getZ(int, int)
	 */
	@Override
	public Number getZ(int series, int item) {
		return stats.get(item).getValue(zType);
	}

	@Override
	public double getZValue(int series, int item) {
		return stats.get(item).getValue(zType);
	}
}
