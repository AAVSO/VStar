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

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.message.PolynomialFitMessage;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.util.polyfit.IPolynomialFitter;

/**
 * A concurrent task in which a potentially long-running polynomial fit is
 * executed.
 */
public class PolynomialFitTask extends SwingWorker<Void, Void> {

	private boolean error;
	private IPolynomialFitter polynomialFitter;

	/**
	 * Constructor
	 * 
	 * @param polynomialFitter
	 *            The polynomial fitter to execute.
	 */
	public PolynomialFitTask(IPolynomialFitter polynomialFitter) {
		error = false;
		this.polynomialFitter = polynomialFitter;
	}

	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	protected Void doInBackground() throws Exception {
		MainFrame.getInstance().getStatusPane().setMessage(
				"Performing Polynomial Fit...");
		try {
			polynomialFitter.execute();
		} catch (Throwable t) {
			error = true;
			MessageBox.showErrorDialog("Polynomial Fit Error", t);
		}

		MainFrame.getInstance().getStatusPane().setMessage("");
		return null;
	}

	/**
	 * Executed in event dispatching thread.
	 */
	public void done() {
		if (!error && !isCancelled()) {
			PolynomialFitMessage msg = new PolynomialFitMessage(this,
					polynomialFitter);
			Mediator.getInstance().getPolynomialFitNofitier().notifyListeners(
					msg);
		}

		Mediator.getInstance().getProgressNotifier().notifyListeners(
				ProgressInfo.COMPLETE_PROGRESS);

		Mediator.getInstance().getProgressNotifier().notifyListeners(
				ProgressInfo.CLEAR_PROGRESS);
	}
}
