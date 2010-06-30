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
import java.awt.geom.RectangularShape;

import org.jfree.chart.renderer.xy.XYErrorRenderer;

/**
 * This is VStar's plot data-point renderer.
 * 
 * We subclass XYErrorRenderer in order to be able to plot error bars.
 * TODO: Should we instead use StatisticalLineAndShapeRenderer
 *  (at least for means plot)?
 * 
 * We override DATAPOINT_SHAPE rendering methods.
 */
public class VStarPlotDataRenderer extends XYErrorRenderer {

	private final static RectangularShape DATAPOINT_SHAPE = new Ellipse2D.Double(-2.5, -2.5, 5, 5);		

	/**
	 * @see org.jfree.chart.renderer.AbstractRenderer#getItemShape(int, int)
	 */
	public Shape getItemShape(int row, int column) {
		return DATAPOINT_SHAPE;
	}

	/**
	 * @see org.jfree.chart.renderer.AbstractRenderer#getLegendShape(int)
	 */
	public Shape getLegendShape(int series) {
		return DATAPOINT_SHAPE;
	}
}
