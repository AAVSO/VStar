/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2014 AAVSO (http://www.aavso.org/)
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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.dialog.TextDialog;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;

/**
 * This is an observation VStar tool plug-in which displays, for each loaded
 * series, the observer codes of observers who have contributed observations to
 * that series.
 * 
 * @author Paul F. York (with lots of patient help from D. Benn)
 * @version 1.0 - 14 Jul 2014
 */
public class ObserversBySeries extends ObservationToolPluginBase {

	@Override
	public void invoke(ISeriesInfoProvider seriesInfo) {
		String obsCodes;
		String squareBrackets = "\\[|\\]";

		List<ITextComponent<String>> seriesFields = new ArrayList<ITextComponent<String>>();
		for (SeriesType type : seriesInfo.getSeriesKeys()) {
			if (!type.isSynthetic() && !type.isUserDefined()
					&& (type != SeriesType.Excluded)
					&& (type != SeriesType.DISCREPANT)) {
				Set<String> obsCodesForCurrentSeries = new TreeSet<String>();
				for (ValidObservation obs : seriesInfo.getObservations(type)) {
					obsCodesForCurrentSeries.add(obs.getObsCode());
				}
				obsCodes = obsCodesForCurrentSeries.toString();
				obsCodes = obsCodes.replaceAll(squareBrackets, "");
				obsCodes = wrap(obsCodes); // Force lines to wrap ...
				seriesFields.add(new TextArea(type.getDescription(), obsCodes));
			}
		}
		new TextDialog("Observers By Series", seriesFields);
	}

	/*
	 * Wrap a string of text for output ...
	 */
	private String wrap(String str) {
		int approxObsCodesPerLine = 12;
		int approxCharsPerObsCode = 5;
		int approxLineLength = approxObsCodesPerLine * approxCharsPerObsCode;
		int position;
		String replacementString = (", " + "\n");
		StringBuffer sbuff = new StringBuffer();

		sbuff.append(str);
		position = approxLineLength;
		if (!(sbuff.length() < approxLineLength)) {
			while (position < sbuff.length() - 7) {
				position = sbuff.indexOf(", ", position);
				sbuff = sbuff
						.replace(position, position + 2, replacementString);
				position += replacementString.length() + approxLineLength;
			}
		}
		return sbuff.toString();
	}

	@Override
	public String getDescription() {
		return "Observation tool plug-in to display observers by series";
	}

	@Override
	public String getDisplayName() {
		return "Observers by Series";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "ObserversBySeries.pdf";
	}

}