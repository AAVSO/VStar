/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2026  AAVSO (http://www.aavso.org/)
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

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.external.lib.KweeVanWoerdenLib;
import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.Checkbox;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.SelectableTextField;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.dialog.TextDialog;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.util.locale.NumberParser;
import org.aavso.tools.vstar.ui.dialog.series.SingleSeriesSelectionDialog;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.util.Tolerance;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * General tool plug-in: Kwee–van Woerden time of minimum/maximum with Deeg
 * (2020) timing uncertainties.
 */
public class KweeVanWoerdenTool extends GeneralToolPluginBase {

	private static final String FOLDS_5 = "5 (recommended)";
	private static final String FOLDS_3 = "3 (classic KvW)";
	private static final String FOLDS_7 = "7";

	private static final String T1_MIDPOINT = "Midpoint of light curve";
	private static final String T1_EXTREMUM = "Extreme magnitude";

	private static final String EVENT_MIN = "Minimum (eclipse / transit)";
	private static final String EVENT_MAX = "Maximum";

	@Override
	public void invoke() {
		Mediator mediator = Mediator.getInstance();

		if (mediator.getNewStarMessageList().isEmpty()) {
			MessageBox.showErrorDialog(getDisplayName(), "There are no observations loaded.");
			return;
		}

		if (mediator.getAnalysisType() != AnalysisType.RAW_DATA) {
			MessageBox.showErrorDialog(getDisplayName(),
					"Kwee–van Woerden requires the Raw Data (JD) view. Switch from the phase plot and try again.");
			return;
		}

		ObservationAndMeanPlotModel plotModel = mediator.getObservationPlotModel(AnalysisType.RAW_DATA);
		SingleSeriesSelectionDialog seriesDialog = new SingleSeriesSelectionDialog(plotModel);
		if (seriesDialog.isCancelled()) {
			return;
		}

		SeriesType series = seriesDialog.getSeries();
		List<ValidObservation> obs = plotModel.getObservations(series);
		if (obs == null || obs.size() < 7) {
			MessageBox.showErrorDialog(getDisplayName(),
					"The selected series needs at least 7 observations (eclipse-only segment recommended).");
			return;
		}

		ParamsDialogResult dialogResult = showParamsDialog(obs);
		if (dialogResult == null) {
			return;
		}

		try {
			KweeVanWoerdenLib.Result result = analyzeObservations(obs, dialogResult.params, dialogResult.eventMinimum);
			showResultDialog(result, series, obs.size());
		} catch (AlgorithmError e) {
			MessageBox.showErrorDialog(getDisplayName(), e.getMessage());
		}
	}

	@Override
	public String getDescription() {
		return "Kwee–van Woerden eclipse/transit timing (Deeg 2020 modified error estimate)";
	}

	@Override
	public String getDisplayName() {
		return "Kwee-van Woerden";
	}

	@Override
	public String getDocName() {
		return "KweeVanWoerden.md";
	}

	@Override
	public Boolean test() {
		boolean ok = true;
		setTestMode(true);
		try {
			double tMid = 2459000.25;
			int n = 41;
			double dt = 0.001;
			List<ValidObservation> obs = new ArrayList<ValidObservation>();
			for (int i = 0; i < n; i++) {
				double t = tMid + (i - n / 2) * dt;
				double x = t - tMid;
				// Eclipse: fainter (higher mag) at mid-time
				double mag = 10.0 + 0.8 * Math.exp(-x * x / (2 * 0.008 * 0.008));
				ValidObservation ob = new ValidObservation();
				ob.setJD(t);
				ob.setMagnitude(new Magnitude(mag, 0.01));
				obs.add(ob);
			}

			KweeVanWoerdenLib.Params params = new KweeVanWoerdenLib.Params();
			params.nfold = 5;
			params.t1Mode = KweeVanWoerdenLib.T1Mode.MIDPOINT;
			params.mu = 0.01;
			params.resampleIfNeeded = false;

			KweeVanWoerdenLib.Result result = analyzeObservations(obs, params, true);
			ok &= Tolerance.areClose(tMid, result.t0, 1e-4, true);
			ok &= result.sigmaDeeg > 0 && !Double.isNaN(result.sigmaDeeg);
		} catch (Exception e) {
			ok = false;
		} finally {
			setTestMode(false);
		}
		return ok;
	}

	/**
	 * Run KvW on VStar observations. Magnitudes are converted so the event is a
	 * minimum in the value array (negate for eclipse minima).
	 */
	static KweeVanWoerdenLib.Result analyzeObservations(List<ValidObservation> obs, KweeVanWoerdenLib.Params params,
			boolean eventIsMinimum) throws AlgorithmError {
		int n = obs.size();
		double[] times = new double[n];
		double[] values = new double[n];
		for (int i = 0; i < n; i++) {
			ValidObservation ob = obs.get(i);
			times[i] = ob.getJD();
			double mag = ob.getMag();
			// Lower value at the event of interest
			values[i] = eventIsMinimum ? -mag : mag;
		}
		params.eventType = KweeVanWoerdenLib.EventType.MINIMUM;
		return KweeVanWoerdenLib.analyze(times, values, params);
	}

	private ParamsDialogResult showParamsDialog(List<ValidObservation> obs) {
		// Use the (name, values, initial) constructor so the combo is not editable
		// (MultiEntryComponentDialog calls setEditable(!isReadOnly())).
		SelectableTextField nfoldField = new SelectableTextField("Number of folds",
				Arrays.asList(FOLDS_5, FOLDS_3, FOLDS_7), FOLDS_5);

		SelectableTextField t1Field = new SelectableTextField("Initial time estimate",
				Arrays.asList(T1_MIDPOINT, T1_EXTREMUM), T1_MIDPOINT);

		SelectableTextField eventField = new SelectableTextField("Event type", Arrays.asList(EVENT_MIN, EVENT_MAX),
				EVENT_MIN);

		Double meanUnc = meanUncertainty(obs);
		String muInitial = meanUnc != null ? NumericPrecisionPrefs.formatOther(meanUnc) : "";
		TextField muField = new TextField("Photometric noise μ (empty = estimate)", muInitial, false, true);

		Checkbox resampleBox = new Checkbox("Resample if not equidistant", true);

		List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();
		fields.add(nfoldField);
		fields.add(t1Field);
		fields.add(eventField);
		fields.add(muField);
		fields.add(resampleBox);

		MultiEntryComponentDialog dialog = new MultiEntryComponentDialog("Kwee–van Woerden parameters", getDocName(),
				fields, Optional.empty());
		if (dialog.isCancelled()) {
			return null;
		}

		KweeVanWoerdenLib.Params params = new KweeVanWoerdenLib.Params();
		String nfoldSel = nfoldField.getValue();
		if (FOLDS_3.equals(nfoldSel)) {
			params.nfold = 3;
		} else if (FOLDS_7.equals(nfoldSel)) {
			params.nfold = 7;
		} else {
			params.nfold = 5;
		}

		params.t1Mode = T1_EXTREMUM.equals(t1Field.getValue()) ? KweeVanWoerdenLib.T1Mode.EXTREMUM
				: KweeVanWoerdenLib.T1Mode.MIDPOINT;

		boolean eventMin = EVENT_MIN.equals(eventField.getValue());
		String muText = muField.getValue() != null ? muField.getValue().trim() : "";
		if (muText.length() > 0) {
			try {
				double mu = NumberParser.parseDouble(muText);
				if (mu < 0) {
					MessageBox.showErrorDialog(getDisplayName(), "Photometric noise μ must be non-negative.");
					return null;
				}
				params.mu = mu;
			} catch (Exception e) {
				MessageBox.showErrorDialog(getDisplayName(), "Invalid photometric noise μ value.");
				return null;
			}
		} else {
			params.mu = null;
		}
		params.resampleIfNeeded = Boolean.TRUE.equals(resampleBox.getValue());

		ParamsDialogResult result = new ParamsDialogResult();
		result.params = params;
		result.eventMinimum = eventMin;
		return result;
	}

	private void showResultDialog(KweeVanWoerdenLib.Result result, SeriesType series, int nObs) {
		StringBuilder sb = new StringBuilder();
		sb.append("Series: ").append(series.getDescription()).append("\n");
		sb.append("Observations used: ").append(nObs).append("\n");
		sb.append("N (after prep): ").append(result.n).append("\n");
		sb.append("Δt (median): ").append(NumericPrecisionPrefs.formatTime(result.dt)).append(" d\n");
		sb.append("Z (pairings): ").append(result.z).append("\n");
		sb.append("Folds used in fit: ").append(result.nfoldUsed).append("\n");
		sb.append("μ used: ").append(formatSigma(result.muUsed)).append("\n");
		sb.append("\n");
		sb.append("T0 = ").append(NumericPrecisionPrefs.formatTime(result.t0)).append("\n");
		sb.append("σ (Deeg 2020) = ").append(formatSigma(result.sigmaDeeg)).append(" d\n");
		// Classic KvW σ is often undefined (negative discriminant) on low-noise data;
		// omit the line rather than printing NaN.
		if (!Double.isNaN(result.sigmaClassic)) {
			sb.append("σ (classic KvW) = ").append(formatSigma(result.sigmaClassic)).append(" d\n");
		}
		if (result.wasResampled || result.equidistanceWarning) {
			sb.append("\n");
			if (result.wasResampled) {
				sb.append("Note: light curve was linearly resampled to uniform spacing.\n");
			}
			if (result.equidistanceWarning) {
				sb.append("Warning: time spacing was not equidistant within tolerance.\n");
			}
		}
		sb.append("\n");
		sb.append("S(T) fold samples (time, S):\n");
		for (KweeVanWoerdenLib.FoldSample sample : result.foldSamples) {
			sb.append("  ").append(NumericPrecisionPrefs.formatTime(sample.time)).append("  ")
					.append(String.format("%.6g", sample.s)).append("\n");
		}
		sb.append("\n");
		sb.append("Method: Kwee–van Woerden with Deeg (2020) timing errors.\n");
		sb.append("Cite: Deeg, H.J. 2020, Galaxies, 9, 1 (arXiv:2011.09231);\n");
		sb.append("Kwee & van Woerden 1956, BAN, 12, 327.\n");

		String text = sb.toString();
		List<ITextComponent<String>> fields = new ArrayList<ITextComponent<String>>();
		fields.add(new TextArea("Result", text));
		new TextDialog("Kwee–van Woerden result", fields);

		// Also place a compact CSV line on the clipboard for convenience
		String csv = String.format("T0,sigma_Deeg,sigma_classic,N,Z,mu,dt%n%s,%s,%s,%d,%d,%s,%s%n",
				NumericPrecisionPrefs.formatTime(result.t0), formatSigma(result.sigmaDeeg),
				Double.isNaN(result.sigmaClassic) ? "" : formatSigma(result.sigmaClassic), result.n, result.z,
				formatSigma(result.muUsed), NumericPrecisionPrefs.formatTime(result.dt));
		try {
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(csv), null);
		} catch (Exception e) {
			// Clipboard may be unavailable; ignore
		}
	}

	private static String formatSigma(double v) {
		if (Double.isNaN(v)) {
			return "NaN";
		}
		return String.format("%.7g", v);
	}

	private static Double meanUncertainty(List<ValidObservation> obs) {
		double sum = 0;
		int count = 0;
		for (ValidObservation ob : obs) {
			double unc = ob.getMagnitude().getUncertainty();
			if (unc > 0 && !Double.isNaN(unc)) {
				sum += unc;
				count++;
			}
		}
		if (count == 0) {
			return null;
		}
		return sum / count;
	}

	private static class ParamsDialogResult {
		KweeVanWoerdenLib.Params params;
		boolean eventMinimum;
	}
}
