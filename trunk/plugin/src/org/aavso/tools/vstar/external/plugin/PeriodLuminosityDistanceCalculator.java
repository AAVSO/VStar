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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.SelectableTextField;
import org.aavso.tools.vstar.ui.dialog.TextDialog;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.ui.dialog.series.SingleSeriesSelectionDialog;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.util.stats.DescStats;

/**
 * <p>
 * This plug-in calculates the distance to an object based upon the
 * Period-Luminosity relationship for specific variable star types.
 * <p/>
 * <p>
 * I was inspired to write this while participating in the "CHOICE Variable Star
 * Classification and Light Curves III:" course in Jan & Feb 2013.
 * </p>
 * <p>
 * References:<br>
 * <ol>
 * <li>[1] http://outreach.atnf.csiro.au/education/senior/astrophysics/
 * variable_cepheids.html</li>
 * <li>[2] Turner, D.,"The PL calibration for Milky Way Cepheids and its
 * implications for the distance scale"</li>
 * </ol>
 * </p>
 */
public class PeriodLuminosityDistanceCalculator extends
		ObservationToolPluginBase {

	private final static String DCEP = "DCEP";
	private final static Map<String, String> absMagEqns;

	static {
		absMagEqns = new HashMap<String, String>();
		absMagEqns.put(DCEP, "-1.29 - 2.78 * log10(period)");
	}

	private Double period;
	private Double magnitude;

	public PeriodLuminosityDistanceCalculator() {
		super();
		this.period = null;
		this.magnitude = null;
	}

	@Override
	public void invoke(ISeriesInfoProvider seriesInfo) {
		// Get default period, if there is one.
		StarInfo starInfo = Mediator.getInstance().getLatestNewStarMessage()
				.getStarInfo();
		period = starInfo.getPeriod();

		// Request the series to be used.
		ObservationAndMeanPlotModel model = Mediator.getInstance()
				.getObservationPlotModel(AnalysisType.RAW_DATA);

		SingleSeriesSelectionDialog seriesDlg = new SingleSeriesSelectionDialog(
				model);

		if (!seriesDlg.isCancelled()) {
			// Get the mean magnitude.
			SeriesType series = seriesDlg.getSeries();
			List<ValidObservation> obs = seriesInfo.getObservations(series);
			// TODO: how should this be computed? B-V or arithmetic mean as
			// below?
			magnitude = DescStats.calcMagMeanInRange(obs,
					JDTimeElementEntity.instance, 0, obs.size() - 1)[DescStats.MEAN_MAG_INDEX];

			// Get the type, modified period and magnitude.
			Set<String> types = new TreeSet<String>();
			types.add(DCEP);
			String type = starInfo.getVarType();
			String initialType = type;
			if (type == null || !types.contains(type)) {
				initialType = DCEP;
			}

			List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();

			final TextField absMagEqn = new TextField(
					"Absolute Mag Relationship",
					initialType != null ? absMagEqns.get(initialType) : "",
					true, false);

			final SelectableTextField typesField = new SelectableTextField(
					"Variable Type", types, initialType);
			typesField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Update the abs mag equation when the variable type
					// selection changes.
					absMagEqns.get(typesField.getValue());
				}
			});
			fields.add(typesField);
			fields.add(absMagEqn);

			DoubleField periodField = new DoubleField("Period", null, null,
					period);
			fields.add(periodField);
			DoubleField magField = new DoubleField("Mean Apparent Mag", null,
					null, magnitude);
			fields.add(magField);
			MultiEntryComponentDialog inputDlg = new MultiEntryComponentDialog(
					"Inputs", fields);

			if (!inputDlg.isCancelled()) {
				period = periodField.getValue();
				magnitude = magField.getValue();
				String varType = typesField.getValue();

				Double absMagnitude = null;
				Double distance = null;

				if (varType == DCEP) {
					absMagnitude = calcAbsMagForDCEPType(period);
					distance = calcDistance(magnitude, absMagnitude);
					// double absMagnitudeMin = -1.29 - 0.1 - (2.78 - 0.12)
					// * Math.log10(period);
					// double distanceMin = calcDistance(magnitude,
					// absMagnitudeMin);
					//
					// double absMagnitudeMax = -1.29 + 0.1 - (2.78 + 0.12)
					// * Math.log10(period);
					// double distanceMax = calcDistance(magnitude,
					// absMagnitudeMax);
				} else {
					// TODO: generalise to Cepheid Type II and RR Lyrae via a
					// chooser one choice of which is
					// "Custom" that uses VELa (VStar expression language:
					// modelling, this, filter, JD calc, basically anywhere
					// numbers are used as input) => opens
				}

				if (absMagnitude != null && distance != null) {
					showDistance(varType, absMagnitude, distance);
				}
			}
		}
	}

	/**
	 * Show the absolute magnitude and distance in a dialog.
	 * 
	 * @param varType
	 *            The variable type.
	 * @param absMagnitude
	 *            The absolute magnitude.
	 * @param distance
	 *            The distance in parsecs.
	 */
	private void showDistance(String varType, double absMagnitude,
			double distance) {
		List<ITextComponent<String>> resultFields = new ArrayList<ITextComponent<String>>();

		// resultFields.add(new TextField("Absolute Magnitude Calculation",
		// absMagEqns.get(varType), true, false, TextField.Kind.LINE));
		resultFields.add(new TextField("Absolute Magnitude",
				NumericPrecisionPrefs.formatMag(absMagnitude), true, false));
		resultFields.add(new TextField("Distance (parsecs)",
				NumericPrecisionPrefs.formatOther(distance), true, false));
		resultFields
				.add(new TextField("Distance (light years)",
						NumericPrecisionPrefs.formatOther(distance * 3.26),
						true, false));
		resultFields.add(new TextField("Distance Modulus",
				"10 ^ ((apparent mag - absolute mag + 5) / 5)", true, false));
		// resultFields[2] = new TextField("Minimum Distance", String
		// .format(otherFmt + " parsecs", distanceMin), true,
		// false, TextField.Kind.LINE);
		// resultFields[3] = new TextField("Maximum Distance", String
		// .format(otherFmt + " parsecs", distanceMax), true,
		// false, TextField.Kind.LINE);

		new TextDialog("Distance", resultFields);
	}

	/**
	 * Given period, return the absolute magnitude according to the Classical
	 * Type I Cepheid Period-Luminosity relationship, according to Turner et al.
	 * 
	 * @param period
	 *            The period in days.
	 * @return The absolute magnitude.
	 */
	private double calcAbsMagForDCEPType(double period) {
		// TODO: -1.29+/-0.1, 2.78+/-0.12 => 2 or 4 combinations?
		return -1.29 - 2.78 * Math.log10(period);
	}

	/**
	 * Given apparent and absolute magnitudes, calculate distance.
	 * 
	 * @param apparentMag
	 *            The absolute magnitude.
	 * @param absoluteMag
	 *            apparent magnitude.
	 * @return The distance via the distance-modulus.
	 */
	private double calcDistance(double apparentMag, double absoluteMag) {
		return Math.pow(10, (apparentMag - absoluteMag + 5) / 5);
	}

	@Override
	public String getDescription() {
		return "Leavitt's Law (Period-Luminosity Relationship) distance calculator";
	}

	@Override
	public String getDisplayName() {
		return "Leavitt's Law distance calculator";
	}
}
