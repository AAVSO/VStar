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

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.TextDialog;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.ui.dialog.TextField.Kind;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.util.stats.DescStats;

/**
 * This is an observation VStar tool plug-in which displays, descriptive
 * statistics about each series.
 * 
 * @author David Benn
 * @version 1.0 - 19 Feb 2015
 * */
public class DescStatsBySeries extends ObservationToolPluginBase {

	@Override
	public void invoke(ISeriesInfoProvider seriesInfo) {
		List<ITextComponent<String>> seriesFields = new ArrayList<ITextComponent<String>>();

		for (SeriesType type : seriesInfo.getSeriesKeys()) {
			if (!type.isSynthetic() && !type.isUserDefined()
					&& (type != SeriesType.Excluded)
					&& (type != SeriesType.DISCREPANT)) {
				List<ValidObservation> obs = seriesInfo.getObservations(type);

				double[] means = DescStats.calcMagMeanInRange(obs,
						JDTimeElementEntity.instance, 0, obs.size() - 1);
				double stdev = DescStats.calcMagSampleStdDevInRange(obs, 0,
						obs.size() - 1);

				String jdMeanStr = NumericPrecisionPrefs.formatTime(means[1]);
				String magMeanStr = NumericPrecisionPrefs.formatMag(means[0]);
				String magStdevStr = NumericPrecisionPrefs.formatMag(stdev);

				String statsStr = String.format("%s: %s (%s)", jdMeanStr,
						magMeanStr, magStdevStr);
						
				seriesFields.add(new TextField(type.getDescription(), statsStr,
						Kind.LINE));
			}
		}

		new TextDialog("JD mean, mag mean & stdev", seriesFields);
	}

	@Override
	public String getDescription() {
		return "Observation tool plug-in to display descriptive statistics by series";
	}

	@Override
	public String getDisplayName() {
		return "Descriptive statistics by series";
	}
}