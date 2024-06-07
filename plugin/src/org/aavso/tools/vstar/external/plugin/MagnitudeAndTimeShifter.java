/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.external.plugin;

import java.util.Set;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationTransformerPluginBase;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.UndoableActionType;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.ui.undo.IUndoableAction;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This plugin allows the time and/or magnitude of a set of observations to be
 * reversibly shifted by a specified amount.
 */
public class MagnitudeAndTimeShifter extends ObservationTransformerPluginBase {

	private double magShift, origMagShift;
	private double timeShift, origTimeShift;
	private boolean firstInvocation;

	public MagnitudeAndTimeShifter() {
		super();
		origTimeShift = timeShift = 0;
		origMagShift = magShift = 0;
		firstInvocation = true;
	}

	@Override
	public String getDisplayName() {
		return "Magnitude and Time Shifter";
	}

	@Override
	public String getDescription() {
		return "Magnitude and Time Shifter";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "Magnitude and Time Shifter Plug-In.pdf";
	}

	@Override
	public IUndoableAction createAction(ISeriesInfoProvider seriesInfo,
			Set<SeriesType> series) {

		if (firstInvocation) {
			Mediator.getInstance().getNewStarNotifier()
					.addListener(getNewStarListener());

			firstInvocation = false;
		}

		return new IUndoableAction() {
			@Override
			public String getDisplayString() {
				return "shifted time/magnitude";
			}

			@Override
			public boolean execute(UndoableActionType type) {
				boolean ok = true;
				switch (type) {
				case DO:
					// For a do operation, invoke the dialog when the action is
					// executed.
					ok = invokeDialog();
					break;
					
				case UNDO:
				case REDO:
					// For an undo or a redo operation, negate the shift.
				    timeShift = -timeShift;
					magShift = -magShift;
					break;
				}

                if (timeShift != 0 || magShift != 0) {
                    for (SeriesType seriesType : series) {
                        for (ValidObservation ob : seriesInfo
                                .getObservations(seriesType)) {
                            if (timeShift != 0) {
                                ob.setJD(ob.getJD() + timeShift);
                            }
                            if (magShift != 0) {
                                ob.setMag(ob.getMag() + magShift);
                            }
						}
					}
				}
				
				return ok;
			}
		};
	}

	/**
	 * Get the new star listener for this plugin.
	 */
	protected Listener<NewStarMessage> getNewStarListener() {
		return new Listener<NewStarMessage>() {
			public void update(NewStarMessage info) {
				origMagShift = magShift = 0;
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * Invoke dialog to request time and magnitude shift values.
	 * 
	 * @return Whether the dialog was dismissed but not cancelled.
	 */
	private boolean invokeDialog() {
		boolean ok = true;

		DoubleField magShiftField = new DoubleField("Magnitude Shift", null, null, origMagShift);
        DoubleField timeShiftField = new DoubleField("Time Shift", null, null, origTimeShift);

        MultiEntryComponentDialog dialog = new MultiEntryComponentDialog(
				"Magnitude and Time Shift", timeShiftField, magShiftField);

		ok = !dialog.isCancelled();

		if (ok) {
			origMagShift = magShift = magShiftField.getValue();
			origTimeShift = timeShift = timeShiftField.getValue();
		}

		return ok;
	}
}
