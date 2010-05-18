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
package org.aavso.tools.vstar.plugin;

import java.util.List;

import javax.swing.JDialog;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.MeanSourceSeriesChangeMessage;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.PeriodChangeMessage;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.notification.Notifier;

/**
 * This is the abstract base class for all period analysis plugin classes.
 * 
 * Note: plugins will have to be licensed under AGPL because they will use some
 * VStar classes!
 */
abstract public class PeriodAnalysisPluginBase {

	protected Mediator mediator = Mediator.getInstance();

	/**
	 * Parameterless constructor for creation within Mediator.
	 */
	public PeriodAnalysisPluginBase() {
		mediator.getNewStarNotifier().addListener(this.getNewStarListener());

		mediator.getMeanSourceSeriesChangeNotifier().addListener(
				this.getMeanSourceSeriesChangeListener());
	}

	// ** Methods that must be implemented by concrete plugin subclasses. **

	/**
	 * Get the human-readable display name for this plugin, e.g. for a period
	 * analysis menu item.
	 */
	abstract public String getDisplayName();

	/**
	 * Get a description of this plugin.
	 */
	abstract public String getDescription();

	/**
	 * Execute a period analysis algorithm instance for this plugin to
	 * be applied to the specified observations. 
	 */
	abstract public void executeAlgorithm(List<ValidObservation> obs);

	/**
	 * Get the period analysis dialog for this plugin.
	 * 
	 * @param sourceSeriesType
	 *            The mean source series type to be used on the plot for display
	 *            purposes.
	 *            
	 * TODO: may need an overloaded method here since some period analysis plugins
	 * may self-determine the period analysis source type.  
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
	 * When the mean source series changes is loaded, a plugin may want to
	 * discard previous computation results and GUI components, so the plugin
	 * will listen for such messages. Of course, a plugin will only care about
	 * such messages if the period analysis computations are based upon the
	 * current mean series.
	 * 
	 * Note: some period analysis plugins may be uninterested in this message. 
	 * 
	 * @param The
	 *            mean source series change message.
	 */
	abstract protected void meanSourceSeriesChangeAction(
			MeanSourceSeriesChangeMessage message);

	// ** Methods for use by subclasses. **

	/**
	 * @return the periodChangeNotifier
	 */
	protected Notifier<PeriodChangeMessage> getPeriodChangeNotifier() {
		return mediator.getPeriodChangeMessageNotifier();
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

	/**
	 * Get the mean observation change listener for this plugin.
	 */
	private Listener<MeanSourceSeriesChangeMessage> getMeanSourceSeriesChangeListener() {
		return new Listener<MeanSourceSeriesChangeMessage>() {
			public void update(MeanSourceSeriesChangeMessage info) {
				meanSourceSeriesChangeAction(info);
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}
}
