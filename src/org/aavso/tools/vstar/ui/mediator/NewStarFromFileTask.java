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

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.input.text.ObservationSourceAnalyser;
import org.aavso.tools.vstar.input.text.TextFormatObservationReader;
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.jdesktop.swingworker.SwingWorker;

/**
 * A concurrent task in which a new star from file request task is handled.
 */
public class NewStarFromFileTask extends SwingWorker<Void, Void> {

	private Mediator mediator = Mediator.getInstance();

	private File obsFile;
	private ObservationSourceAnalyser analyser;
	private int plotTaskPortion;

	/**
	 * Constructor.
	 * 
	 * @param obsFile
	 *            The file from which to load the star observations.
	 * @param analyser
	 *            An observation file analyser.
	 * @param plotTaskPortion
	 *            The portion of the total task that involves the light curve
	 *            plot.
	 */
	public NewStarFromFileTask(File obsFile,
			ObservationSourceAnalyser analyser, int plotTaskPortion) {
		this.obsFile = obsFile;
		this.analyser = analyser;
		this.plotTaskPortion = plotTaskPortion;
	}

	/**
	 * Main task. Executed in background thread.
	 */
	public Void doInBackground() {
		createFileBasedObservationArtefacts(obsFile, analyser);
		return null;
	}

	/**
	 * Create observation table and plot models from a file.
	 * 
	 * @param obsFile
	 *            The file from which to load the star observations.
	 * @param analyser
	 *            An observation file analyser.
	 */
	protected void createFileBasedObservationArtefacts(File obsFile,
			ObservationSourceAnalyser analyser) {

		try {
			AbstractObservationRetriever textFormatReader = new TextFormatObservationReader(
					new LineNumberReader(new FileReader(obsFile.getPath())),
					analyser);

			textFormatReader.retrieveObservations();

			if (textFormatReader.getValidObservations().isEmpty()) {
				throw new ObservationReadError(
						"No observations for the specified period or error in observation source.");
			}

			mediator.createNewStarObservationArtefacts(analyser
					.getNewStarType(), new StarInfo(obsFile.getName()),
					textFormatReader, plotTaskPortion);

		} catch (Throwable t) {
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					"New Star From File Read Error", t);
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