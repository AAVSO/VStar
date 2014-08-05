/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2012  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.data.filter;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.ui.pane.plot.PhaseAndMeanPlotPane;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.locale.NumberParser;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * A phase field matcher.
 */
public class PhaseFieldMatcher extends DoubleFieldMatcher {

	public PhaseFieldMatcher(Double testValue, ObservationMatcherOp op) {
		super(testValue, op);
	}

	public PhaseFieldMatcher() {
		super();
	}

	@Override
	public void setSelectedObservationMessage(ObservationSelectionMessage msg) {
		// We only want an observation selection message if it is from a phase
		// plot or if it is null.
		if (msg == null || msg.getSource() instanceof PhaseAndMeanPlotPane) {
			observationSelectionMessage = msg;
		}
	}

	@Override
	protected Double getValueUnderTest(ValidObservation ob) {
		// Phase is mandatory; it cannot be null.
		return ob.getStandardPhase();
	}

	@Override
	public IObservationFieldMatcher create(String fieldValue,
			ObservationMatcherOp op) {
		IObservationFieldMatcher matcher = null;

		try {
			Double value = NumberParser.parseDouble(fieldValue);
			if (value < 0.0) {
				// Convert to standard phase.
				value += 1;
			}
			matcher = new PhaseFieldMatcher(value, op);
		} catch (NumberFormatException e) {
			// Nothing to do but return null.
		}

		return matcher;
	}

	@Override
	public String getDisplayName() {
		return LocaleProps.get("PHASE_FILTER_TYPE");
	}

	@Override
	public String getDefaultTestValue() {
		return null;
	}

	@Override
	public String getTestValueFromObservation(ValidObservation ob) {
		Double phase = null;

		if (observationSelectionMessage == null) {
			phase = ob.getStandardPhase();
		} else {
			Object obj = observationSelectionMessage.getSource();
			if (obj instanceof PhaseAndMeanPlotPane) {
				PhaseAndMeanPlotPane pane = (PhaseAndMeanPlotPane) obj;
				if (pane.wasLastSelectionStdPhase()) {
					phase = ob.getStandardPhase();
				} else {
					phase = ob.getPreviousCyclePhase();
				}
			}
		}

		return NumericPrecisionPrefs.formatTime(phase);
	}
}
