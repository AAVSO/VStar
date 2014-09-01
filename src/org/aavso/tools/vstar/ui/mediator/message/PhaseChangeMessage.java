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
package org.aavso.tools.vstar.ui.mediator.message;

import java.util.Map;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This message is sent when a phase change (period, epoch) occurs.
 */
public class PhaseChangeMessage extends MessageBase {

	private double period;
	private double epoch;
	private Map<SeriesType, Boolean> seriesVisibilityMap;

	/**
	 * Constructor.
	 * 
	 * @param source
	 *            The source of this message.
	 * @param period
	 *            The period associated with the phase change.
	 * @param epoch
	 *            The epoch associated with the phase change.
	 * @param seriesVisibilityMap
	 *            A mapping from series type to series visibility. This records
	 *            the series that were visible at the time of the phase change.
	 */
	public PhaseChangeMessage(Object source, double period, double epoch,
			Map<SeriesType, Boolean> seriesVisibilityMap) {
		super(source);
		this.period = period;
		this.epoch = epoch;
		this.seriesVisibilityMap = seriesVisibilityMap;
	}

	/**
	 * @return the period
	 */
	public double getPeriod() {
		return period;
	}

	/**
	 * @rturn the epoch
	 */
	public double getEpoch() {
		return epoch;
	}

	/**
	 * @return the seriesVisibilityMap
	 */
	public Map<SeriesType, Boolean> getSeriesVisibilityMap() {
		return seriesVisibilityMap;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();

		buf.append("period: " + NumericPrecisionPrefs.formatOther(period));
		buf.append(", epoch: " + NumericPrecisionPrefs.formatTime(epoch));
		buf.append(", series: ");
		for (SeriesType series : seriesVisibilityMap.keySet()) {
			if (seriesVisibilityMap.containsKey(series)
					&& seriesVisibilityMap.get(series)) {
				buf.append(series.getShortName());
				buf.append(" ");
			}
		}

		return buf.toString();
	}
}
