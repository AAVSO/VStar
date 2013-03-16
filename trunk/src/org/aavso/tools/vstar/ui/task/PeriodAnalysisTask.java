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
package org.aavso.tools.vstar.ui.task;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.SwingWorker;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.ui.mediator.message.StopRequestMessage;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * A concurrent task in which a potentially long-running period analysis plugin
 * is executed.
 */
public class PeriodAnalysisTask extends SwingWorker<Void, Void> {

	private PeriodAnalysisPluginBase periodAnalysisPlugin;
	private SeriesType sourceSeriesType;
	private List<ValidObservation> obs;

	private Listener<StopRequestMessage> stopListener;

	private boolean successful;

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
		this.successful = true;

		stopListener = createStopRequestListener();
	}

	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	protected Void doInBackground() throws Exception {

		Mediator.getInstance().getStopRequestNotifier().addListener(
				stopListener);

		Mediator.getUI().getStatusPane().setMessage(
				LocaleProps.get("STATUS_PANE_PERFORMING_PERIOD_ANALYSIS"));
		try {
			periodAnalysisPlugin.executeAlgorithm(obs);
		} catch (CancellationException e) {
			successful = false;
		} catch (Throwable t) {
			successful = false;
			MessageBox.showErrorDialog("Period Analysis Error", t);
		} finally {
			Mediator.getInstance().getStopRequestNotifier()
					.removeListenerIfWilling(stopListener);
		}

		return null;
	}

	/**
	 * Executed in event dispatching thread.
	 */
	public void done() {
		if (!isCancelled() && successful) {
			JDialog dialog = periodAnalysisPlugin.getDialog(sourceSeriesType);
			dialog.setVisible(true);
		}

		Mediator.getInstance().getProgressNotifier().notifyListeners(
				ProgressInfo.COMPLETE_PROGRESS);

		Mediator.getInstance().getProgressNotifier().notifyListeners(
				ProgressInfo.CLEAR_PROGRESS);

		Mediator.getUI().getStatusPane().setMessage("");
	}

	// Creates a stop request listener to interrupt the period analysis.
	private Listener<StopRequestMessage> createStopRequestListener() {
		return new Listener<StopRequestMessage>() {
			@Override
			public void update(StopRequestMessage info) {
				periodAnalysisPlugin.interrupt();
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}
}
