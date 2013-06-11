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

import org.aavso.tools.vstar.util.model.IModel;
import org.jfree.data.xy.AbstractXYDataset;

/**
 * This class represents a continuous plot of a Model series.
 */
@SuppressWarnings("serial")
public class ContinuousModelPlotModel extends AbstractXYDataset {

	private ICoordSource coordSrc;
	private IModel model;
	
	// TODO:
	// 1. May be able to pass this to ObservationPlotPane and PhasePlotPane to create a
	// model overlay in a similar way to how we already do this in PhasePlotPane for
	// prev and curr cycle.
	// 2. In obs plot model, may need to pre-allocate or share 2 model series for use
	//    in same chart.
	// 3. Need to set line renderer.
	
	@Override
	public int getSeriesCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Comparable getSeriesKey(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getItemCount(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Number getX(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Number getY(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}
}
