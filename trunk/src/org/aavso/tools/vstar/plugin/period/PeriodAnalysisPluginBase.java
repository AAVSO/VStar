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
import org.aavso.tools.vstar.plugin.IPlugin;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodChangeMessage;
import org.aavso.tools.vstar.ui.resources.LoginInfo;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.notification.Notifier;

/**
 * <p>
 * This is the abstract base class for all period analysis plugin classes.
 * </p>
 * 
 * <p>
 * Period analysis plugins will appear in VStar's Analysis menu when its jar
 * file is placed into the vstar_plugins directory.
 * </p>
 * 
 * @see org.aavso.tools.vstar.plugin.IPlugin
 */

abstract public class PeriodAnalysisPluginBase implements IPlugin {
	
	/**
	 * Send a period change message.
	 * 
	 * @param period
	 *            The period to be sent in the notification. This will cause a
	 *            phase plot dialog to be invoked, asking for confirmation of
	 *            the new period (and epoch) before creating a new phase plot.
	 */
	public void sendPeriodChangeMessage(double period) {
		Mediator.getInstance().getPeriodChangeNotifier().notifyListeners(
				new PeriodChangeMessage(this, period));
	}

	// ** Methods that must be implemented by concrete plugin subclasses. **

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDisplayName()
	 */
	@Override
	abstract public String getDisplayName();

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDescription()
	 */
	@Override
	abstract public String getDescription();

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getGroup()
	 */
	@Override
	public String getGroup() {
		return "Period Analysis";
	}

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
	 * Interrupt the execution of the algorithm.
	 */
	abstract public void interrupt();

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
		return Mediator.getInstance().getPeriodChangeNotifier();
	}

	/**
	 * @return the periodAnalysisSelectionNotifier
	 */
	protected Notifier<PeriodAnalysisSelectionMessage> getPeriodAnalysisSelectionNotifier() {
		return Mediator.getInstance().getPeriodAnalysisSelectionNotifier();
	}
	
	// ** Internal helper methods. **

	/**
	 * Get the new star listener for this plugin.
	 */
	protected Listener<NewStarMessage> getNewStarListener() {
		return new Listener<NewStarMessage>() {
			public void update(NewStarMessage info) {
				newStarAction(info);
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#requiresAuthentication()
	 */
	@Override
	public boolean requiresAuthentication() {
		return false;
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#additionalAuthenticationSatisfied(org.aavso.tools.vstar.ui.resources.LoginInfo)
	 */
	@Override
	public boolean additionalAuthenticationSatisfied(LoginInfo loginInfo) {
		return true;
	}
}
