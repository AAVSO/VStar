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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.Checkbox;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.RAInfo;

/**
 * Converts currently loaded observations to HJD if they are not already
 * Heliocentric.
 * 
 * TODO: undoable edits!
 */
public class HJDConverter extends ObservationToolPluginBase {

	@Override
	public String getDisplayName() {
		return "Heliocentric JD Converter";
	}

	@Override
	public String getDescription() {
		return "Heliocentric JD Converter";
	}

	@Override
	public void invoke(ISeriesInfoProvider seriesInfo) {
		// If coordinates were returned, iterate over all new star messages,
		// converting observation times to HJD that are not already
		// Heliocentric. Note that we do not make use of seriesInfo since it
		// does not tell us which observations are Heliocentric.
		int count = 0;
		Pair<RAInfo, DecInfo> lastCoordsFound = null;
		for (AbstractObservationRetriever retriever : getRequestedNonHeliocentricDatasets()) {
			if (!retriever.isHeliocentric()) {

				Pair<RAInfo, DecInfo> coords = getCoordinates(
						retriever.getStarInfo(), lastCoordsFound);

				Mediator.getInstance().convertObsToHJD(
						retriever.getValidObservations(), coords.first,
						coords.second);

				int numConverted = retriever.getValidObservations().size();

				if (numConverted > 0) {
					retriever.setHeliocentric(true);
					count += numConverted;
				}

				lastCoordsFound = coords;
			}
		}

		MessageBox.showMessageDialog("HJD Conversion",
				String.format("%d observations converted.", count));
	}

	/**
	 * Return a list of requested selected retrievers whose observations have
	 * not been converted to HJD. The user is prompted to select which
	 * non-Heliocentric datasets to convert.
	 */
	private List<AbstractObservationRetriever> getRequestedNonHeliocentricDatasets() {
		List<AbstractObservationRetriever> selected = new ArrayList<AbstractObservationRetriever>();

		List<AbstractObservationRetriever> retrievers = getNonHeliocentricDatasets();

		List<ITextComponent<?>> checkboxes = new ArrayList<ITextComponent<?>>();
		Map<String, AbstractObservationRetriever> name2retriever = new HashMap<String, AbstractObservationRetriever>();

		for (AbstractObservationRetriever retriever : retrievers) {
			if (!retriever.isHeliocentric()) {
				String name = retriever.getSourceType() + ": "
						+ retriever.getStarInfo().getDesignation();
				checkboxes.add(new Checkbox(name, false));
				name2retriever.put(name, retriever);
			}
		}

		MultiEntryComponentDialog dialog = new MultiEntryComponentDialog(
				"Non-Heliocentric Datasets", checkboxes);

		if (!dialog.isCancelled()) {
			for (ITextComponent<?> checkbox : checkboxes) {
				Boolean checked = (Boolean) checkbox.getValue();
				if (checked) {
					selected.add(name2retriever.get(checkbox.getName()));
				}
			}
		}

		return selected;
	}

	/**
	 * Return a list of observation retrievers whose observations have not been
	 * converted to HJD.
	 */
	private List<AbstractObservationRetriever> getNonHeliocentricDatasets() {
		List<AbstractObservationRetriever> retrievers = new ArrayList<AbstractObservationRetriever>();

		for (NewStarMessage msg : Mediator.getInstance()
				.getNewStarMessageList()) {
			AbstractObservationRetriever retriever = msg.getStarInfo()
					.getRetriever();

			if (!retriever.isHeliocentric()) {
				retrievers.add(retriever);
			}
		}

		return retrievers;
	}

	/**
	 * Return RA and Dec. First look for coordinates in any of our loaded
	 * datasets. Use the first coordinates found. We are making the simplifying
	 * assumption that all data sets correspond to the same object! If not
	 * found, ask the user to enter them. If none are supplied, null is
	 * returned.
	 * 
	 * @param info
	 *            a StarInfo object possibly containing coordinates
	 * @param otherCoords
	 *            Coordinates to use if info contains none.
	 * @return A pair of coordinates: RA and Declination
	 */
	private Pair<RAInfo, DecInfo> getCoordinates(StarInfo info,
			Pair<RAInfo, DecInfo> otherCoords) {
		RAInfo ra = info.getRA();
		DecInfo dec = info.getDec();
		Pair<RAInfo, DecInfo> coords = null;

		if (ra == null || dec == null) {
			// Ask the user for J2000.0 RA/DEC and if that is cancelled,
			// indicate that HJD conversion cannot take place.
			if (otherCoords != null) {
				// Alternative coordinates supplied.
				ra = otherCoords.first;
				dec = otherCoords.second;
				// TODO: still prompt! need degs to H:M:S, D:M:S
			} else {
				// No other coordinates supplied so ask user.
				ra = Mediator.getInstance().requestRA();
				dec = Mediator.getInstance().requestDec();
				if (ra == null || dec == null) {
					MessageBox
							.showWarningDialog("HJD Conversion",
									"The previously loaded observations have NOT been converted to HJD.");
				}
			}
		}

		if (ra != null && dec != null) {
			coords = new Pair<RAInfo, DecInfo>(ra, dec);
		}

		return coords;
	}
}
