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
import org.jfree.data.xy.AbstractXYDataset;

/**
 * This class is the basis of all 2D period analysis plot models. It provides
 * for the raw data plot along with the 
 */
public class PeriodAnalysis2DPlotModel extends AbstractXYDataset {

	private List<Double> domainValues;
	private List<Double> rangeValues;
	private PeriodAnalysisCoordinateType domainType;
	private PeriodAnalysisCoordinateType rangeType;

	private Map<PeriodAnalysisCoordinateType, List<Double>> topHits;

	/**
	 * Constructor
	 * 
	 * @param domainValues
	 *            A list of domainValues (domain values).
	 * @param rangeValues
	 *            A list of values that are dependent upon the domainValues
	 *            (range values).
	 * @param domainType
	 *            The type of the domain axis.
	 * @param rangeTypes
	 *            The type of the range axis.
	 */
	public PeriodAnalysis2DPlotModel(List<Double> domainValues,
			List<Double> rangeValues, PeriodAnalysisCoordinateType domainType,
			PeriodAnalysisCoordinateType rangeType) {
		super();
		assert domainValues.size() == rangeValues.size();
		this.domainValues = domainValues;
		this.rangeValues = rangeValues;
		this.domainType = domainType;
		this.rangeType = rangeType;
		topHits = null;
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
		int count = 1;
		if (topHits != null)
			count += 1;
		return count;
	}

	/**
	 * @see org.jfree.data.general.AbstractSeriesDataset#getSeriesKey(int)
	 */
	public Comparable getSeriesKey(int series) {
		String key = null;

		if (series == 0) {
			key = rangeType.getDescription() + " vs "
					+ domainType.getDescription();
		} else {
			assert topHits != null;
			key = "top hit";
		}

		return key;
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getItemCount(int)
	 */
	public int getItemCount(int series) {
		int count;

		if (series == 0) {
			count = domainValues.size();
		} else {
			assert topHits != null;
			count = topHits.get(PeriodAnalysisCoordinateType.POWER).size();
		}

		return count;
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getX(int, int)
	 */
	public Number getX(int series, int item) {
		Double x;

		if (series == 0) {
			x = domainValues.get(item);
		} else {
			assert topHits != null;
			x = topHits.get(getDomainType()).get(item);
		}

		return x;
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getY(int, int)
	 */
	public Number getY(int series, int item) {
		Double y;

		if (series == 0) {
			y = rangeValues.get(item);
		} else {
			assert topHits != null;
			y = topHits.get(getRangeType()).get(item);
		}

		return y;
	}

	/**
	 * @param topHits
	 *            the topHits to set
	 */
	public void setTopHits(
			Map<PeriodAnalysisCoordinateType, List<Double>> topHits) {
		this.topHits = topHits;
		fireDatasetChanged();
	}
	
	public void  setData(Map<PeriodAnalysisCoordinateType, List<Double>> data) {
		domainValues = data.get(getDomainType());
		rangeValues = data.get(getRangeType());
		fireDatasetChanged();
	}
}
