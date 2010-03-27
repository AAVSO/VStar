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

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.IPeriodAnalysisAlgorithm;
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
// - Create an interface that this class implements, e.g.
// void execute(), Map<String, List<DcDftDataPoint>> getPlotSeriesMap(),
// where the key is a series name and the values are a set of points
// to be plotted, void PlotType getPlotType(), where PlotType is an
// enum. This would allow us to have arbitrary analysis types, plots,
// - Don't create arrays if we don't have to; instead pull everything we
// can out of the obs list.
// - If possible, remove weights array.
// - If possible, avoid having to copy any data at all, i.e. skip load_raw().
// - Also be able to retrieve via getters info in header of generated
// .ts file, e.g.
// DCDFT File=delcep.vis NUM= 3079 AVE= 3.9213 SDV=
// 0.2235 VAR= 0.0500
// JD 2450000.2569-2450999.7097 T.AVE=2450446.0000
// - Double check with Matt that we are doing the date compensation
// part in dcdft() here.
// - Is DCDFT primarily intended to solve the aliasing problem? See
// cataclysmic variable book ref.
// How to properly deal with the many large frequencies? Skip, given
// some user-definable threshold or other criteria? See also requirements.

public class DateCompensatedDiscreteFourierTransform implements
		IPeriodAnalysisAlgorithm {

	private List<ValidObservation> observations;
	private Map<PeriodAnalysisCoordinateType, List<Double>> resultSeries;

	private double dangcut;
	private double damp;
	private double damp2;
	private double dave;
	private double dcoef[] = new double[51];
	private double dfouramp2;
	private double dfpow;
	private double dfre[] = new double[21];
	private double dgnu[] = new double[21];
	private double dgper[] = new double[21];
	private double dgpower[] = new double[21];
	private double dlamp;
	private double dllamp;
	private double dlnu;
	private double dlper;
	private double dlpower;
	private double dmat[][] = new double[51][51];
	private double dpower;
	private double dsig;
	private double dt0;
	private double dtsig;
	private double dtave;
	private double dtscale;
	private double dtvar;
	private double dtzero;
	private double dvar;
	private double dvec[] = new double[51];
	private double dweight;
	private double ff;
	private double hifre;
	private int ma;
	private int magspan;
	private int mb;
	private int mhigh;
	private int mlow;
	private int nbias;
	private int nbrake;
	private int ndigt;
	private int ndim;
	private int ndim2;
	private int nfre;
	private int nj;
	private int nlolim;
	private int npoly;
	private int numact;
	private int numraw;
	private int nuplim;
	private String obias[] = new String[51];
	private String obs[];
	private double tlolim;
	private double tmark;
	private double tresolv;
	private double tuplim;
	private double tuplimit;
	private double tvec[];
	private double wvec[];
	private double xvec[];

	// -------------------------------------------------------------------------------

	/**
	 * Constructor Note: In future, we may want to specify a min and max JD
	 * here.
	 * 
	 * @param observations
	 *            The observations over which to perform a period analysis.
	 */
	public DateCompensatedDiscreteFourierTransform(
			List<ValidObservation> observations) {
		this.observations = observations;

		// TODO: change to zero index start to get rid of +1!
		int sz = observations.size() + 1;
		this.obs = new String[sz];
		this.tvec = new double[sz];
		this.xvec = new double[sz];
		this.wvec = new double[sz];

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

	protected void load_raw() {

		double dtspan, x, dd, dtcorr, dx;
		double jda, jdb;
		int num, n;
		double deetee, deex;

		ma = 999999;
		mb = -999999;
		num = 0;
		jda = observations.get(0).getJD();
		jdb = observations.get(observations.size() - 1).getJD();

		for (ValidObservation observation : this.observations) {
			num = num + 1;
			deetee = observation.getJD();
			deex = observation.getMag();
			tvec[num] = deetee;
			xvec[num] = deex;
			if (num == 1)
				dt0 = (int) (tvec[num]); // TODO: do this once at start?

			tvec[num] = tvec[num] - dt0;
			wvec[num] = 1.0;
			obs[num] = "    ";
			if (tvec[num] < tvec[num - 1]) {
				dx = tvec[num];
				x = xvec[num];
				boolean skip_n_gets0 = false;
				for (n = num - 1; n >= 1 && !skip_n_gets0; n--) {
					if (dx >= tvec[n]) {
						skip_n_gets0 = true;
						break;
					}
					tvec[n + 1] = tvec[n];
					xvec[n + 1] = xvec[n];
				}
				if (!skip_n_gets0) {
					n = 0;
				}
				tvec[n + 1] = dx;
				xvec[n + 1] = x;
			}
			if (xvec[num] < (double) ma)
				ma = (int) (xvec[num]);
			if (xvec[num] > (double) mb)
				mb = (int) (xvec[num]);
		}
		ma = ma - 1;
		mb = mb + 2;
		mlow = ma;
		mhigh = mb;
		magspan = mhigh - mlow;
		numraw = num;
		if (tvec[1] < 0.0) {
			dx = (int) (-1.0 * tvec[1]) + 1.0;
			dt0 = dt0 - dx;
			for (n = 1; n <= num; n++) {
				tvec[n] = tvec[n] + dx;
			}
		}
		dtspan = tvec[numraw];
		if (dtspan < 1.0)
			dtspan = 1.0;
		x = Math.log10(dtspan);
		x = (int) (x - 0.5);
		ndigt = 7 - (int) x;
		tresolv = Math.pow(10.0, x);
		dd = (int) (dt0 / tresolv) - 1.0;
		dd = dd * tresolv;
		dtcorr = dt0 - dd;
		dt0 = dd;
		for (n = 1; n <= numraw; n++) {
			tvec[n] = tvec[n] + dtcorr;
		}
		x = (int) (tvec[numraw] / tresolv) + 2.0;
		tuplimit = x * tresolv;
		tlolim = 0.0;
		tuplim = tuplimit;
		nlolim = 1;
		nuplim = numraw;
		tmark = 0.0;

		statcomp();
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

	protected void matinv() {
		double dsol[][] = new double[101][101];
		double dfac = 0;
		int ni = 0;
		int nj = 0;
		int nk = 0;

		for (ni = 0; ni <= ndim; ni++) {
			for (nj = 0; nj <= ndim; nj++) {
				dsol[ni][nj] = 0.0;
			}
			dsol[ni][ni] = 1.0;
		}

		for (ni = 0; ni <= ndim; ni++) {
			if (dmat[ni][ni] == 0.0) {
				if (ni == ndim)
					return;
				boolean exit_and_carry_on = false;
				for (nj = ni + 1; nj <= ndim; nj++) {
					if (dmat[nj][ni] != 0.0) {
						exit_and_carry_on = true;
						break;
					}
				}

				if (!exit_and_carry_on)
					return;

				for (nk = 0; nk <= ndim; nk++) {
					dmat[ni][nk] = dmat[ni][nk] + dmat[nj][nk];
					dsol[ni][nk] = dsol[ni][nk] + dsol[nj][nk];
				}
			}

			dfac = dmat[ni][ni];
			for (nj = 0; nj <= ndim; nj++) {
				dmat[ni][nj] = dmat[ni][nj] / dfac;
				dsol[ni][nj] = dsol[ni][nj] / dfac;
			}

			for (nj = 0; nj <= ndim; nj++) {
				if (nj != ni) {
					dfac = dmat[nj][ni];
					for (nk = 0; nk <= ndim; nk++) {
						dmat[nj][nk] = dmat[nj][nk] - (dmat[ni][nk] * dfac);
						dsol[nj][nk] = dsol[nj][nk] - (dsol[ni][nk] * dfac);
					}
				}
			}
		}

		for (ni = 0; ni <= ndim; ni++) {
			for (nj = 0; nj <= ndim; nj++) {
				dmat[ni][nj] = dsol[ni][nj];
			}
		}
	}

	// -------------------------------------------------------------------------------

	protected void project() {
		double dpow[] = new double[51]; // TODO just 50 (0:50); same for others
		// below?
		double drad[] = new double[51];
		double dcc[] = new double[51];
		double dss[] = new double[51];
		double dt, dx, dphase, twopi;
		int n, n1, n2, nf, nf2, nb, np;

		int ii, jj;

		twopi = 6.283185307179586;

		for (ii = 0; ii <= 50; ii++) {
			for (jj = 0; jj <= 50; jj++) {
				dmat[ii][jj] = 0.0;
			}
			dvec[ii] = 0.0;
		}

		ndim2 = npoly + (2 * nfre);
		ndim = ndim2 + nbias;
		dweight = 0.0;

		for (n1 = 0; n1 <= ndim; n1++) {
			dvec[n1] = 0.0;
			for (n2 = 0; n2 <= ndim; n2++) {
				dmat[n1][n2] = 0.0;
			}
		}

		for (nf = 1; nf <= nfre; nf++) {
			if (dfre[nf] < dangcut) {
				dfpow = 0.0;
				dpower = 0.0;
				return;
			}
			drad[nf] = twopi * dfre[nf] * dtscale;
			for (nf2 = nf + 1; nf2 <= nfre; nf2++) {
				if (Math.abs(dfre[nf] - dfre[nf2]) < 1E-8) {
					dpower = 0.0;
					return;
				}
			}
		}

		dpow[0] = 1.0;

		// main loop for summation
		for (n = nlolim; n <= nuplim; n++) {
			if (wvec[n] > 0.0) {
				dweight = dweight + 1.0;
				dt = tvec[n];
				dt = (dt - dtzero) / dtscale;
				dx = xvec[n];

				// compute powers of time
				for (np = 1; np <= npoly; np++) {
					dpow[np] = dpow[np - 1] * dt;
				}

				// compute trig functions
				for (nf = 1; nf <= nfre; nf++) {
					dphase = drad[nf] * dt;
					dcc[nf] = Math.cos(dphase);
					dss[nf] = Math.sin(dphase);
				}

				// compute matrix coefficients for polynomials...
				for (np = 0; np <= npoly; np++) {
					dmat[0][np] = dmat[0][np] + dpow[np];
					if (np > 0) {
						dmat[np][npoly] = dmat[np][npoly]
								+ (dpow[np] * dpow[npoly]);
					}

					dvec[np] = dvec[np] + (dx * dpow[np]);
					n2 = npoly;
					// ...and for products of polynomials with trig functions
					for (nf = 1; nf <= nfre; nf++) {
						n2 = n2 + 2;
						dmat[np][n2 - 1] = dmat[np][n2 - 1]
								+ (dpow[np] * dcc[nf]);
						dmat[np][n2] = dmat[np][n2] + (dpow[np] * dss[nf]);
					}
				}

				// compute matrix values for products of trig functions
				n1 = npoly;
				for (nf = 1; nf <= nfre; nf++) {
					n2 = n1;
					n1 = n1 + 2;
					dvec[n1 - 1] = dvec[n1 - 1] + (dx * dcc[nf]);
					dvec[n1] = dvec[n1] + (dx * dss[nf]);
					for (nf2 = nf; nf2 <= nfre; nf2++) {
						n2 = n2 + 2;
						dmat[n1 - 1][n2 - 1] = dmat[n1 - 1][n2 - 1]
								+ (dcc[nf] * dcc[nf2]);
						dmat[n1 - 1][n2] = dmat[n1 - 1][n2]
								+ (dcc[nf] * dss[nf2]);
						dmat[n1][n2 - 1] = dmat[n1][n2 - 1]
								+ (dss[nf] * dcc[nf2]);
						dmat[n1][n2] = dmat[n1][n2] + (dss[nf] * dss[nf2]);
					}
				}

				// compute matrix entries for observer bias functions
				for (nb = 1; nb <= nbias; nb++) {
					if (obs[n] == obias[nb]) {
						n2 = ndim2 + nb;
						dmat[n2][n2] = dmat[n2][n2] + 1.0;
						dvec[n2] = dvec[n2] + dx;
						for (np = 0; np <= npoly; np++) {
							dmat[np][n2] = dmat[np][n2] + dpow[np];
						}
						n1 = npoly;
						for (nf = 1; nf <= nfre; nf++) {
							n1 = n1 + 2;
							dmat[n1 - 1][n2] = dmat[n1 - 1][n2] + dcc[nf];
							dmat[n1][n2] = dmat[n1][n2] + dss[nf];
						}
					}
				}
			}

		}
		// end of summation loop

		// check for absent bias observers
		// TODO: needed?
		for (n = 1; n <= nbias; n++) {
			if (dmat[ndim2 + n][ndim2 + n] < 1.0) {
				// write(6,*) 'absent BIAS Obs: ',obias(n)
				ndim = ndim2;
				nbias = 0;
			}
		}

		for (n1 = 1; n1 <= npoly - 1; n1++) {
			for (n2 = n1; n2 <= npoly - 1; n2++) {
				dmat[n1][n2] = dmat[n1 - 1][n2 + 1];
			}
		}

		for (n1 = 0; n1 <= ndim; n1++) {
			dvec[n1] = dvec[n1] / dweight;
			for (n2 = n1; n2 <= ndim; n2++) {
				dmat[n1][n2] = dmat[n1][n2] / dweight;
			}
		}

		dmat[0][0] = 1.0;
		for (n1 = 1; n1 <= ndim; n1++) {
			for (n2 = 0; n2 <= n1 - 1; n2++) {
				dmat[n1][n2] = dmat[n2][n1];
			}
		}

		matinv();

		damp2 = 0.0;
		for (n1 = 0; n1 <= ndim; n1++) {
			dcoef[n1] = 0.0;
			for (n2 = 0; n2 <= ndim; n2++) {
				dcoef[n1] = dcoef[n1] + (dmat[n1][n2] * dvec[n2]);
			}
			damp2 = damp2 + (dcoef[n1] * dvec[n1]);
		}

		damp2 = damp2 - (dave * dave);
		if (damp2 < 0.0)
			damp2 = 0.0;
		if (ndim > 0) {
			dpower = (double) (numact - 1) * damp2 / dvar / (double) (ndim);
		} else {
			dpower = 0.0;
		}

		// compute Fourier power, amplitude squared

		dfpow = (double) (numact - 1) * (damp2 - dfouramp2);
		dfpow = dfpow / (dvar - dfouramp2) / 2.0;
		damp = 2.0 * (damp2 - dfouramp2);
		if (damp < 0.0)
			damp = 0.0;
		damp = Math.sqrt(damp);
	}

	// -------------------------------------------------------------------------------

	protected void statcomp() {
		int n, nx;
		double dw, dt, dx, dtspan, xx, dts2;

		setlimit();

		nbrake = 0;
		numact = 0;
		dweight = 0.0;
		dave = 0.0;
		dtave = 0.0;
		dtvar = 0.0;

		for (n = nlolim; n <= nuplim; n++) {
			if (wvec[n] > 0.0) {
				numact = numact + 1;
				dw = wvec[n];
				dt = tvec[n];
				dx = xvec[n];
				dweight = dweight + dw;
				dave = dave + (dw * dx);
				dvar = dvar + ((dw * dx) * (dw * dx));
				dtave = dtave + dt;
				dtvar = dtvar + (dt * dt);
			}
		}

		if (numact < 1) {
			dave = 0.0;
			dvar = 0.0;
			dsig = 0.0;
			return;
		}

		dave = dave / dweight;
		dvar = dvar / dweight;
		dvar = dvar - (dave * dave);

		if (dvar < 0.0)
			dvar = 0.0;
		dsig = 0.0;

		if (numact > 1)
			dsig = Math.sqrt(dvar * (double) (numact) / (double) (numact - 1));

		dtave = dtave / (double) (numact);
		dtvar = (dtvar / (double) (numact)) - (dtave * dtave);

		if (dtvar < 0.0)
			dtvar = 0.0;

		dtsig = Math.sqrt(dtvar);
		dtspan = tvec[nuplim] - tvec[nlolim];

		if (dtspan <= 0.0)
			return;

		xx = Math.log10(dtspan);
		nx = (int) (xx + 0.5);
		dtscale = Math.pow(10.0, nx);
		dts2 = Math.pow(10.0, (nx - 3));
		dtzero = dtave / dts2;
		dtzero = dts2 * (int) (dtzero + 0.5);
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

	// -------------------------------------------------------------------------------

	protected void setlimit() {
		int n;

		nlolim = 0;
		nuplim = 0;

		for (n = 1; n <= numraw; n++) {
			if (tvec[n] >= tlolim)
				break;
		}

		nlolim = n;
		for (n = nlolim; n <= numraw; n++) {
			if (tvec[n] > tuplim)
				break;
		}

		nuplim = n - 1;
	}
}
