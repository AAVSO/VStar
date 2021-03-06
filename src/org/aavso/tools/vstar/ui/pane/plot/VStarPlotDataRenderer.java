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
package org.aavso.tools.vstar.ui.pane.plot;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.jfree.chart.LegendItem;
import org.jfree.chart.renderer.xy.XYErrorRenderer;

/**
 * This is VStar's plot data-point renderer.
 * 
 * We subclass XYErrorRenderer in order to be able to plot error bars.<br/>
 * TODO: Should we instead use StatisticalLineAndShapeRenderer (at least for
 * means plot)?
 */
@SuppressWarnings("serial")
public class VStarPlotDataRenderer extends XYErrorRenderer {

	/**
	 * Set the size of the series.
	 * 
	 * @param seriesNum
	 *            The series number.
	 * @param size
	 *            The size to set each plot point to for this series.
	 */
	public void setSeriesSize(int seriesNum, int size) {
		Shape datapointShape = new Ellipse2D.Double(-size / 2, -size / 2, size,
				size);
		setSeriesShape(seriesNum, datapointShape);
	}

	/* (non-Javadoc)
	 * @see org.jfree.chart.renderer.xy.XYLineAndShapeRenderer#getLegendItem(int, int)
	 */
	@Override
	public LegendItem getLegendItem(int dataSetIndex, int seriesNum) {
		// Only show a legend item for a series for one dataset.
		// In the case of the light curve plot, there is only one,
		// whereas in the case of the phase plot there are two.
		LegendItem item = null;

		if (dataSetIndex == 0) {
			item = super.getLegendItem(dataSetIndex, seriesNum);
		}
		
		return item;
	}
}
