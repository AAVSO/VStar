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
package org.aavso.tools.vstar.ui.mediator;

import java.util.Map;

import javax.swing.SwingWorker;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;

/**
 * A concurrent task in which a phase plot operation is executed.
 */
public class PhasePlotTask extends SwingWorker<Void, Void> {

	private double period;
	private double epoch;
	private Map<SeriesType, Boolean> seriesVisibilityMap;

	/**
	 * Constructor
	 * 
	 * @param period
	 *            The requested period of the phase plot.
	 * @param epoch
	 *            The epoch (first Julian Date) for the phase plot.
	 * @param seriesVisibilityMap
	 *            A mapping from series number to visibility status.
	 */
	public PhasePlotTask(double period, double epoch,
			Map<SeriesType, Boolean> seriesVisibilityMap) {
		super();
		this.period = period;
		this.epoch = epoch;
		this.seriesVisibilityMap = seriesVisibilityMap;
	}

	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	protected Void doInBackground() throws Exception {
		Mediator.getInstance().getProgressNotifier().notifyListeners(
				ProgressInfo.START_PROGRESS);
		Mediator.getInstance().getProgressNotifier().notifyListeners(
				ProgressInfo.BUSY_PROGRESS);

		Mediator.getInstance().createPhasePlotArtefacts(period, epoch,
				seriesVisibilityMap);
		
		return null;
	}

	/**
	 * Executed in event dispatching thread.
	 */
	public void done() {
		Mediator.getInstance().setAnalysisType(AnalysisType.PHASE_PLOT);
		
		Mediator.getInstance().setPhasePlotStatusMessage();

		Mediator.getInstance().getProgressNotifier().notifyListeners(
				ProgressInfo.COMPLETE_PROGRESS);

		Mediator.getInstance().getProgressNotifier().notifyListeners(
				ProgressInfo.CLEAR_PROGRESS);
	}
}
