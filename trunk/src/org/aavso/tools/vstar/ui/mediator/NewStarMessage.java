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

import org.aavso.tools.vstar.ui.model.NewStarType;

/**
 * A message class containing new star type and GUI component information.
 */
public class NewStarMessage {

	private NewStarType newStarType;
	private StarInfo starInfo;

	/**
	 * Constructor.
	 * 
	 * @param newStarType
	 *            The type of the new star.
	 * @param info
	 *            Information about the star's characteristics, e.g.
	 *            designation, name, and possibly: period, epoch).
	 */
	public NewStarMessage(NewStarType newStarType, StarInfo info) {
		this.newStarType = newStarType;
		this.starInfo = info;
	}

	/**
	 * @return the newStarType
	 */
	public NewStarType getNewStarType() {
		return newStarType;
	}

	/**
	 * @return the starInfo
	 */
	public StarInfo getStarInfo() {
		return starInfo;
	}
}
