/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.util.period.dcdft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.TSBase;
import org.aavso.tools.vstar.util.comparator.RankedIndexPairComparator;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisAlgorithm;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;

/**
 * This class computes a Date Compensated Discrete Fourier Transform over an
 * observation list.
 * 
 * This is a Java translation of Fortran code from ts1201.f by M. Templeton,
 * which in turn is based upon BASIC code by G. Foster, AAVSO.
 * 
 * References (supplied by M. Templeton):
 * 
 * <ol>
 * <li>
 * Ferraz-Mello, S., 1981, Estimation of Periods from Unequally Spaced
 * Observations, Astron. Journal 86, 619
 * (http://adsabs.harvard.edu/abs/1981AJ.....86..619F)</li>
 * <li>
 * Foster, G., 1995, Time Series Analysis by Projection. II. Tensor Methods for
 * Time Series Analysis, Astron. Journal 111, 555
 * (http://adsabs.harvard.edu/abs/1996AJ....111..555F)</li>
 * <li>
 * http://www.aavso.org/aavso/meetings/spring03present/templeton.shtml</li>
 * </ol>
 */

// TODO:
// - If possible, remove weights array.
// - If possible, avoid having to copy any data at all, i.e. skip load_raw().
// - Also be able to retrieve via getters info in header of generated
// .ts file, e.g.
// DCDFT File=delcep.vis NUM= 3079 AVE= 3.9213 SDV=0.2235 VAR= 0.0500
// JD 2450000.2569-2450999.7097 T.AVE=2450446.0000
// - Double check with Matt that we are doing the date compensation
// part in dcdft() here.
// - Is DCDFT primarily intended to solve the aliasing problem? See
// cataclysmic variable book ref.
// - How to properly deal with the many large frequencies? Skip, given
// some user-definable threshold or other criteria? See also requirements.

public class DateCompensatedDiscreteFourierTransform extends TSBase implements
		IPeriodAnalysisAlgorithm {

	private Map<PeriodAnalysisCoordinateType, List<Double>> resultSeries;

	// -------------------------------------------------------------------------------

	/**
	 * Constructor Note: In future, we may want to specify a min and max JD
	 * here, and a frequency range (use inclusive range class?) and resolution.
	 * 
	 * @param observations
	 *            The observations over which to perform a period analysis.
	 */
	public DateCompensatedDiscreteFourierTransform(
			List<ValidObservation> observations) {
		super(observations);

		// Create result collections.

		this.resultSeries = new TreeMap<PeriodAnalysisCoordinateType, List<Double>>();
		for (PeriodAnalysisCoordinateType type : PeriodAnalysisCoordinateType
				.values()) {
			this.resultSeries.put(type, new ArrayList<Double>());
		}
	}

	// -------------------------------------------------------------------------------

	/**
	 * @return the adjusted time vector.
	 */
	public double[] getAdjustedJDs() {
		return tvec;
	}

	/**
	 * @return the magnitude vector.
	 */
	public double[] getMags() {
		return xvec;
	}

	/**
	 * @return the weight vector.
	 */
	public double[] getWeights() {
		return wvec;
	}

	/**
	 * @return the resultSeries
	 */
	public Map<PeriodAnalysisCoordinateType, List<Double>> getResultSeries() {
		return resultSeries;
	}

	// -------------------------------------------------------------------------------

	/**
	 * Perform a "standard scan", a date compensated discrete Fourier transform.
	 */
	public void execute() {
		load_raw();
		dcdft();
		statcomp();
	}

	// -------------------------------------------------------------------------------

	/**
	 * From the resulting data, create an array of rank-index pairs
	 * (first and second elements respectively) sorted by rank, where
	 * rank could be power or amplitude.
	 * 
	 * It is a precondition that results have been generated, i.e. the
	 * execute() method has been invoked. 
	 */
	public double[][] getTopNRankedIndices(int topN) {
		assert !this.resultSeries.keySet().isEmpty();
		
		// Create an array of doubles where the first element is amplitude, and
		// the second is the common index into all result lists (frequency,
		// period, power, amplitude).
		int n = this.resultSeries.get(PeriodAnalysisCoordinateType.AMPLITUDE)
				.size();
		double[][] topRankedIndexArray = new double[n][2];

		for (int i = 0; i < n; i++) {
			topRankedIndexArray[i][0] = this.resultSeries.get(
					PeriodAnalysisCoordinateType.AMPLITUDE).get(i);

			topRankedIndexArray[i][1] = i;
		}
		
		Arrays.sort(topRankedIndexArray, RankedIndexPairComparator.instance);

		return topRankedIndexArray;
	}

	// -------------------------------------------------------------------------------

	protected void dcdft() {
		int magres, nchoice;
		double dpolyamp2, dang0, dang00, damplit, dt, dx, hifre, xlofre; // TODO:
		// should
		// we
		// use
		// this
		// dt, dang0
		// or
		// global?
		int nbest;

		npoly = 0;
		nbest = 20;
		nbrake = 0;
		dpolyamp2 = 0.0;
		dfouramp2 = 0.0;

		statcomp();

		dang0 = 1.0 / Math.sqrt(12.0 * dtvar) / 4.0;
		dang00 = dang0;
		magres = 1;
		dangcut = 0.95 * dang0;
		damplit = (int) ((double) (mb - ma) / 2.0) + 1.0;
		dt = (tvec[nuplim] - tvec[nlolim]) / (double) numact;
		if (dt <= 0.0)
			dt = 1.0; // TODO: this will never be communicated!

		standard_scan(dang0);
	}

	// -------------------------------------------------------------------------------

	protected void standard_scan(double dang0) {
		nfre = 1;
		hifre = (double) numact * dang0;
		for (nj = 1 + npoly; nj <= numact; nj++) {
			ff = (double) nj * dang0;
			fft(ff);
			// TODO: nbrake is never set to anything other than 0!!
			if (nbrake < 0) {
				statcomp();
				return;
			}
		}
		return;
	}

	/**
	 * Compute a FFT.
	 * 
	 * @param ff
	 *            The frequency.
	 */
	protected void fft(double ff) {
		double pp = 0;

		int na, nb;
		double dd;

		if (ff != 0.0)
			pp = 1.0 / ff; // TODO: what should the default/else pp value be?
		dfre[nfre] = ff;
		project();
		// G. Foster bugfix, May 2003
		na = npoly + 1;
		nb = na + 1;
		dd = Math.sqrt(dcoef[na] * dcoef[na] + dcoef[nb] * dcoef[nb]);
		// System.out.println(String.format("%14.9f%10.4f%10.4f%10.4f", ff, pp,
		// dfpow, dd));
		collect_datapoint(ff, pp, dfpow, dd);
		// end of bugfix
		if (damp < dlamp && dlamp >= dllamp)
			tablit();
		dllamp = dlamp;
		dlamp = damp;
		dlnu = ff;
		dlper = pp;
		dlpower = dfpow;
	}

	/**
	 * Collect a single <frequency, period, power, amplitude> result as a
	 * data-point and as elements of an array in a series map.
	 * 
	 * @param freq
	 *            The frequency.
	 * @param period
	 *            The period.
	 * @param power
	 *            The power.
	 * @param amplitude
	 *            The amplitude.
	 */
	private void collect_datapoint(double freq, double period, double power,
			double amplitude) {
		this.resultSeries.get(PeriodAnalysisCoordinateType.FREQUENCY).add(freq);
		this.resultSeries.get(PeriodAnalysisCoordinateType.PERIOD).add(period);
		this.resultSeries.get(PeriodAnalysisCoordinateType.POWER).add(power);
		this.resultSeries.get(PeriodAnalysisCoordinateType.AMPLITUDE).add(
				amplitude);
	}

	// -------------------------------------------------------------------------------

	protected void tablit() {
		int nq = 0;
		int nqq = 0;

		for (nq = 1; nq <= 20; nq++) {
			if (dlpower > dgpower[nq]) {
				for (nqq = 19; nqq >= nq; nqq--) {
					dgpower[nqq + 1] = dgpower[nqq];
					dgnu[nqq + 1] = dgnu[nqq];
					dgper[nqq + 1] = dgper[nqq];
				}
				dgpower[nq] = dlpower;
				dgnu[nq] = dlnu;
				dgper[nq] = dlper;
				return;
			}
		}
	}
}