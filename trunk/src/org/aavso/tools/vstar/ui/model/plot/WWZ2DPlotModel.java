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
import org.jfree.data.xy.AbstractXYDataset;

/**
 * This class represents a line plot model for two coordinates of Weighted
 * Wavelet Z-Transform statistics, e.g. Tau vs Frequency or Period or Amplitude.
 */
public class WWZ2DPlotModel extends AbstractXYDataset {

	protected List<WWZStatistic> stats;
	protected WWZCoordinateType domainType; // e.g. tau
	protected WWZCoordinateType rangeType; // e.g. period, frequency

	/**
	 * Constructor
	 * 
	 * @param stats
	 *            A list of WWZ statistics.
	 * @param domainType
	 *            The domain (X) WWZ statistic coordinate type (e.g. Tau).
	 * @param rangeType
	 *            The range (Y) WWZ statistic coordinate type (e.g. Period).
	 */
	public WWZ2DPlotModel(List<WWZStatistic> stats,
			WWZCoordinateType domainType, WWZCoordinateType rangeType) {
		super();
		this.stats = stats;
		this.domainType = domainType;
		this.rangeType = rangeType;
	}

	/**
	 * @return the stats
	 */
	public List<WWZStatistic> getStats() {
		return stats;
	}

	/**
	 * @return the domainType
	 */
	public WWZCoordinateType getDomainType() {
		return domainType;
	}

	/**
	 * @return the rangeType
	 */
	public WWZCoordinateType getRangeType() {
		return rangeType;
	}

	/**
	 * 
	 * @see org.jfree.data.general.AbstractSeriesDataset#getSeriesCount()
	 */
	@Override
	public int getSeriesCount() {
		return 1;
	}

	/**
	 * 
	 * @see org.jfree.data.general.AbstractSeriesDataset#getSeriesKey(int)
	 */
	@Override
	public Comparable getSeriesKey(int series) {
		return rangeType.toString() + " vs " + domainType.toString();
	}

	/**
	 * 
	 * @see org.jfree.data.xy.XYDataset#getItemCount(int)
	 */
	@Override
	public int getItemCount(int series) {
		return stats.size();
	}

	/**
	 * 
	 * @see org.jfree.data.xy.XYDataset#getX(int, int)
	 */
	@Override
	public Number getX(int series, int item) {
		return stats.get(item).getValue(domainType);
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getY(int, int)
	 */
	@Override
	public Number getY(int series, int item) {
		return stats.get(item).getValue(rangeType);
	}

	/**
	 * @see org.jfree.data.xy.AbstractXYDataset#getXValue(int, int)
	 */
	@Override
	public double getXValue(int series, int item) {
		return stats.get(item).getValue(domainType);
	}

	/**
	 * @see org.jfree.data.xy.AbstractXYDataset#getYValue(int, int)
	 */
	@Override
	public double getYValue(int series, int item) {
		return stats.get(item).getValue(rangeType);
	}
}
