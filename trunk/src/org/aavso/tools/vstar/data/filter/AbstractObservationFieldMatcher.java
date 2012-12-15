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
package org.aavso.tools.vstar.data.filter;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * The interface for all observation field matchers.
 */
public abstract class AbstractObservationFieldMatcher<T> implements
		IObservationFieldMatcher {

	protected T testValue;
	protected ObservationMatcherOp op;
	protected ObservationMatcherOp[] ops;
	protected ObservationSelectionMessage observationSelectionMessage;

	/**
	 * Constructor.
	 * 
	 * @param testValue
	 *            The value against which data are to be matched.
	 * @param op
	 *            The operation to apply to the observation and test expression.
	 * @param ops
	 *            The operations associated with matchers of this type.
	 */
	public AbstractObservationFieldMatcher(T testValue,
			ObservationMatcherOp op, ObservationMatcherOp[] ops) {
		this.testValue = testValue;
		this.op = op;
		this.ops = ops;
		this.observationSelectionMessage = null;

		// Is the operator legal with respect to supplied legal operators?
		if (op != null) {
			boolean found = false;

			for (ObservationMatcherOp possibleOp : ops) {
				if (op == possibleOp) {
					found = true;
					break;
				}
			}

			if (!found) {
				String msg = LocaleProps.get("FIELD_MATCHER_OP_ERR_MSG") + " '"
						+ op + "'";
				throw new IllegalArgumentException(msg);
			}
		}
	}

	/**
	 * Constructor (for matcher "prototypes").
	 * 
	 * @param ops
	 *            The operations associated with matchers of this type.
	 */
	protected AbstractObservationFieldMatcher(ObservationMatcherOp[] ops) {
		this(null, null, ops);
	}

	/**
	 * @see org.aavso.tools.vstar.data.filter.IObservationFieldMatcher#getMatcherOps()
	 */
	public ObservationMatcherOp[] getMatcherOps() {
		return ops;
	}

	/**
	 * Get the value under test. If any conversion must first be done, e.g. from
	 * string, this method must handle that.
	 * 
	 * @param ob
	 *            An observation containing the value.
	 * @return The value to be matched against.
	 */
	protected abstract T getValueUnderTest(ValidObservation ob);

	/**
	 * @see org.aavso.tools.vstar.data.filter.IObservationFieldMatcher#setSelectedObservationMessage(org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage)
	 */
	@Override
	public void setSelectedObservationMessage(ObservationSelectionMessage msg) {
		this.observationSelectionMessage = msg;
	}

	/**
	 * @return the observationSelectionMessage
	 */
	public ObservationSelectionMessage getObservationSelectionMessage() {
		return observationSelectionMessage;
	}

	/**
	 * @see org.aavso.tools.vstar.data.filter.IObservationFieldMatcher#getParsableDescription()
	 */
	@Override
	public String getParsableDescription() {
		return getDisplayName() + " " + op.toParsableString() + " " + testValue;
	}	
}
