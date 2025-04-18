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
import org.aavso.tools.vstar.ui.mediator.message.ModelCreationMessage;
import org.aavso.tools.vstar.ui.mediator.message.ModelSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.ui.mediator.message.StopRequestMessage;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.stats.PhaseCalcs;

/**
 * A concurrent task in which a potentially long-running modelling is executed.
 */
public class ModellingTask extends SwingWorker<Void, Void> {

    private String error;
    private IModel model;

    private Listener<StopRequestMessage> stopListener;

    /**
     * Constructor
     * 
     * @param model The model algorithm to execute.
     */
    public ModellingTask(IModel model) {
        this.error = null;
        this.model = model;

        stopListener = createStopRequestListener();
    }

    /**
     * @see javax.swing.SwingWorker#doInBackground()
     */
    protected Void doInBackground() throws Exception {

        Mediator.getInstance().getStopRequestNotifier().addListener(stopListener);

        Mediator.getInstance().getProgressNotifier().notifyListeners(ProgressInfo.START_PROGRESS);

        Mediator.getUI().getStatusPane().setMessage("Performing " + model.getKind() + "...");
        try {
            model.execute();
            if (!model.getFit().isEmpty()) {
                // Is there a phase plot in effect?
                // If so, set the model's phase values accordingly.
                if (Mediator.getInstance().getDocumentManager().phasePlotExists()) {
                    double epoch = Mediator.getInstance().getDocumentManager().getEpoch();
                    double period = Mediator.getInstance().getDocumentManager().getPeriod();
                    PhaseCalcs.setPhases(model.getFit(), epoch, period);
                    PhaseCalcs.setPhases(model.getResiduals(), epoch, period);
                }
            }
        } catch (Throwable t) {
            error = t.getLocalizedMessage();
        } finally {
            Mediator.getInstance().getStopRequestNotifier().removeListenerIfWilling(stopListener);
        }

        Mediator.getUI().getStatusPane().setMessage("");

        return null;
    }

    /**
     * Executed in event dispatching thread.
     */
    public void done() {
        if (!model.getFit().isEmpty()) {
            if (error != null) {
                MessageBox.showErrorDialog(model.getKind() + " Error", error);
            } else if (!isCancelled()) {
                ModelSelectionMessage selectionMsg = new ModelSelectionMessage(this, model);
                Mediator.getInstance().getModelSelectionNofitier().notifyListeners(selectionMsg);

                ModelCreationMessage creationMsg = new ModelCreationMessage(this, model);
                Mediator.getInstance().getModelCreationNotifier().notifyListeners(creationMsg);
            }
        }

        Mediator.getInstance().getProgressNotifier().notifyListeners(ProgressInfo.COMPLETE_PROGRESS);

        Mediator.getInstance().getProgressNotifier().notifyListeners(ProgressInfo.CLEAR_PROGRESS);
    }

    // Creates a stop request listener to interrupt the model creation.
    private Listener<StopRequestMessage> createStopRequestListener() {
        return new Listener<StopRequestMessage>() {
            @Override
            public void update(StopRequestMessage info) {
                model.interrupt();
            }

            @Override
            public boolean canBeRemoved() {
                return true;
            }
        };
    }
}
