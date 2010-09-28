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

import javax.swing.SwingWorker;

import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;

/**
 * A concurrent task in which a new star from observation source plug-in request
 * is handled.
 */
public class NewStarFromObSourcePluginTask extends SwingWorker<Void, Void> {

	private Mediator mediator = Mediator.getInstance();

	private ObservationSourcePluginBase obSourcePlugin;

	/**
	 * Constructor.
	 * 
	 * @param obSourcePlugin
	 *            The plugin that will be used to obtain observations.
	 */
	public NewStarFromObSourcePluginTask(
			ObservationSourcePluginBase obSourcePlugin) {
		this.obSourcePlugin = obSourcePlugin;
	}

	/**
	 * Main task. Executed in background thread.
	 */
	public Void doInBackground() {
		createObservationArtefacts();
		return null;
	}

	/**
	 * Create observation table and plot models from an observation source
	 * plug-in.
	 */
	protected void createObservationArtefacts() {

		try {
			AbstractObservationRetriever retriever = this.obSourcePlugin
					.getObservationRetriever();

			retriever.retrieveObservations();

			if (retriever.getValidObservations().isEmpty()) {
				throw new ObservationReadError(
						"No observations for the specified period or error in observation source.");
			}

			NewStarType type = NewStarType.NEW_STAR_FROM_EXTERNAL_SOURCE;
			mediator.createNewStarObservationArtefacts(type, new StarInfo(
					obSourcePlugin.getCurrentStarName()), retriever, 0);

		} catch (Throwable t) {
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					"New Star From Observation Source Read Error", t);

			mediator.getProgressNotifier().notifyListeners(
					ProgressInfo.COMPLETE_PROGRESS);

			mediator.getProgressNotifier().notifyListeners(
					ProgressInfo.CLEAR_PROGRESS);
		}
	}

	/**
	 * Executed in event dispatching thread.
	 */
	public void done() {
		mediator.getProgressNotifier().notifyListeners(
				ProgressInfo.COMPLETE_PROGRESS);

		mediator.getProgressNotifier().notifyListeners(
				ProgressInfo.CLEAR_PROGRESS);

		// TODO: how to detect task cancellation and clean up map etc
	}
}