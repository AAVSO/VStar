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
package org.aavso.tools.vstar.ui.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.IDateAndMagSource;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.AbstractXYDataset;

/**
 * This class is a model that represents a series of valid variable star
 * observations from disparate sources, e.g. files, AAVSO database. This
 * corresponds to a light curve (TODO: rename to LightCurvePlotModel?)
 */
public class ObservationPlotModel extends AbstractXYDataset {

	// A unique series number for this model.
	private int seriesNum;

	/**
	 * A mapping from series number to a list of observations where each such
	 * list is a data series.
	 */
	private Map<Integer, List<? extends IDateAndMagSource>> seriesNumToObSrcListMap;

	/**
	 * A mapping from series number to source name.
	 */
	private Map<Integer, String> seriesNumToSrcNameMap;

	/**
	 * Constructor
	 */
	public ObservationPlotModel() {
		super();
		this.seriesNum = 0;
		this.seriesNumToSrcNameMap = new HashMap<Integer, String>();
		this.seriesNumToObSrcListMap = new HashMap<Integer, List<? extends IDateAndMagSource>>();
	}

	public <T extends IDateAndMagSource> ObservationPlotModel(String name, List<T> obsSourceList) {
		this();
		this.addObservationSeries(name, obsSourceList);
	}

	/**
	 * Add an observation series.
	 * 
	 * @param name
	 *            A name to be associated with the data source.
	 * @param obSourceList
	 *            A series (list) of observations, in particular, magnitude and
	 *            Julian Day.
	 * @postcondition Both seriesNumToObSrcListMap and seriesNumToSrcNameMap
	 *                must be the same length.
	 */
	public <T extends IDateAndMagSource> void addObservationSeries(String name,
			List<T> obSourceList) {
		
		int seriesNum = this.getNextSeriesNum();
		
		this.seriesNumToObSrcListMap.put(seriesNum, obSourceList);
		this.seriesNumToSrcNameMap.put(seriesNum, name);
		
		assert (this.seriesNumToObSrcListMap.size() == this.seriesNumToSrcNameMap
				.size());
		
		this.fireDatasetChanged();
	}

	/**
	 * @see org.jfree.data.general.AbstractSeriesDataset#getSeriesCount()
	 * @param Return
	 *            the number of observation series that exist on the plot.
	 */
	public int getSeriesCount() {
		return this.seriesNumToObSrcListMap.size();
	}

	/**
	 * @see org.jfree.data.general.AbstractSeriesDataset#getSeriesKey(int)
	 */
	public Comparable getSeriesKey(int series) {
		if (series >= this.seriesNumToSrcNameMap.size()) {
			throw new IllegalArgumentException("Series number '" + series
					+ "' out of range.");
		}

		return this.seriesNumToSrcNameMap.get(series);
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getItemCount(int)
	 * @return The number of observations (items) in the requested series.
	 */
	public int getItemCount(int series) {
		if (series >= this.seriesNumToSrcNameMap.size()) {
			throw new IllegalArgumentException("Series number '" + series
					+ "' out of range.");
		}

		return this.seriesNumToObSrcListMap.get(series).size();
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getX(int, int)
	 */
	public Number getX(int series, int item) {
		if (series >= this.seriesNumToObSrcListMap.size()) {
			throw new IllegalArgumentException("Series number '" + series
					+ "' out of range.");
		}

		if (item >= this.seriesNumToObSrcListMap.get(series).size()) {
			throw new IllegalArgumentException("Item number '" + item
					+ "' out of range.");
		}

		return this.seriesNumToObSrcListMap.get(series).get(item).getDateInfo()
				.getJulianDay();
	}

	/**
	 * @see org.jfree.data.xy.XYDataset#getY(int, int)
	 */
	public Number getY(int series, int item) {
		if (series >= this.seriesNumToObSrcListMap.size()) {
			throw new IllegalArgumentException("Series number '" + series
					+ "' out of range.");
		}

		if (item >= this.seriesNumToObSrcListMap.get(series).size()) {
			throw new IllegalArgumentException("Item number '" + item
					+ "' out of range.");
		}

		return this.seriesNumToObSrcListMap.get(series).get(item)
				.getMagnitude().getMagValue();
	}

	/**
	 * @see org.jfree.data.xy.AbstractXYDataset#getDomainOrder()
	 */
	public DomainOrder getDomainOrder() {
		return DomainOrder.ASCENDING;
	}

	// Helpers

	private int getNextSeriesNum() {
		return seriesNum++;
	}
}
