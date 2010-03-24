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

/**
 * This class encodes information about application progress.
 */
public class ProgressInfo {

	// Singletons for use by consumers of this class.
	public static ProgressInfo RESET_PROGRESS = new ProgressInfo(
			ProgressType.RESET_PROGRESS);

	public static ProgressInfo INCREMENT_PROGRESS = new ProgressInfo(
			ProgressType.INCREMENT_PROGRESS, 1);

	public static ProgressInfo COMPLETE_PROGRESS = new ProgressInfo(
			ProgressType.COMPLETE_PROGRESS);

	public static ProgressInfo BUSY_PROGRESS = new ProgressInfo(
			ProgressType.BUSY_PROGRESS);

	// Type of this progress info.
	private ProgressType type;

	// A type-dependent number.
	private int num;

	/**
	 * Constructor.
	 * 
	 * @param type
	 *            The type of this progress information object.
	 * @param num
	 *            A number whose meaning depends upon type, e.g. min or max
	 *            progress value.
	 */
	public ProgressInfo(ProgressType type, int num) {
		this.type = type;
		this.num = num;
	}

	/**
	 * Constructor.
	 * 
	 * The num member defaults to 0.
	 * 
	 * @param type
	 *            The type of this progress information object.
	 */
	public ProgressInfo(ProgressType type) {
		this(type, 0);
	}

	/**
	 * @return the type
	 */
	public ProgressType getType() {
		return type;
	}

	/**
	 * @return the num
	 */
	public int getNum() {
		return num;
	}
}
