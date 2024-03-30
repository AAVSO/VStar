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

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.SwingWorker;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationSinkPluginBase;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;

// C. Kotnik 2018-12-16
// Exclude excluded observations from the output file
/**
 * A concurrent task in which an observation list file save operation takes
 * place.
 */
public class ObsListFileSaveTask extends SwingWorker<Void, Void> {

	private ObservationSinkPluginBase plugin;
	private List<ValidObservation> observations;
	private File outFile;
	private String delimiter;
	private String error;

	/**
	 * Constructor.
	 * 
	 * @param plugin
	 *            The observation sink plugin.
	 * @param observations
	 *            Observation list.
	 * @param outFile
	 *            Output file.
	 * @param delimiter
	 *            The field delimiter to use.
	 */
	public ObsListFileSaveTask(ObservationSinkPluginBase plugin,
			List<ValidObservation> observations, File outFile, String delimiter) {
		super();
		this.plugin = plugin;
		this.observations = observations;
		this.outFile = outFile;
		this.delimiter = delimiter;
	}

	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	protected Void doInBackground() throws Exception {
		
	    error = null;
	    
	    Mediator.getInstance().getProgressNotifier().notifyListeners(
                ProgressInfo.BUSY_PROGRESS);

		try (PrintWriter writer = new PrintWriter(outFile)) {
			plugin.save(writer, observations, delimiter);
        } catch (Exception ex) {
        	error = ex.getLocalizedMessage();
        }
		
		return null;
	}

	/**
	 * Executed in event dispatching thread.
	 */
	public void done() {
		if (error != null) {
            MessageBox.showErrorDialog("Observation File Save Error", error);
		}
		
		Mediator.getInstance().getProgressNotifier()
				.notifyListeners(ProgressInfo.COMPLETE_PROGRESS);

		Mediator.getUI().getStatusPane()
				.setMessage("Saved '" + outFile.getAbsolutePath() + "'");

		Mediator.getInstance().getProgressNotifier()
				.notifyListeners(ProgressInfo.CLEAR_PROGRESS);
	}
}
