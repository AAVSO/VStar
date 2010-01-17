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

import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;

/**
 * A message class containing new star type and GUI component information.
 */
public class NewStarMessage {

	private NewStarType newStarType;
	private StarInfo starInfo;
	private List<ValidObservation> obs;
	private Map<SeriesType, List<ValidObservation>> obsCategoryMap;

	/**
	 * Constructor.
	 * 
	 * @param newStarType
	 *            The type of the new star.
	 * @param info
	 *            Information about the star's characteristics, e.g.
	 *            designation, name, and possibly: period, epoch).
	 * @param obs
	 *            The list of valid observations for the loaded star.
	 * @param obsCategoryMap
	 *            A mapping from category (band, fainter-than) to observation.
	 */
	public NewStarMessage(NewStarType newStarType, StarInfo info,
			List<ValidObservation> obs,
			Map<SeriesType, List<ValidObservation>> obsCategoryMap) {
		this.newStarType = newStarType;
		this.starInfo = info;
		this.obs = obs;
		this.obsCategoryMap = obsCategoryMap;
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

	/**
	 * @return the observation list for the star
	 */
	public List<ValidObservation> getObservations() {
		return obs;
	}

	/**
	 * @return the obsCategoryMap
	 */
	public Map<SeriesType, List<ValidObservation>> getObsCategoryMap() {
		return obsCategoryMap;
	}

}
