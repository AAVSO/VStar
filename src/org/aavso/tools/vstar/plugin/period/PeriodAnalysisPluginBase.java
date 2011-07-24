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
package org.aavso.tools.vstar.plugin.period;

import java.util.List;

import javax.swing.JDialog;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.plugin.PluginBase;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodChangeMessage;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.notification.Notifier;

/**
 * <p>
 * This is the abstract base class for all period analysis plugin classes.
 * </p>
 * 
 * <p>
 * Period analysis plugins will appear in VStar's Analysis -> Period Search menu
 * when its jar file is placed into the vstar_plugins directory.
 * </p>
 * 
 * @see org.aavso.tools.vstar.plugin.PluginBase
 */

// TODO:
// - Add a getter method to say whether or not a series selection dialog
// should be invoked before the algorithm is executed, the SeriesType from
// which would be made available via another getter.

abstract public class PeriodAnalysisPluginBase implements PluginBase {

	protected Mediator mediator = Mediator.getInstance();

	/**
	 * Parameterless constructor for creation within Mediator.
	 */
	public PeriodAnalysisPluginBase() {
		mediator.getNewStarNotifier().addListener(this.getNewStarListener());
	}

	/**
	 * Send a period change message.
	 * 
	 * @param period
	 *            The period to be sent in the notification. This will cause a
	 *            phase plot dialog to be invoked, asking for confirmation of
	 *            the new period (and epoch) before creating a new phase plot.
	 */
	public void sendPeriodChangeMessage(double period) {
		mediator.getPeriodChangeNotifier().notifyListeners(
				new PeriodChangeMessage(this, period));
	}

	// ** Methods that must be implemented by concrete plugin subclasses. **

	/**
	 * @see org.aavso.tools.vstar.plugin.PluginBase#getDisplayName()
	 */
	abstract public String getDisplayName();

	/**
	 * @see org.aavso.tools.vstar.plugin.PluginBase#getDescription()
	 */
	abstract public String getDescription();

	/**
	 * Execute a period analysis algorithm instance for this plugin to be
	 * applied to the specified observations.
	 * 
	 * @param obs
	 *            The observations on which to perform the period analysis.
	 * @throws AlgorithmError
	 *             if an error occurs during period analysis.
	 * @throws CancellationException
	 *             if the operation is cancelled.
	 */
	abstract public void executeAlgorithm(List<ValidObservation> obs)
			throws AlgorithmError, CancellationException;

	/**
	 * Get the period analysis dialog for this plugin.
	 * 
	 * @param sourceSeriesType
	 *            The mean source series type to be used on the plot for display
	 *            purposes.
	 * @return A dialog to be invoked, or null, if there is no dialog.
	 */
	abstract public JDialog getDialog(SeriesType sourceSeriesType);

	/**
	 * When a new dataset is loaded, previous computation results and GUI
	 * components should be discarded, so the plugin will listen for such
	 * messages.
	 * 
	 * @param message
	 *            The new star message.
	 */
	abstract protected void newStarAction(NewStarMessage message);

	/**
	 * Reset the plugin, e.g. clear internal algorithm and dialog objects.
	 */
	abstract public void reset();

	// ** Methods for use by subclasses. **

	/**
	 * @return the periodChangeNotifier
	 */
	protected Notifier<PeriodChangeMessage> getPeriodChangeNotifier() {
		return mediator.getPeriodChangeNotifier();
	}

	/**
	 * @return the periodAnalysisSelectionNotifier
	 */
	protected Notifier<PeriodAnalysisSelectionMessage> getPeriodAnalysisSelectionNotifier() {
		return mediator.getPeriodAnalysisSelectionNotifier();
	}

	// ** Internal helper methods. **

	/**
	 * Get the new star listener for this plugin.
	 */
	private Listener<NewStarMessage> getNewStarListener() {
		return new Listener<NewStarMessage>() {
			public void update(NewStarMessage info) {
				newStarAction(info);
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}
}
