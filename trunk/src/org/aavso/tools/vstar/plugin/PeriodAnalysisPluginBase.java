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

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.IPeriodAnalysisAlgorithm;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.PeriodChangeMessage;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.notification.Notifier;

/**
 * This is the abstract base class for all period analysis plugin classes.
 * Each plugin is a Singleton.
 * 
 * Note: plugins will have to be GPL (even AGPL?) because they will use some 
 * VStar classes!
 */
abstract public class PeriodAnalysisPluginBase {
	
	// Enables notification to consumers of period change.
	private Notifier<PeriodChangeMessage> periodChangeNotifier;

	// Enables notification to consumers of period analysis selection.
	private Notifier<PeriodAnalysisSelectionMessage> periodAnalysisSelectionNotifier;

	private static PeriodAnalysisPluginBase instance = null;
	
	/**
	 * Protected constructor for Singleton.
	 */
	protected PeriodAnalysisPluginBase() {
		instance = this;
		
		this.periodAnalysisSelectionNotifier = Mediator.getInstance().getPeriodAnalysisSelectionNotifier();
		this.periodChangeNotifier = Mediator.getInstance().getPeriodChangeMessageNotifier();
		
		Mediator.getInstance().getNewStarNotifier().addListener(this.getNewStarListener());
	}

	/**
	 * Return the Singleton instance.
	 */
	public static PeriodAnalysisPluginBase getInstance() {
		return instance;
	}
	
	// TODO:
	// - Put existing dcdft in map without creating a jar; test that separately
	
	/**
	 * @return the periodChangeNotifier
	 */
	protected Notifier<PeriodChangeMessage> getPeriodChangeNotifier() {
		return periodChangeNotifier;
	}

	/**
	 * @return the periodAnalysisSelectionNotifier
	 */
	protected Notifier<PeriodAnalysisSelectionMessage> getPeriodAnalysisSelectionNotifier() {
		return periodAnalysisSelectionNotifier;
	}

	/**
	 * Get the period analysis algorithm for this plugin.
	 */
	abstract public IPeriodAnalysisAlgorithm getAlgorithm();
	
	/**
	 * Get the period analysis dialog for this plugin.
	 */ 
	abstract public JDialog getDialog();
	
	/**
	 * Get the new star listener for this plugin.
	 * 
	 * When a new dataset is loaded, previous computation results and GUI
	 * components should be discarded, so the plugin will listen for such
	 * messages.
	 */
	abstract protected Listener<NewStarMessage> getNewStarListener();

	/**
	 * Get the mean observation change listener for this plugin.
	 * 
	 * The plugin itself cannot register this listener. It must be done
	 * at new dataset load time, either by Mediator or via a new-star or
	 * analysis change message. TODO: which?
	 * 
	 * This should do the kind of thing Mediator.createMeanObsChangeListener()
	 * does now. Can we create some base class functionality here? Either that,
	 * or we really do leave that method in Mediator as the only registered listener
	 * and it just calls reset() on this class? Or better yet, have Mediator generate
	 * yet another mean
	 */
	abstract public Listener<List<ValidObservation>> getMeanObsChangeListener();
	
	/**
	 * This method should discard the result of previous computations, 
	 * previously created dialogs or whatever makes sense for the plugin
	 * when a new dataset is loaded or a mean series changes etc. 
	 */
	abstract public void reset();
}
