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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.util.TSBase;
import org.aavso.tools.vstar.util.model.PeriodAnalysisDerivedMultiPeriodicModel;
import org.aavso.tools.vstar.util.model.PeriodFitParameters;
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

public class TSDcDft extends TSBase implements IPeriodAnalysisAlgorithm {

	private DcDftAnalysisType analysisType;

	// Parameter values (by frequency or period).
	private double loFreqValue;
	private double hiFreqValue;
	private double loPeriodValue;
	private double hiPeriodValue;
	private double resolutionValue;

	private double dang0;

	private int nbest;

	private Map<PeriodAnalysisCoordinateType, List<Double>> resultSeries;
	private Map<PeriodAnalysisCoordinateType, List<Double>> topHits;
	private List<PeriodAnalysisDataPoint> deltaTopHits;

	// -------------------------------------------------------------------------------

	/**
	 * Constructor
	 * 
	 * @param observations
	 *            The observations over which to perform a period analysis.
	 */
	public TSDcDft(List<ValidObservation> observations) {
		super(observations);

		this.analysisType = DcDftAnalysisType.STANDARD_SCAN;

		resultSeries = new TreeMap<PeriodAnalysisCoordinateType, List<Double>>();
		for (PeriodAnalysisCoordinateType type : PeriodAnalysisCoordinateType
				.values()) {
			resultSeries.put(type, new ArrayList<Double>());
		}

		deltaTopHits = new ArrayList<PeriodAnalysisDataPoint>();

		load_raw();
	}

	/**
	 * Constructor
	 * 
	 * The analysis type is specified.
	 * 
	 * @param observations
	 *            The observations over which to perform a period analysis.
	 * @param analysisType
	 *            The type of analysis to be carried out: standard scan,
	 *            frequency range, period range.
	 */
	public TSDcDft(List<ValidObservation> observations,
			DcDftAnalysisType analysisType) {
		this(observations);

		this.analysisType = analysisType;

		if (analysisType == DcDftAnalysisType.FREQUENCY_RANGE) {
			// Set the default parameters for the specified dataset
			// (frequency range and resolution).
			determineDefaultParameters();
		}
	}

	/**
	 * Constructor
	 * 
	 * As per last constructor except that we override the parameter values and
	 * request a frequency range analysis type.
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
	public TSDcDft(List<ValidObservation> observations, double loFreq,
			double hiFreq, double resolution) {
		this(observations, DcDftAnalysisType.FREQUENCY_RANGE);
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
	 * @return the loPeriodValue
	 */
	public double getLoPeriodValue() {
		return loPeriodValue;
	}

	/**
	 * @param loPeriodValue
	 *            the loPeriodValue to set
	 */
	public void setLoPeriodValue(double loPeriodValue) {
		this.loPeriodValue = loPeriodValue;
	}

	/**
	 * @return the hiPeriodValue
	 */
	public double getHiPeriodValue() {
		return hiPeriodValue;
	}

	/**
	 * @param hiPeriodValue
	 *            the hiPeriodValue to set
	 */
	public void setHiPeriodValue(double hiPeriodValue) {
		this.hiPeriodValue = hiPeriodValue;
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

		// Create top-hits collection.
		topHits = new TreeMap<PeriodAnalysisCoordinateType, List<Double>>();

		for (PeriodAnalysisCoordinateType type : PeriodAnalysisCoordinateType
				.values()) {
			topHits.put(type, new ArrayList<Double>());
		}

		for (int i = 1; i <= MAX_TOP_HITS - 1; i++) {
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
	@Override
	public void execute() throws AlgorithmError {
		dcdft();
		// TODO: why is this statcomp() here!?
		// In TS, it's called only after the Fourier menu has been exited!
		// statcomp();
	}

	@Override
	public List<PeriodAnalysisDataPoint> refineByFrequency(List<Double> freqs,
			List<Integer> harmonics, List<Double> variablePeriods,
			List<Double> lockedPeriods) throws AlgorithmError {

		deltaTopHits.clear();
		cleanest(freqs, harmonics, variablePeriods, lockedPeriods);

		return deltaTopHits;
	}

	@Override
	public String getRefineByFrequencyName() {
		return "CLEANest";
	}

	// -------------------------------------------------------------------------------

	protected void dcdftCommon() {
		int magres;
		double dpolyamp2, dang00, damplit, dt, dx;
		npoly = 0;
		nbest = MAX_TOP_HITS - 1;
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
	}

	// For use in conjunction with frequency_range().
	public void determineDefaultParameters() {
		double xlofre, res, xloper, hiper;
		int iff, ixx;

		dcdftCommon();

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
		switch (analysisType) {
		case STANDARD_SCAN:
			dcdftCommon();
			standard_scan();
			break;
		case FREQUENCY_RANGE:
			// dcdftCommon() has already been called in
			// determineDefaultParameters()
			// via the specify-parameters form of the constructor.
			frequency_range();
			break;
		case PERIOD_RANGE:
			dcdftCommon();
			period_range();
			break;
		}
	}

	// -------------------------------------------------------------------------------

	// DC DFT as standard scan.
	protected void standard_scan() {
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
	protected void frequency_range() {
		double xlofre, res, xloper, hiper, dpolyamp2;
		int iff, ixx;

		dpolyamp2 = 0.0; // added Apr 7

		// write(6,261) dfloat(ndim+1)*dang0/2.0
		// read[5][260] xlofre;
		xlofre = this.loFreqValue;

		// write(6,262) dfloat(numact)*dang0
		// read[5][260] hifre;
		hifre = this.hiFreqValue;

		// write(6,263) dang0
		// read[5][260] res;
		res = this.resolutionValue;

		hiper = 0.0; // hiper not used!
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

	// DC DFT with period range and resolution specified.
	protected void period_range() {
		double xlofre, res, xloper, hiper, pper;
		int ipp, ixx;

		nfre = 1;

		xloper = getLoPeriodValue();
		hiper = getHiPeriodValue();

		// Question: why "< 0.0"?
		if (hiper < 0.0) {
			hiper = xloper;
			res = 1.0;
		} else {
			res = getResolutionValue();
		}
		
		hifre = 0.0;

		if (xloper != 0.0) {
			hifre = 1.0 / xloper;
		}

		xlofre = 0.0;

		if (hiper != 0.0) {
			xlofre = 1.0 / hiper;
		}
		
		// write(1,290) fprint,numact,dave,dsig,dvar
		// write(1,292) dt0+tvec(nlolim),dt0+tvec(nuplim),dt0+dtzero
		// call lognow
		// write(1,201)

		if (hiper >= (xloper + res)) {
			ipp = (int) ((hiper - xloper) / res) + 1;
			for (ixx = 1; ixx <= ipp; ixx++) {
				pper = xloper + ((double) (ixx - 1) * res);

				if (pper != 0.0) {
					ff = 1.0 / pper;
				}
				
				fft(ff);
				
				if (nbrake < 0) {
					statcomp();
					break;
				}
			}
		} else {
			ff = 1.0 / xloper;
			fft(ff);
			dgpower[nbest] = 0.0;
			tablit();
		}
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

	/**
	 * A translation of the Fortran TS CLEANest algorithm.
	 * 
	 * @param freqs
	 *            The user-specified frequencies to be included.
	 * @param harmonics
	 *            The maximum number of harmonics per user-specified frequency
	 *            to be found and included in the analysis. May be null or
	 *            empty.
	 * @param varPeriods
	 *            The variable periods to be included. May be null or empty.
	 * @param lockedPeriods
	 *            The locked periods to be included. May be null or empty.
	 * 
	 *            TODO: it would be more consistent to pass freqs as periods!
	 */
	protected void cleanest(List<Double> freqs, List<Integer> harmonics,
			List<Double> variablePeriods, List<Double> lockedPeriods)
			throws AlgorithmError {
		// getfreq();

		int varCount = variablePeriods == null ? 0 : variablePeriods.size();
		int lockedCount = lockedPeriods == null ? 0 : lockedPeriods.size();
		int totalCount = freqs.size() + varCount + lockedCount;

		// Convert frequencies to be considered to Fortran array index form.
		// TODO: we should just dispense with 1-originated arrays and use
		// 0-originated arrays.
		dfre = new double[totalCount + 1];
		// dfre = new double[MAX_TOP_HITS];
		for (int i = 1; i <= freqs.size(); i++) {
			dfre[i] = freqs.get(i - 1);
		}

		double[] dtest = new double[totalCount + 1];
		// double[] dtest = new double[MAX_TOP_HITS];
		double[] dres = new double[totalCount + 1];
		// double[] dres = new double[MAX_TOP_HITS];

		// Initialise arrays with user specified frequencies, converting to
		// periods.
		for (int n = 1; n <= freqs.size(); n++) {
			dtest[n] = 1.0 / dfre[n];
			dres[n] = (dang0 * (dtest[n] * dtest[n])) / 10.0;
			ResolutionResult result = resolve(dres[n], dtest[n]);

			if (result != null) {
				dres[n] = result.ddr;
				dtest[n] = result.ddp;
				// System.out.println(String.format("After resolve: %1.6f, %1.6f",
				// dres[n], dtest[n]));
			} else {
				throw new AlgorithmError("No resolution result");
			}
		}

		nfre = freqs.size();

		// ** Add variable periods. **
		// TODO: should dtest array elements just be 0?
		if (variablePeriods != null) {
			for (double period : variablePeriods) {
				nfre++;
				dres[nfre] = period;
			}
		}

		// // write(6,*) 'enter number of variable periods: (0 for none)'
		//
		// read*,nvariable
		// ;
		// if (nvariable > 0) {
		// for (int ixx = 1;ixx <= nvariable;ixx++) {
		// nfre=nfre+1
		// ;
		// // write(6,*) 'please enter var. per. #',ixx
		//
		// read*,dres[nfre]
		// ;
		// }
		// }

		// Store the max index of variable periods.
		int nvary = nfre;

		// ** Get locked periods. **
		if (lockedPeriods != null) {
			for (double period : lockedPeriods) {
				nfre++;
				dtest[nfre] = period;
				dres[nfre] = 0;
			}
		}

		assert nfre == totalCount;

		// // write(6,*) 'enter number of locked periods: (0 for none)'
		//
		// read*,nlocked
		// ;
		// if (nlocked > 0) then {
		// for (ixx = 1;ixx <= nlocked;ixx++) {
		// nfre=nfre+1
		// ;
		// // write(6,*) 'please enter locked per. #',ixx
		//
		// read*,dtest[nfre]
		// ;
		// dres[nfre] = 0.0
		// ;
		// }
		// }
		double dbpower = 0.0;

		// ** Perform multi-scan. **
		// write(1,*) 'MULTI: '

		// lognow();
		// ** Multi-period scan. **

		// Compute base level.

		for (int n = 1; n <= nfre; n++) {
			// System.out.println(String.format("Before: %1.6f, %1.6f", dfre[n],
			// dtest[n]));
			if (dtest[n] != 0) {
				dfre[n] = 1.0 / dtest[n];
			}
			// System.out.println(String.format("After: %1.6f, %1.6f", dfre[n],
			// dtest[n]));
		}
		project();
		dbpower = dfpow;
		if (dbpower == 0.0)
			dbpower = 1.0;
		int nsofar = 0;
		int nv = 0;
		int nvlast = 0;
		int nchange = 0;

		// ** Refine the periods. **
		// 81 continue

		do {
			int iswap;

			if (nchange < 0 && nvlast > 0) {
				iswap = nvlast;
				nvlast = nv;
				nv = iswap;
			} else {
				if (nchange < 0)
					nvlast = nv;
				nv = nv + 1;
				if (nv > nvary)
					nv = 1;
			}
			nchange = 0;

			// ** Test higher periods. **
			// 82 continue

			do {
				dtest[0] = dtest[nv] + dres[nv];
				dfre[nv] = 1.0 / dtest[0];
				project();
				// write(6,*) dtest(0),dfre(nv),dfpow
				// System.out.println(String.format("%1.6f  %1.6f  %1.6f",
				// dtest[0], dfre[nv], dfpow));

				if (dfpow > dbpower) {
					dbpower = dfpow;
					dtest[nv] = dtest[0];
					nchange = -1;
					nsofar = -1;
				} else {
					dfpow = 0.0;
				}
			} while (dfpow >= dbpower);

			// if (dfpow>=dbpower) goto 82 ;

			if (nchange == 0) {

				// test lower periods
				// 83 continue

				do {
					dtest[0] = dtest[nv] - dres[nv];
					dfre[nv] = 1.0 / dtest[0];
					project();
					// write(6,*) dtest(0),dfre(nv),dfpow
					// System.out.println(String.format("%1.6f  %1.6f  %1.6f",
					// dtest[0], dfre[nv], dfpow));

					if (dfpow > dbpower) {
						dbpower = dfpow;
						dtest[nv] = dtest[0];
						nsofar = -1;
						nchange = -1;
					} else {
						dfpow = 0;
					}

				} while (dfpow >= dbpower);
				// if (dfpow>=dbpower) goto 83;
			}

			dfre[nv] = 1.0 / dtest[nv];

			// for (n = 1; n <= nfre; n++) {
			// // write(1,208) dtest(n)
			// }
			// write(1,208) dbpower

			nsofar = nsofar + 1;

			// write(6,*) dbpower,nsofar

		} while (nsofar < nvary);
		// if (nsofar<nvary) goto 81;

		// ** Save best set to table. **
		dlpower = dbpower;
		for (int n = 1; n <= nfre; n++) {
			dlper = dtest[n];
			dlnu = 1.0 / dlper;
			tablit();
		}
	}

	/**
	 * Create a multi-periodic fit to the data from a list of periods.
	 * 
	 * @param periods
	 *            The periods to be used to create the fit.
	 * @param model
	 *            A multi-period fit class that takes place in the context of a
	 *            period analysis. Data members in this parameter are populated
	 *            as a result of invoking this method.
	 */
	public void multiPeriodicFit(List<Double> periods,
			PeriodAnalysisDerivedMultiPeriodicModel model) {

		List<ValidObservation> modelObs = model.getFit();
		List<ValidObservation> residualObs = model.getResiduals();
		List<PeriodFitParameters> parameters = model.getParameters();

		// CASE F6

		// 60 call getfreq
		// ;

		// Convert frequencies to be considered to Fortran array index form.
		// TODO: we should just dispense with this everywhere and use
		// 0-originated indices.
		nfre = periods.size();
		dfre = new double[nfre + 1];
		for (int i = 1; i <= nfre; i++) {
			dfre[i] = 1.0 / periods.get(i - 1);
		}

		// write(6,*) 'save residuals? (y/n)'

		// read*,rfil
		// ;
		// if (rfil=='Y'||rfil=='y') then {
		// if (rfil=='Y') rfil='y';
		// write(6,*) 'residuals filename:'

		// read*,rname
		// ;
		// open[unit=9,file=rname][status='unknown']
		// ;
		// }
		double avemod = 0.0;
		double varmod = 0.0;

		// compute coefficients
		// write(6,*) 'Computing...'

		project();

		// write(1,293) dfpow,fprint,numact,dave,dsig,dvar

		// write(1,292) dt0+tvec(nlolim),dt0+tvec(nuplim),dt0+dtzero

		// call lognow
		for (int np = 1; np <= npoly; np++) {
			// write(1,204) np,dcoef(np),dtscale
		}
		int nb = npoly + (2 * nfre);
		for (int nn = 1; nn <= nbias; nn++) {
			// write(1,205) obias(nn),dcoef(nb+nn)

		}
		// write(1,206)

		nb = npoly;
		for (int nn = 1; nn <= nfre; nn++) {
			nb = nb + 2;
			int na = nb - 1;
			double dd = dcoef[na] * dcoef[na] + dcoef[nb] * dcoef[nb];
			parameters
					.add(new PeriodFitParameters(dfre[nn], periods.get(nn - 1),
							Math.sqrt(dd), dcoef[na], dcoef[nb], dcoef[0]));
			// if (nn > 9) {
			// write(1,207) dfre(nn),1.0/dfre(nn),nn,dsqrt(dd),dcoef(na),

			// 1 dcoef(nb),dcoef(0)
			// ;
			// } else {
			// ;
			// write(1,277) dfre(nn),1.0/dfre(nn),nn,dsqrt(dd),dcoef(na),

			// 1 dcoef(nb),dcoef(0)
			// ;
			// }
		}

		double ttl = 0.0;
		double xml = 0.0;
		double residl = 0.0;

		// compute and plot points
		for (int n = nlolim; n <= nuplim; n++) {
			if (nbrake < 0)
				break;
			if (wvec[n] > 0.0) {
				double tt = tvec[n];
				double dt = tt;
				double dx = smooth(dt);
				double xm = dx;
				double resid = xvec[n] - xm;
				// TODO: permit bias to be added in model creation
				for (nb = 1; nb <= nbias; nb++) {
					if (obs[n] == obias[nb])
						resid = resid - dcoef[ndim2 + nb];
				}
				// if (rfil=='y') then {
				// write(9,250) tt+dt0,resid,obs(n),xvec(n),xm

				// Create model and residual "observations".

				ValidObservation modelOb = new ValidObservation();
				modelOb.setDateInfo(new DateInfo(tt + dt0));
				modelOb.setMagnitude(new Magnitude(xm, 0));
				modelOb.setComments(model.getDescription());
				modelOb.setBand(SeriesType.Model);
				modelObs.add(modelOb);

				ValidObservation residualOb = new ValidObservation();
				residualOb.setDateInfo(new DateInfo(tt + dt0));
				residualOb.setMagnitude(new Magnitude(resid, 0));
				residualOb.setComments(model.getDescription());
				residualOb.setBand(SeriesType.Residuals);
				residualObs.add(residualOb);

				// }
				ttl = tt;
				xml = xm;
				residl = resid;
				avemod = avemod + resid;
				varmod = varmod + (resid * resid);
			}
		}
		// close[9]
		avemod = avemod / (double) numact;
		varmod = varmod / (double) (numact - 1);
		double rdev = Math.sqrt(varmod - avemod * avemod);
	}

	// -------------------------------------------------------------------------------

	protected ResolutionResult resolve(double ddr, double ddp) {
		// implicit none

		// double ddr,ddp
		// int nexp

		int nexp = 0;
		if (ddr == 0.0)
			return null;
		// 10 if(ddr<1.0) then
		if (ddr < 1.0) {
			while (ddr < 1.0) {
				ddr = ddr * 10.0;
				nexp = nexp - 1;
				// goto 10
			}
		} else {
			// 11 if(ddr>10.0) then
			while (ddr > 10.0) {
				ddr = ddr / 10.0;
				nexp = nexp + 1;
				// goto 11
			}
		}

		if (ddr >= 1.0 && ddr < 2.0)
			ddr = 1.0;
		if (ddr >= 2.0 && ddr < 5.0)
			ddr = 2.0;
		if (ddr >= 5.0)
			ddr = 5.0;
		ddr = ddr * (Math.pow(10.0, nexp));
		ddp = ddp / ddr;
		ddp = ddr * ((int) (ddp + 0.5));

		return new ResolutionResult(ddr, ddp);
	}

	class ResolutionResult {
		public double ddr;
		public double ddp;

		public ResolutionResult(double ddr, double ddp) {
			super();
			this.ddr = ddr;
			this.ddp = ddp;
		}
	}

	// -------------------------------------------------------------------------------

	// TODO: I think we will also want to add as many top hits as requested
	// straight to the topHits map in a little bit...; the key difference from
	// deltaTopHits is that topHits isn't cleared between calls to tablit();
	// may want a list of PeriodAnalysisDataPoints instead.

	protected void tablit() {
		int nq = 0;
		int nqq = 0;

		for (nq = 1; nq <= MAX_TOP_HITS - 1; nq++) {
			// We have found a higher power!
			if (dlpower > dgpower[nq]) {
				// Move everything below this down one.
				// Note that with a list, we'll just be able to insert!
				for (nqq = MAX_TOP_HITS - 2; nqq >= nq; nqq--) {
					dgpower[nqq + 1] = dgpower[nqq];
					dgnu[nqq + 1] = dgnu[nqq];
					dgper[nqq + 1] = dgper[nqq];
					dgamplitude[nqq + 1] = dgamplitude[nqq];
				}
				// Replace the current element with the new highest.
				dgpower[nq] = dlpower;
				dgnu[nq] = dlnu;
				dgper[nq] = dlper;
				dgamplitude[nq] = dlamplitude;

				// Capture this new value.
				deltaTopHits.add(new PeriodAnalysisDataPoint(dlnu, dlper,
						dlpower, dlamplitude));

				return;
			}
		}
	}
}