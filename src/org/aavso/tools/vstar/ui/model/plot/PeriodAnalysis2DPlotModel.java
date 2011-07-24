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

import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPoint;
import org.jfree.data.xy.AbstractXYDataset;

/**
 * This class is the basis of all 2D period analysis plot models.
 */
public class PeriodAnalysis2DPlotModel extends AbstractXYDataset {

	private Map<PeriodAnalysisCoordinateType, List<Double>> analysisValues;
	private List<Double> domainValues;
	private List<Double> rangeValues;
	private PeriodAnalysisCoordinateType domainType;
	private PeriodAnalysisCoordinateType rangeType;

	/**
	 * Constructor
	 * 
	 * @param analysisValues
	 *            A mapping from period analysis coordinate type to lists of
	 *            values.
	 * @param domainType
	 *            The type of the domain axis.
	 * @param rangeTypes
	 *            The type of the range axis.
	 */
	public PeriodAnalysis2DPlotModel(
			Map<PeriodAnalysisCoordinateType, List<Double>> analysisValues,
			PeriodAnalysisCoordinateType domainType,
			PeriodAnalysisCoordinateType rangeType) {
		super();
		this.analysisValues = analysisValues;
		this.domainValues = analysisValues.get(domainType);
		this.rangeValues = analysisValues.get(rangeType);
		assert domainValues.size() == rangeValues.size();
		this.domainType = domainType;
		this.rangeType = rangeType;
	}

	/**
	 * @return the analysisValues
	 */
	public Map<PeriodAnalysisCoordinateType, List<Double>> getAnalysisValues() {
		return analysisValues;
	}

	/**
	 * @return the domainValues
	 */
	public List<Double> getDomainValues() {
		return domainValues;
	}

	/**
	 * @return the rangeValues
	 */
	public List<Double> getRangeValues() {
		return rangeValues;
	}

	/**
	 * @return the domainType
	 */
	public PeriodAnalysisCoordinateType getDomainType() {
		return domainType;
	}

	/**
	 * @return the rangeType
	 */
	public PeriodAnalysisCoordinateType getRangeType() {
		return rangeType;
	}

	/**
	 * @see org.jfree.data.general.AbstractSeriesDataset#getSeriesCount()
	 */
	public int getSeriesCount() {
		return 1;
	}

	/**
	 * @see org.jfree.data.general.AbstractSeriesDataset#getSeriesKey(int)
	 */
	public Comparable getSeriesKey(int series) {
		return rangeType.getDescription() + " vs "
				+ domainType.getDescription();
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getItemCount(int)
	 */
	public int getItemCount(int series) {
		return domainValues.size();
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getX(int, int)
	 */
	public Number getX(int series, int item) {
		return domainValues.get(item);
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getY(int, int)
	 */
	public Number getY(int series, int item) {
		return rangeValues.get(item);
	}

	/**
	 * Given an item number, return a period analysis data point object
	 * containing frequency, period, power, and amplitude values.
	 * 
	 * @param item
	 *            The item number in the sequence of data.
	 * @return A data point object corresponding to that point in the sequence.
	 */
	public PeriodAnalysisDataPoint getDataPointFromItem(int item) {
		double frequency = analysisValues.get(
				PeriodAnalysisCoordinateType.FREQUENCY).get(item);
		double period = analysisValues.get(PeriodAnalysisCoordinateType.PERIOD)
				.get(item);
		double power = analysisValues.get(PeriodAnalysisCoordinateType.POWER)
				.get(item);
		double amplitude = analysisValues.get(
				PeriodAnalysisCoordinateType.AMPLITUDE).get(item);

		return new PeriodAnalysisDataPoint(frequency, period, power, amplitude);
	}
	
	/**
	 * Force plot to update.
	 */
	public void refresh() {
		fireDatasetChanged();
	}
}
