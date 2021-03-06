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
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
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
 * TODO:<br/>
 * - undoable edits!
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
		List<AbstractObservationRetriever> retrievers = getRequestedNonHeliocentricDatasets();
		for (AbstractObservationRetriever retriever : retrievers) {
			if (!retriever.isHeliocentric()) {

				Pair<RAInfo, DecInfo> coords = getCoordinates(
						retriever.getStarInfo(), lastCoordsFound);

				if (coords != null) {
					retriever.setHeliocentric(true);

					count += Mediator.getInstance().convertObsToHJD(
							retriever.getValidObservations(), coords.first,
							coords.second);

					lastCoordsFound = coords;
				}
			}
		}
		
		if (retrievers.size() != 0 && count != 0) {
			updateUI();
			MessageBox.showMessageDialog("HJD Conversion",
					String.format("%d observations converted.", count));
		}
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
			if (!retriever.getValidObservations().isEmpty()
					&& !retriever.isHeliocentric()
					&& !retriever.isBarycentric()) {
				String name = retriever.getSourceType();
				String designation = retriever.getStarInfo().getDesignation();
				if (!name.equals(designation)) {
					name = name + ": " + designation;
				}
				checkboxes.add(new Checkbox(name, false));
				name2retriever.put(name, retriever);
			}
		}

		if (checkboxes.size() != 0) {
			MultiEntryComponentDialog dialog = new MultiEntryComponentDialog(
					"Non-Heliocentric Datasets", checkboxes);

			if (!dialog.isCancelled()) {
				for (ITextComponent<?> checkbox : checkboxes) {
					Boolean checked = (Boolean) checkbox.getValue();
					if (checked) {
						selected.add(name2retriever.get(checkbox.getName()));
					}
				}
			} else {
				selected.clear();
			}
		} else {
			MessageBox.showMessageDialog("Non-Heliocentric Datasets",
					"No datasets with Julian Date observations");
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
				// Alternative coordinates supplied. Use these as defaults for
				// requests.
				ra = Mediator.getInstance().requestRA(otherCoords.first);
				dec = Mediator.getInstance().requestDec(otherCoords.second);
			} else {
				// No other coordinates supplied so ask user.
				ra = Mediator.getInstance().requestRA();
				dec = Mediator.getInstance().requestDec();
			}
		}

		if (ra != null && dec != null) {
			coords = new Pair<RAInfo, DecInfo>(ra, dec);
		} else {
			MessageBox
					.showWarningDialog("HJD Conversion",
							"The previously loaded observations have NOT been converted to HJD.");
		}

		return coords;
	}
	
	/**
	 * Update UI
	 */
	private void updateUI() {
		
		// PMAK (2021-06-03):
		// There is no way to recalculate observation phases (as for VStar 2.21.3)
		// So we are switching to RAW plot and trying to delete existing phase plot.
		
		Mediator mediator = Mediator.getInstance();
		
		mediator.changeAnalysisType(AnalysisType.RAW_DATA);

		try {
			mediator.dropPhasePlotAnalysis();
		} catch (Exception e) {
			MessageBox.showWarningDialog("HJD Conversion", 
				"Cannot delete current Phase Plot. Please recreate it to reflect changes.");
		}
	
		// Updates RAW plot and data table.
		Mediator.getInstance().updatePlotsAndTables();
		
	}
	
}
