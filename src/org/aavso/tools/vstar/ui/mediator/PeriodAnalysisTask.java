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

import java.util.List;

import javax.swing.JDialog;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.ui.MainFrame;
import org.jdesktop.swingworker.SwingWorker;

/**
 * A concurrent task in which a potentially long-running periodAnalysisPlugin is
 * executed.
 */
public class PeriodAnalysisTask extends SwingWorker<Void, Void> {

	private PeriodAnalysisPluginBase periodAnalysisPlugin;
	private SeriesType sourceSeriesType;
	private List<ValidObservation> obs;

	/**
	 * Constructor
	 * 
	 * @param periodAnalysisPlugin
	 *            The period analysis plugin.
	 * @param sourceSeriesType
	 *            The source series of this analysis run.
	 * @param obs
	 *            The observations to which the period analysis is to be
	 *            applied.
	 */
	public PeriodAnalysisTask(PeriodAnalysisPluginBase plugin,
			SeriesType sourceSeriesType, List<ValidObservation> obs) {
		this.periodAnalysisPlugin = plugin;
		this.sourceSeriesType = sourceSeriesType;
		this.obs = obs;
	}

	/**
	 * @see org.jdesktop.swingworker.SwingWorker#doInBackground()
	 */
	protected Void doInBackground() throws Exception {
		MainFrame.getInstance().getStatusPane().setMessage(
				"Performing Period Analysis...");
		periodAnalysisPlugin.executeAlgorithm(obs);
		MainFrame.getInstance().getStatusPane().setMessage("");
		return null;
	}

	/**
	 * Executed in event dispatching thread.
	 */
	public void done() {
		JDialog dialog = periodAnalysisPlugin.getDialog(sourceSeriesType);
		dialog.setVisible(true);

		Mediator.getInstance().getProgressNotifier().notifyListeners(
				ProgressInfo.COMPLETE_PROGRESS);

		// TODO: how to detect task cancellation?
	}
}
