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

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.plugin.ob.src.impl.AAVSOPhotometryURLObservationSourceBase;
import org.aavso.tools.vstar.ui.resources.LoginInfo;

/**
 * An APASS epoch photometry database observation source plugin.
 */
public class APASSEpochPhotometryObservationSource extends
		AAVSOPhotometryURLObservationSourceBase {

	public APASSEpochPhotometryObservationSource() {

		super(
				"APASS",
				"https://physics.mcmaster.ca/astro/APASS/conesearch_filter_cr.php?",
				// "apass", "Its_full_of_stars",
				null, null, true);

		seriesNameToTypeMap.put("B", SeriesType.Johnson_B);
		seriesNameToTypeMap.put("V", SeriesType.Johnson_V);
		seriesNameToTypeMap.put("u", SeriesType.Sloan_u);
		seriesNameToTypeMap.put("g", SeriesType.Sloan_g);
		seriesNameToTypeMap.put("r", SeriesType.Sloan_r);
		seriesNameToTypeMap.put("i", SeriesType.Sloan_i);
		seriesNameToTypeMap.put("z", SeriesType.Sloan_z);

		// Initial parameter values.
		raDegs = 57.8155;
		decDegs = -0.26506;
		radiusDegs = 0.005;
		seriesNames.add("B");
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#requiresAuthentication()
	 */
	@Override
	public boolean requiresAuthentication() {
		return true;
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.ObservationSourcePluginBase#additionalAuthenticationSatisfied(org.aavso.tools.vstar.ui.resources.LoginInfo)
	 */
	@Override
	public boolean additionalAuthenticationSatisfied(LoginInfo loginInfo) {
		return loginInfo.isMember();
	}
}
