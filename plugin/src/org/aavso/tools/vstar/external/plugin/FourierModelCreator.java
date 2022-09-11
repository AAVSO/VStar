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

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.ui.dialog.IntegerField;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.model.HarmonicInputDialog;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.model.PeriodAnalysisDerivedMultiPeriodicModel;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisAlgorithm;
import org.aavso.tools.vstar.util.period.dcdft.TSDcDft;

/**
 * This plug-in creates a Fourier model from a number of periods and one or more
 * of their harmonics.
 */
public class FourierModelCreator extends ModelCreatorPluginBase {

	@Override
	public String getDescription() {
		return "Create a Fourier model independent of period search";
	}

	@Override
	public String getDisplayName() {
		return "Fourier Model";
	}

	@Override
	public IModel getModel(List<ValidObservation> obs) {
		PeriodAnalysisDerivedMultiPeriodicModel model = null;		
		try {
			// Catch all possible errors: getHarmonics() may generate an error if one of frequencies < 0, etc.			
			IntegerField numPeriodField = new IntegerField("Number of Periods", 0, null, 1);
			MultiEntryComponentDialog numPeriodsDialog = new MultiEntryComponentDialog(
					"Period Count", numPeriodField);
			
			if (!numPeriodsDialog.isCancelled()) {
				int numPeriods = numPeriodField.getValue();

				List<Double> userSelectedFreqs = new ArrayList<Double>();
				for (int i = 0; i < numPeriods; i++) {
					userSelectedFreqs.add(1.0);
				}

				Map<Double, List<Harmonic>> freqToHarmonicsMap = new HashMap<Double, List<Harmonic>>();

				HarmonicInputDialog dialog = new HarmonicInputDialog(
						DocumentManager.findActiveWindow(), userSelectedFreqs,
						freqToHarmonicsMap);
			
				if (!dialog.isCancelled()) { 
					List<Harmonic> harmonics = dialog.getHarmonics();
					if (!harmonics.isEmpty()) {
						IPeriodAnalysisAlgorithm algorithm = new TSDcDft(obs);
						model = new PeriodAnalysisDerivedMultiPeriodicModel(null,
								harmonics, algorithm);

					} else {
						throw new Exception("Period list error");
					}
				}
			}
		} catch (Exception e) {
			MessageBox.showErrorDialog("Fourier Model Creator", 
					e.getLocalizedMessage());
		}

		return model;
	}
}
