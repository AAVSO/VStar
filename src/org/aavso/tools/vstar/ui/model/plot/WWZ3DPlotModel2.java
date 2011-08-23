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
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;

/**
 * This class represents a plot model for three coordinates of Weighted Wavelet
 * Z-Transform statistics, e.g. Tau vs Frequency vs WWZ. The result may be a
 * "contour plot" where the 3rd dimension is represented by a colour, or it may
 * be a 3D plot, i.e. one with three axes.
 */
public class WWZ3DPlotModel2 implements XYZDataset {

	private List<WWZStatistic> stats;
	private WWZCoordinateType domainType; // e.g. tau
	private WWZCoordinateType rangeType; // e.g. period, frequency
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
	public WWZ3DPlotModel2(List<WWZStatistic> stats,
			WWZCoordinateType domainType, WWZCoordinateType rangeType,
			WWZCoordinateType zType) {
		super();
		this.stats = stats;
		this.domainType = domainType;
		this.rangeType = rangeType;
		this.zType = zType;
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
	 * @return the Z coordinate type
	 */
	public WWZCoordinateType getZType() {
		return zType;
	}

	@Override
	public int getSeriesCount() {
		return 1;
	}

	@Override
	public int getItemCount(int series) {
		return stats.size();
	}

	@Override
	public Number getX(int series, int item) {
		return new Double(getXValue(series, item));
	}

	@Override
	public double getXValue(int series, int item) {
//		return Math.random();
		return getValue(item, domainType);
	}

	@Override
	public Number getY(int series, int item) {
		return new Double(getYValue(series, item));
	}

	@Override
	public double getYValue(int series, int item) {
//		return Math.random();
		return getValue(item, rangeType);
	}

	@Override
	public Number getZ(int series, int item) {
		return new Double(getZValue(series, item));
	}

	@Override
	public double getZValue(int series, int item) {
		return getValue(item, zType);
	}

	@Override
	public Comparable getSeriesKey(int series) {
		return rangeType.toString() + " vs " + domainType.toString() + " vs "
				+ zType.toString();
	}

	@Override
	public int indexOf(Comparable series) {
		return 0;
	}

	@Override
	public void addChangeListener(DatasetChangeListener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removeChangeListener(DatasetChangeListener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public DatasetGroup getGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGroup(DatasetGroup group) {
		// TODO Auto-generated method stub
	}

	@Override
	public DomainOrder getDomainOrder() {
		return DomainOrder.ASCENDING;
	}
	
	// Helpers

	protected final double getValue(int index, WWZCoordinateType type) {
		double value = 0;

		WWZStatistic stat = stats.get(index);

		switch (type) {
		case TAU:
			value = stat.getTau();
			break;
		case FREQUENCY:
			value = stat.getFrequency();
			break;
		case PERIOD:
			value = stat.getPeriod();
			break;
		case WWZ:
			value = stat.getWwz();
			break;
		case SEMI_AMPLITUDE:
			value = stat.getAmplitude();
			break;
		case MEAN_MAG:
			value = stat.getMave();
			break;
		case EFFECTIVE_NUM_DATA:
			value = stat.getNeff();
			break;
		}

		return value;
	}
}
