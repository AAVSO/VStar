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
package org.aavso.tools.vstar.util.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.aavso.tools.vstar.util.locale.NumberParser;

/**
 * This class computes relative amplitudes and phases from a set of multi-period
 * fit model coefficients in the presence of harmonics, grouped by fundamental
 * frequency, and makes them available in different string formats.
 */
public class RelativeAmplitudeAndPhaseCreator {

	private List<PeriodFitParameters> paramsSeq;
	private Map<Double, List<PeriodFitParameters>> harmonicMap;

	/**
	 * Constructor.
	 * 
	 * @param paramsList
	 *            A list of period fit parameters containing information about
	 *            coefficients (sine and cosine) and the frequencies (and
	 *            harmonics) that were inputs to the model.
	 */
	public RelativeAmplitudeAndPhaseCreator(
			List<PeriodFitParameters> paramsList) {
		paramsSeq = new ArrayList<PeriodFitParameters>(paramsList);
		Collections.sort(paramsSeq);

		createHarmonicMap();
	}

	/**
	 * 
	 * @return The mapping from fundamental frequencies to sorted period fit
	 *         parameter list.
	 */
	public Map<Double, List<PeriodFitParameters>> getHarmonicMap() {
		return harmonicMap;
	}

	/**
	 * Returns a list of known (mapped) fundamental frequencies.
	 * 
	 * @return The list of fundamental frequencies.
	 */
	public Set<Double> getFundamentals() {
		return harmonicMap.keySet();
	}

	/**
	 * Do any of the fundamental frequencies map to lists of harmonics, i.e.
	 * lists of period fit parameters of length > 1 (since first is always the
	 * fundamental)?
	 * 
	 * @return Whether or not any fundamentals have harmonics.
	 */
	public boolean hasHarmonics() {
		boolean result = false;

		for (Double freq : harmonicMap.keySet()) {
			if (harmonicMap.get(freq).size() > 1) {
				result = true;
				break;
			}
		}

		return result;
	}

	/**
	 * Returns a string representing the sequence of relative amplitude-phase
	 * pairs for a given fundamental frequency, at a specified precision, with
	 * the option of whether the relative phase values should be in radians or
	 * cycles.
	 * 
	 * @param fundamental
	 *            The fundamental frequency whose relative values we seek.
	 * @param precision
	 *            The required precision (decimal places).
	 * @param cycles
	 *            Whether cycles (true) or radians (false) should be returned in
	 *            the string.
	 * @return The string of space-delimited relative amplitude-phase pairs.
	 */
	public String getRelativeSequenceString(double fundamental, int precision,
			boolean cycles) {
		String str = "";

		if (harmonicMap.containsKey(fundamental)) {
			List<PeriodFitParameters> paramsList = harmonicMap.get(fundamental);

			String fmt = "%1." + precision + "f %1." + precision + "f ";

			double firstAmplitude = paramsList.get(0).getAmplitude();
			double firstPhase = paramsList.get(0).getPhase();

			for (int i = 1; i < paramsList.size(); i++) {
				PeriodFitParameters params = paramsList.get(i);

				double relativeAmplitude = params
						.getRelativeAmplitude(firstAmplitude);

				double relativePhase = Double.NaN;

				if (cycles) {
					relativePhase = params.getRelativePhaseInCycles(firstPhase);
				} else {
					relativePhase = params.getRelativePhase(firstPhase);
				}

				str += String.format(fmt, relativeAmplitude, relativePhase);
			}
		} else {
			String msg = String.format("Fundamental frequency '" + fundamental
					+ "' unknown.", fundamental);
			throw new IllegalArgumentException(msg);
		}

		return str;
	}

	/**
	 * Returns a list relative amplitudes for a given fundamental frequency.
	 * 
	 * @param fundamental
	 *            The fundamental frequency whose relative values we seek.
	 * @return The list of relative amplitudes.
	 */
	public List<Double> getRelativeAmplitudes(double fundamental) {
		List<Double> relativeAmplitudes = new ArrayList<Double>();

		if (harmonicMap.containsKey(fundamental)) {
			List<PeriodFitParameters> paramsList = harmonicMap.get(fundamental);

			double firstAmplitude = paramsList.get(0).getAmplitude();

			for (int i = 1; i < paramsList.size(); i++) {
				PeriodFitParameters params = paramsList.get(i);

				double relativeAmplitude = params
						.getRelativeAmplitude(firstAmplitude);

				relativeAmplitudes.add(relativeAmplitude);
			}
		} else {
			String msg = String.format("Fundamental frequency '" + fundamental
					+ "' unknown.", fundamental);
			throw new IllegalArgumentException(msg);
		}

		return relativeAmplitudes;
	}

	/**
	 * Returns a list relative phases for a given fundamental frequency, with
	 * the option of whether the relative phase values should be in radians or
	 * cycles.
	 * 
	 * @param fundamental
	 *            The fundamental frequency whose relative values we seek.
	 * @param cycles
	 *            Whether cycles (true) or radians (false) should be returned.
	 * @return The list of relative phases.
	 */
	public List<Double> getRelativePhases(double fundamental, boolean cycles) {
		List<Double> relativePhases = new ArrayList<Double>();

		if (harmonicMap.containsKey(fundamental)) {
			List<PeriodFitParameters> paramsList = harmonicMap.get(fundamental);

			double firstPhase = paramsList.get(0).getPhase();

			for (int i = 1; i < paramsList.size(); i++) {
				PeriodFitParameters params = paramsList.get(i);

				double relativePhase = Double.NaN;

				if (cycles) {
					relativePhase = params.getRelativePhaseInCycles(firstPhase);
				} else {
					relativePhase = params.getRelativePhase(firstPhase);
				}

				relativePhases.add(relativePhase);
			}
		} else {
			String msg = String.format("Fundamental frequency '" + fundamental
					+ "' unknown.", fundamental);
			throw new IllegalArgumentException(msg);
		}

		return relativePhases;
	}

	// Create a mapping from fundamental frequency to period fit parameters
	// ordered by harmonic number (multiple of fundamental frequency).
	private void createHarmonicMap() {
		harmonicMap = new TreeMap<Double, List<PeriodFitParameters>>();

		// Find the fundamentals, setting up the map's keys and empty lists.
		for (PeriodFitParameters params : paramsSeq) {
			if (params.getHarmonic().isFundamental()) {
				// Truncate the string to ensure we don't get rounding errors
				// when looking for harmonics.
				double fund = round(params.getHarmonic().getFrequency(), 4);
				harmonicMap.put(fund, new ArrayList<PeriodFitParameters>());
			}
		}

		// Now iterate over all parameters again, looking for frequencies
		// that have a particular fundamental, and adding each to the
		// corresponding mapped list.
		//
		// The post-condition is that all parameters whose frequencies have a
		// particular fundamental (including the fundamental itself) will be
		// grouped into a list mapped from a key which is the fundamental
		// frequency. List elements will be ordered by harmonic number iff
		// the parameters list was so ordered to begin with.
		for (PeriodFitParameters params : paramsSeq) {
			double fund = round(params.getHarmonic().getFundamentalFrequency(), 4);
			harmonicMap.get(fund).add(params);
		}
	}

	// Return the number rounded to the specified precision.
	private double round(double n, int precision) {
		String fmt = "%1." + precision + "f";
		String nStr = String.format(fmt, n);
		return NumberParser.parseDouble(nStr);
	}
}
