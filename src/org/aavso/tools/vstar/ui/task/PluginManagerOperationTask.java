/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2013  AAVSO (http://www.aavso.org/)
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

import javax.swing.SwingWorker;

import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.plugin.manager.PluginManagementOperation;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.ui.mediator.message.StopRequestMessage;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * A concurrent task in which a potentially long-running plugin management
 * operation is executed.
 */
public class PluginManagerOperationTask extends SwingWorker<Void, Void> {

	private PluginManagementOperation op;

	private Listener<StopRequestMessage> stopListener;

	/**
	 * Constructor
	 * 
	 * @param manager
	 *            The plugin manager.
	 * @param message
	 *            The message to display. on the status bar.
	 */
	public PluginManagerOperationTask(PluginManagementOperation op) {
		this.op = op;
		stopListener = createStopRequestListener();
	}

	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	protected Void doInBackground() throws Exception {

		Mediator.getInstance().getProgressNotifier().notifyListeners(
				ProgressInfo.START_PROGRESS);
		Mediator.getInstance().getProgressNotifier().notifyListeners(
				ProgressInfo.BUSY_PROGRESS);

		Mediator.getInstance().getStopRequestNotifier().addListener(
				stopListener);

		Mediator.getUI().getStatusPane().setMessage(op.getMessage() + "...");
		try {
			op.execute();
		} catch (Throwable t) {
			MessageBox.showErrorDialog("Plugin Manager: " + op.getMessage(), t
					.getLocalizedMessage());
		} finally {
			Mediator.getInstance().getStopRequestNotifier()
					.removeListenerIfWilling(stopListener);
		}

		Mediator.getUI().getStatusPane().setMessage("");

		return null;
	}

	/**
	 * Executed in event dispatching thread.
	 */
	public void done() {
		Mediator.getInstance().getProgressNotifier().notifyListeners(
				ProgressInfo.COMPLETE_PROGRESS);

		Mediator.getInstance().getProgressNotifier().notifyListeners(
				ProgressInfo.CLEAR_PROGRESS);
	}

	// Creates a stop request listener to interrupt the operation.
	private Listener<StopRequestMessage> createStopRequestListener() {
		return new Listener<StopRequestMessage>() {
			@Override
			public void update(StopRequestMessage info) {
				op.interrupt();
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}
}
