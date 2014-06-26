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

import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPoint;
import org.jfree.data.xy.AbstractXYDataset;

/**
 * This class is the basis of all 2D period analysis plot models.
 */
@SuppressWarnings("serial")
public class PeriodAnalysis2DPlotModel extends AbstractXYDataset {

	private Map<PeriodAnalysisCoordinateType, List<Double>> analysisValues;
	private PeriodAnalysisCoordinateType[] coordTypes;
	private List<Double> domainValues;
	private List<Double> rangeValues;
	private PeriodAnalysisCoordinateType domainType;
	private PeriodAnalysisCoordinateType rangeType;
	private boolean isLogarithmic;

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
	 * @param isLogarithmic
	 *            Should range values be logarithmic? (e.g. power).
	 */
	public PeriodAnalysis2DPlotModel(
			Map<PeriodAnalysisCoordinateType, List<Double>> analysisValues,
			PeriodAnalysisCoordinateType domainType,
			PeriodAnalysisCoordinateType rangeType, boolean isLogarithmic) {
		super();
		this.analysisValues = analysisValues;
		this.coordTypes = analysisValues.keySet().toArray(
				new PeriodAnalysisCoordinateType[0]);
		this.domainValues = analysisValues.get(domainType);
		this.rangeValues = analysisValues.get(rangeType);
		assert domainValues.size() == rangeValues.size();
		this.domainType = domainType;
		this.rangeType = rangeType;
		this.isLogarithmic = isLogarithmic;
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
	 * @return the isLogarithmic
	 */
	public boolean isLogarithmic() {
		return isLogarithmic;
	}

	/**
	 * @param isLogarithmic
	 *            the isLogarithmic to set
	 */
	public void setLogarithmic(boolean isLogarithmic) {
		this.isLogarithmic = isLogarithmic;
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
	public Comparable<String> getSeriesKey(int series) {
		return rangeType.getDescription() + " " + LocaleProps.get("VERSUS")
				+ " " + domainType.getDescription();
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
		double yValue = rangeValues.get(item);
		if (isLogarithmic) {
			yValue = yValue == 0 ? 0 : Math.log10(yValue);
		}
		return yValue;
	}

	/**
	 * Given an item number, return a period analysis data point object
	 * containing period analysis result data values.
	 * 
	 * @param item
	 *            The item number in the sequence of data.
	 * @return A data point object corresponding to that point in the sequence.
	 *         May return null if item is out of range.
	 */
	public PeriodAnalysisDataPoint getDataPointFromItem(int item) {
		PeriodAnalysisDataPoint dataPoint = null;

		// This may be a top-hits model and the item may be selected from the
		// full data-set. If so, ignore.
		if (item < domainValues.size()) {
			
			// TODO: why not just use a map rather than PeriodAnalysisDataPoint?
			
			double[] values = new double[coordTypes.length];
			for (int i=0;i<coordTypes.length;i++) {
				values[i] = analysisValues.get(coordTypes[i]).get(item);
			}
					
			dataPoint = new PeriodAnalysisDataPoint(coordTypes, values);
		}

		return dataPoint;
	}

	/**
	 * Force plot to update.
	 */
	public void refresh() {
		fireDatasetChanged();
	}
}
