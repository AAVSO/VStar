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

import org.jfree.data.xy.AbstractXYDataset;

/**
 * This class is the basis of all 2D period analysis plot models.
 * 
 * TODO: may later need a PlotType enum for 2D XY, 3D XY etc
 */
public class PeriodAnalysis2DPlotModel extends AbstractXYDataset {

	// private Map<PeriodAnalysisCoordinateType, List<Double>> coordinates;

	private List<Double> frequencies;
	private List<Double> dependents;
	private String dependentDesc;
	
	/**
	 * Constructor
	 * 
	 * @param frequencies
	 *            A list of frequencies (domain values).
	 * @param dependents
	 *            A list of values that are dependent upon the frequencies
	 *            (range values).
	 */
	public PeriodAnalysis2DPlotModel(List<Double> frequencies,
			List<Double> dependents, String dependentDesc) {
		super();
		assert frequencies.size() == dependents.size();
		this.frequencies = frequencies;
		this.dependents = dependents;
		this.dependentDesc = dependentDesc;
	}

	/**
	 * @return the frequencies
	 */
	public List<Double> getFrequencies() {
		return frequencies;
	}

	/**
	 * @return the dependents
	 */
	public List<Double> getDependents() {
		return dependents;
	}

	/**
	 * @return the dependentDesc
	 */
	public String getDependentDesc() {
		return dependentDesc;
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
		return dependentDesc;
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getItemCount(int)
	 */
	public int getItemCount(int series) {
		return frequencies.size();
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getX(int, int)
	 */
	public Number getX(int series, int item) {
		return frequencies.get(item);
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getY(int, int)
	 */
	public Number getY(int series, int item) {
		return dependents.get(item);
	}
}
