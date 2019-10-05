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
package org.aavso.tools.vstar.plugin.ob.sink.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationSinkPluginBase;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.NewStarType;
import org.aavso.tools.vstar.ui.mediator.ViewModeType;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * An observation sink plugin for simple text and AAVSO Download format file
 * saves.
 */
public class TextFormatObservationSinkPlugin extends ObservationSinkPluginBase {

	private final static Map<String, String> DELIMS;

	static {
		DELIMS = new TreeMap<String, String>();
		DELIMS.put("Comma", ",");
		DELIMS.put("Space", " ");
		DELIMS.put("Tab", "\t");
	}

	@Override
	public void save(PrintWriter writer, List<ValidObservation> obs,
			String delimiter) throws IOException {

		if (Mediator.getInstance().getLatestNewStarMessage().getNewStarType() == NewStarType.NEW_STAR_FROM_SIMPLE_FILE) {
			saveObsToFileInSimpleFormat(writer, obs, delimiter);
		} else {
			saveObsToFileInAAVSOFormat(writer, obs, delimiter);
		}
	}

	@Override
	public String getDisplayName() {
		return LocaleProps.get("TEXT_FORMAT_FILE");
	}

	@Override
	public String getDescription() {
		return "Save as simple or AAVSO text format file.";
	}

	@Override
	public Map<String, String> getDelimiterNameValuePairs() {
		return DELIMS;
	}

	// Helpers

	/**
	 * Write observations in simple format to specified output stream. Also
	 * updates the progress bar upon each observation written.
	 * 
	 * @param writer
	 *            The specified print writer.
	 * @param obs
	 *            A list of observations.
	 * @param delimiter
	 *            The field delimiter to use; may be null.
	 */
	private void saveObsToFileInSimpleFormat(PrintWriter writer,
			List<ValidObservation> obs, String delimiter) throws IOException {
		for (ValidObservation ob : obs) {
			boolean includeJD = Mediator.getInstance().getViewMode() == ViewModeType.LIST_OBS_MODE
					|| Mediator.getInstance().getAnalysisType() == AnalysisType.RAW_DATA;
			// Exclude excluded observations from the output file C.Kotnik
			// 2018-12-16
			if (!ob.isExcluded()) {
				writer.write(ob.toSimpleFormatString(delimiter, includeJD));
			}

			Mediator.getInstance().getProgressNotifier()
					.notifyListeners(ProgressInfo.INCREMENT_PROGRESS);
		}
	}

	/**
	 * Write observations in AAVSO format to specified output stream. Also
	 * updates the progress bar upon each observation written.
	 * 
	 * @param writer
	 *            The specified print writer.
	 * @param obs
	 *            A list of observations.
	 * @param delimiter
	 *            The field delimiter to use; may be null.
	 */
	private void saveObsToFileInAAVSOFormat(PrintWriter writer,
			List<ValidObservation> obs, String delimiter) throws IOException {
		for (ValidObservation ob : obs) {
			boolean includeJD = Mediator.getInstance().getViewMode() == ViewModeType.LIST_OBS_MODE
					|| Mediator.getInstance().getAnalysisType() == AnalysisType.RAW_DATA;
			// Exclude excluded observations from the output file C.Kotnik
			// 2018-12-16
			if (!ob.isExcluded()) {
				writer.write(ob.toAAVSOFormatString(delimiter, includeJD));
			}
			Mediator.getInstance().getProgressNotifier()
					.notifyListeners(ProgressInfo.INCREMENT_PROGRESS);
		}
	}

}