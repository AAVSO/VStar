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
package org.aavso.tools.vstar.util.stats.epoch;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;

/**
 * This epoch determination strategy finds the numerically maximum 
 * magnitude value in the dataset. This magnitude's JD becomes the 
 * epoch. TODO: should this instead mean the numerically smallest,
 * i.e. brightest magnitude? Which is less confusing to the end-user?
 */
public class MaxMagEpochStrategy implements IEpochStrategy {

	/**
	 * @see org.aavso.tools.vstar.util.stats.epoch.IEpochStrategy#determineEpoch(java.util.List)
	 */
	public double determineEpoch(List<ValidObservation> obs) {
		double maxMagJD = 0;
		double maxMag = Double.MIN_VALUE;
		
		for (ValidObservation ob : obs) {
			if (ob.getMag() > maxMag) {
				maxMag = ob.getMag();
				maxMagJD = ob.getJD();
			}
		}
		
		assert(maxMagJD != 0);
		
		return maxMagJD;
	}

	/**
	 * @see org.aavso.tools.vstar.util.stats.epoch.IEpochStrategy#getDescription()
	 */
	public String getDescription() {
		return "JD of maximum magnitude";
	}
}
