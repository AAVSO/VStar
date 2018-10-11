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

import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.RAInfo;

/**
 * Converts currently loaded observations to HJD if they are not already
 * Heliocentric.
 */
public class HJDConverter extends ObservationToolPluginBase {

	@Override
	public String getDisplayName() {
		return "HJD Converter";
	}

	@Override
	public String getDescription() {
		return "HJD Converter";
	}

	@Override
	public void invoke(ISeriesInfoProvider seriesInfo) {
		// Get coordinates for object
		Pair<RAInfo, DecInfo> coords = getCoordinates();

		// If coordinates were returned, iterate over all new star messages,
		// converting observation times to HJD that are not already
		// Heliocentric. Note that we do not make use of seriesInfo since it
		// does not tell us which observations are Heliocentric.
		if (coords != null) {
			int count = 0;
			for (NewStarMessage msg : Mediator.getInstance()
					.getNewStarMessageList()) {
				AbstractObservationRetriever retriever = msg.getStarInfo()
						.getRetriever();
				if (!retriever.isHeliocentric()) {
					Mediator.getInstance().convertObsToHJD(
							retriever.getValidObservations(), coords.first,
							coords.second);
					count += retriever.getValidObservations().size();
				}
			}

			MessageBox.showMessageDialog("HJD Conversion",
					String.format("%d observations converted.", count));
		}
	}

	/**
	 * Return RA and Dec. First look for coordinates in any of our loaded
	 * datasets. Use the first coordinates found. We are making the simplifying
	 * assumption that all data sets correspond to the same object! If not
	 * found, ask the user to enter them. If none are supplied, null is
	 * returned.
	 * 
	 * @return A pair of coordinates: RA and Declination
	 */
	private Pair<RAInfo, DecInfo> getCoordinates() {
		RAInfo ra = null;
		DecInfo dec = null;
		Pair<RAInfo, DecInfo> coords = null;

		for (NewStarMessage msg : Mediator.getInstance()
				.getNewStarMessageList()) {
			ra = msg.getStarInfo().getRA();
			dec = msg.getStarInfo().getDec();

			if (ra != null && dec != null) {
				break;
			}
		}

		if (ra == null || dec == null) {
			// Asking the user for J2000.0 RA/DEC and if that is cancelled,
			// indicate that HJD conversion cannot take place.
			ra = Mediator.getInstance().requestRA();
			dec = Mediator.getInstance().requestDec();
			if (ra == null || dec == null) {
				MessageBox
						.showWarningDialog("HJD Conversion",
								"The previously loaded observations have NOT been converted to HJD.");
			}
		}

		if (ra != null && dec != null) {
			coords = new Pair<RAInfo, DecInfo>(ra, dec);
		}

		return coords;
	}
}
