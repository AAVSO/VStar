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
// - Avoid copying any data at all, i.e. skip load_raw().
// - Also be able to retrieve (via getters) info in header of generated
// .ts file, e.g.
// DCDFT File=delcep.vis NUM= 3079 AVE= 3.9213 SDV=0.2235 VAR= 0.0500
// JD 2450000.2569-2450999.7097 T.AVE=2450446.0000

public class DateCompensatedDiscreteFourierTransform extends TSBase implements
		IPeriodAnalysisAlgorithm {

	private boolean specifyParameters;

	private double loFreqValue;
	private double hiFreqValue;
	private double resolutionValue;

	private Map<PeriodAnalysisCoordinateType, List<Double>> resultSeries;

	// -------------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param observations
	 *            The observations over which to perform a period analysis.
	 */
	public DateCompensatedDiscreteFourierTransform(
			List<ValidObservation> observations) {
		super(observations);

		specifyParameters = false;

		this.resultSeries = new TreeMap<PeriodAnalysisCoordinateType, List<Double>>();
		for (PeriodAnalysisCoordinateType type : PeriodAnalysisCoordinateType
				.values()) {
			this.resultSeries.put(type, new ArrayList<Double>());
		}

		load_raw();
	}

	/**
	 * Constructor
	 * 
	 * @param observations
	 *            The observations over which to perform a period analysis.
	 * @param specifyParameters
	 *            Does the caller want to specify parameters (frequency range,
	 *            resolution).
	 */
	public DateCompensatedDiscreteFourierTransform(
			List<ValidObservation> observations, boolean specifyParameters) {
		this(observations);
		this.specifyParameters = specifyParameters;
		if (specifyParameters) {
			// Set the default parameters for the specified dataset
			// (frequency range and resolution).
			determineDefaultParameters();
		}
	}

	/**
	 * Constructor
	 * 
	 * As per last constructor except that we override the
	 * 
	 * @param observations
	 *            The observations over which to perform a period analysis.
	 * @param loFreq
	 *            The low frequency value for the range to be scanned.
	 * @param hiFreq
	 *            The high frequency value for the range to be scanned.
	 * @param resolution
	 *            The resolution with which to scan over the range.
	 */
	public DateCompensatedDiscreteFourierTransform(
			List<ValidObservation> observations, double loFreq, double hiFreq,
			double resolution) {
		this(observations, true);
		setHiFreqValue(hiFreq);
		setLoFreqValue(loFreq);
		setResolutionValue(resolution);
	}

	// -------------------------------------------------------------------------------

	/**
	 * @return the loFreqValue
	 */
	public double getLoFreqValue() {
		return loFreqValue;
	}

	/**
	 * @param loFreqValue
	 *            the loFreqValue to set
	 */
	public void setLoFreqValue(double loFreqValue) {
		this.loFreqValue = loFreqValue;
	}

	/**
	 * @return the hiFreqValue
	 */
	public double getHiFreqValue() {
		return hiFreqValue;
	}

	/**
	 * @param hiFreqValue
	 *            the hiFreqValue to set
	 */
	public void setHiFreqValue(double hiFreqValue) {
		this.hiFreqValue = hiFreqValue;
	}

	/**
	 * @return the resolutionValue
	 */
	public double getResolutionValue() {
		return resolutionValue;
	}

	/**
	 * @param resolutionValue
	 *            the resolutionValue to set
	 */
	public void setResolutionValue(double resolutionValue) {
		this.resolutionValue = resolutionValue;
	}

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
	 * Return the "top hits" from the period analysis.
	 * 
	 * It is a precondition that results have been generated, i.e. the execute()
	 * method has been invoked.
	 */
	public Map<PeriodAnalysisCoordinateType, List<Double>> getTopHits() {
		Map<PeriodAnalysisCoordinateType, List<Double>> topHits = new TreeMap<PeriodAnalysisCoordinateType, List<Double>>();

		for (PeriodAnalysisCoordinateType type : PeriodAnalysisCoordinateType
				.values()) {
			topHits.put(type, new ArrayList<Double>());
		}

		for (int i = 1; i <= 20; i++) {
			if (dgnu[i] != 0) {
				topHits.get(PeriodAnalysisCoordinateType.FREQUENCY)
						.add(dgnu[i]);
				topHits.get(PeriodAnalysisCoordinateType.PERIOD).add(dgper[i]);
				topHits.get(PeriodAnalysisCoordinateType.POWER).add(dgpower[i]);
				topHits.get(PeriodAnalysisCoordinateType.AMPLITUDE).add(
						dgamplitude[i]);
			} else {
				// We've seen a zero frequency which indicates this is the end
				// of line of top hits.
				break;
			}
		}

		return topHits;
	}

	// -------------------------------------------------------------------------------

	/**
	 * Perform a "standard scan" or "frequency range" based DC DFT, a date
	 * compensated discrete Fourier transform.
	 */
	public void execute() {
		dcdft();
		statcomp();
	}

	// -------------------------------------------------------------------------------

	protected double dcdftCommon() {
		int magres;
		double dpolyamp2, dang0, dang00, damplit, dt, dx;
		npoly = 0;
		nbrake = 0;
		dpolyamp2 = 0.0; // added Apr 7
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

		return dang0;
	}

	// For use in conjunction with frequency_range().
	public void determineDefaultParameters() {
		double xlofre, res, xloper, hiper;
		int iff, ixx, nbest;

		double dang0 = dcdftCommon();

		// Initial values.
		xlofre = 0.0;
		hifre = 0;
		res = 0;

		nfre = 1;
		ndim = npoly + (2 * nfre);

		// write(6,261) dfloat(ndim+1)*dang0/2.0
		// read[5][260] xlofre;
		this.loFreqValue = (double) (ndim + 1) * dang0 / 2.0;

		// write(6,262) dfloat(numact)*dang0
		// read[5][260] hifre;
		this.hiFreqValue = (double) (numact) * dang0;

		// write(6,263) dang0
		// read[5][260] res;
		this.resolutionValue = dang0;
	}

	protected void dcdft() {
		if (!specifyParameters) {
			standard_scan(dcdftCommon());
		} else {
			// dcdftCommon() has already been called in
			// determineDefaultParameters()
			// via the specify-parameters form of the constructor.
			frequency_range(this.resolutionValue);
		}
	}

	// -------------------------------------------------------------------------------

	// DC DFT as standard scan.
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
	}

	// DC DFT with frequency range and resolution specified.
	// TODO: dang0 not required here?
	protected void frequency_range(double dang0) {
		double xlofre, res, xloper, hiper, dpolyamp2;
		int iff, ixx, nbest;

		dpolyamp2 = 0.0; // added Apr 7

		nbest = 20;

		// write(6,261) dfloat(ndim+1)*dang0/2.0
		// read[5][260] xlofre;
		xlofre = this.loFreqValue;

		// write(6,262) dfloat(numact)*dang0
		// read[5][260] hifre;
		hifre = this.hiFreqValue;

		// write(6,263) dang0
		// read[5][260] res;
		res = this.resolutionValue;

		hiper = 0.0;
		if (xlofre != 0.0)
			hiper = 1.0 / xlofre;
		xloper = 0.0;
		if (hifre != 0.0)
			xloper = 1.0 / hifre;

		if (hifre > xlofre) {
			// write(1,290) fprint,numact,dave,dsig,dvar
			// write(1,292) dt0+tvec(nlolim),dt0+tvec(nuplim),dt0+dtzero
			// call lognow
			// write(1,201)
			iff = (int) ((hifre - xlofre) / res) + 1;
			for (ixx = 1; ixx <= iff; ixx++) {
				ff = xlofre + (double) (ixx - 1) * res;
				fft(ff);
				if (nbrake < 0) {
					statcomp();
					return;
				}
			}
		} else {
			ff = 1.0 / xloper;
			fft(ff);
			dgpower[nbest] = 0.0;
			tablit();
		}

		// TODO: doesn't appear to be necessary
		dfouramp2 = dpolyamp2; // added Apr 7
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
		// System.out.println(String.format("%14.9f%10.4f%10.4f%10.4f", ff, pp,¯
		// dfpow, dd));
		collect_datapoint(ff, pp, dfpow, dd);
		// end of bugfix
		// dbenn Note: without seeing the previous revision, it's
		// not possible to know what this fix was.
		if (damp < dlamp && dlamp >= dllamp)
			tablit();
		dllamp = dlamp;
		dlamp = damp;
		dlnu = ff;
		dlper = pp;
		dlpower = dfpow;
		dlamplitude = dd;
	}

	/**
	 * Collect a single <frequency, period, power, amplitude> tuple result as a
	 * data-point.
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
					dgamplitude[nqq + 1] = dgamplitude[nqq];
				}
				dgpower[nq] = dlpower;
				dgnu[nq] = dlnu;
				dgper[nq] = dlper;
				dgamplitude[nq] = dlamplitude;
				return;
			}
		}
	}
}