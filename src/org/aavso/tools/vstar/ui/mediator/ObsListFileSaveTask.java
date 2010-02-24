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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.MainFrame;
import org.jdesktop.swingworker.SwingWorker;

/**
 * A concurrent task in which an observation list file save operation takes
 * place.
 */
public class ObsListFileSaveTask extends SwingWorker<Void, Void> {

	private List<ValidObservation> observations;
	private File outFile;
	private NewStarType newStarType;

	/**
	 * Constructor.
	 * 
	 * @param observations
	 *            Observation list.
	 * @param outFile
	 *            Output file.
	 * @param newStarType
	 *            The originating new-star type.
	 */
	public ObsListFileSaveTask(List<ValidObservation> observations,
			File outFile, NewStarType newStarType) {
		super();
		this.observations = observations;
		this.outFile = outFile;
		this.newStarType = newStarType;
	}

	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	protected Void doInBackground() throws Exception {

		BufferedOutputStream bufStream = null;

		try {
			bufStream = new BufferedOutputStream(new FileOutputStream(outFile));

			if (this.newStarType == NewStarType.NEW_STAR_FROM_SIMPLE_FILE) {
				saveObsToFileInSimpleFormat(bufStream);
			} else {
				saveObsToFileInAAVSOFormat(bufStream);
			}
		} finally {
			if (bufStream != null) {
				bufStream.close();
			}
		}

		return null;
	}

	/**
	 * Write observations in simple format to specified output stream. Also
	 * updates the progress bar upon each observation written.
	 * 
	 * @param ostream
	 *            The specified buffered output stream.
	 */
	private void saveObsToFileInSimpleFormat(BufferedOutputStream ostream)
			throws IOException {
		for (ValidObservation ob : this.observations) {
			ostream.write(ob.toSimpleFormatString().getBytes());

			Mediator.getInstance().getProgressNotifier().notifyListeners(
					ProgressInfo.INCREMENT_PROGRESS);
		}
	}

	/**
	 * Write observations in AAVSO format to specified output stream. Also
	 * updates the progress bar upon each observation written.
	 * 
	 * @param ostream
	 *            The specified buffered output stream.
	 */
	private void saveObsToFileInAAVSOFormat(BufferedOutputStream ostream)
			throws IOException {
		for (ValidObservation ob : this.observations) {
			ostream.write(ob.toAAVSOFormatString().getBytes());
			Mediator.getInstance().getProgressNotifier().notifyListeners(
					ProgressInfo.INCREMENT_PROGRESS);
		}
	}

	/**
	 * Executed in event dispatching thread.
	 */
	public void done() {
		Mediator.getInstance().getProgressNotifier().notifyListeners(
				ProgressInfo.COMPLETE_PROGRESS);

		MainFrame.getInstance().getStatusPane().setMessage(
				"Saved '" + outFile.getAbsolutePath() + "'");

		// TODO: how to detect task cancellation and clean up map etc
	}
}
