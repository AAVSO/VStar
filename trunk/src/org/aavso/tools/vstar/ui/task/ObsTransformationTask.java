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

import javax.swing.SwingWorker;

import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.ui.mediator.message.StopRequestMessage;
import org.aavso.tools.vstar.ui.mediator.message.UndoRedoType;
import org.aavso.tools.vstar.ui.undo.IUndoableAction;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * A concurrent task in which a potentially long-running observation task is
 * executed.
 */
public class ObsTransformationTask extends SwingWorker<Void, Void> {

	private IUndoableAction action;

	/**
	 * Constructor
	 */
	public ObsTransformationTask(IUndoableAction action) {
		this.action = action;
	}

	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	protected Void doInBackground() throws Exception {

		Mediator.getUI().getStatusPane()
				.setMessage("Performing " + action.getDisplayString() + "...");
		try {
			// Reset undo/redo actions of this type...
			Mediator.getInstance().getUndoableActionManager()
					.clearPendingAction(action.getDisplayString());

			// Execute a (re)do action...
			action.prepare(UndoRedoType.REDO);
			action.execute();
			Mediator.getInstance().updatePlotsAndTables();

			// ...and add an undo operation.
			Mediator.getInstance().getUndoableActionManager()
					.addAction(action, UndoRedoType.UNDO);
		} catch (Throwable t) {
			MessageBox.showErrorDialog(action.getDisplayString() + " Error", t);
		} finally {
			Mediator.getUI().getStatusPane().setMessage("");
		}

		return null;
	}

	/**
	 * Executed in event dispatching thread.
	 */
	public void done() {
		Mediator.getInstance().getProgressNotifier()
				.notifyListeners(ProgressInfo.COMPLETE_PROGRESS);

		Mediator.getInstance().getProgressNotifier()
				.notifyListeners(ProgressInfo.CLEAR_PROGRESS);
	}
}
