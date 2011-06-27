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
package org.aavso.tools.vstar.ui.mediator.message;

/**
 * This message is sent when a phase change (period, epoch) occurs.
 */
public class PhaseChangeMessage extends MessageBase {

	private double epoch;
	private double period;

	/**
	 * Constructor.
	 * 
	 * @param source
	 *            The source of this message.
	 * @param epoch
	 *            The epoch associated with the phase change.
	 * @param period
	 *            The period associated with the phase change.
	 */
	public PhaseChangeMessage(Object source, double epoch, double period) {
		super(source);
		this.epoch = epoch;
		this.period = period;
	}

	/**
	 * @return the epoch
	 */
	public double getEpoch() {
		return epoch;
	}

	/**
	 * @return the period
	 */
	public double getPeriod() {
		return period;
	}
}
