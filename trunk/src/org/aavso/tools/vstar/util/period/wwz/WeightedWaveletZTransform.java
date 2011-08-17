/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2011  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.util.period.wwz;

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.util.IAlgorithm;

/**
 * <p>
 * This is a Java translation of the Fortran version of Grant Foster's WWZ
 * algorithm. The original (C) notice from the Fortran code is included below.
 * As per the documentation accompanying the program, written (email) permission
 * was sought from the AAVSO Director to use the Fortran code in this way, and
 * granted. Note that the comment about maximum data points below does not apply
 * here, since we dynamically allocate the arrays based upon the number of
 * observations.
 * </p>
 * 
 * <p>
 * WEIGHTED WAVELET Z-TRANSFORM This is a fortran version of Grant Foster's
 * WWZ1.1.BAS BASIC program, stripped down for speed and readability
 * improvements. It is modestly less flexible than the basic code (esp regarding
 * varying input file formats), but will be easy to modify to suit your needs.
 * In the event you have more than 100000 data points, you can resize all arrays
 * to meet your needs -- the only limitation is your free memory. (Note that all
 * variables are double precision, though.) A description of the mathematics can
 * be found in G. Foster, "Wavelets for Period Analysis of Unevenly Sampled Time
 * Series", Astronomical Journal 112, 1709 (Oct 1996).<br/>
 * -Matthew Templeton, August 15, 2002
 * </p>
 * 
 * <p>
 * (C) Copyright 1996, 2002 by the American Association of Variable Star
 * Observers; all rights reserved.
 * </p>
 */
public class WeightedWaveletZTransform implements IAlgorithm {
	private List<WWZStatistic> stats;
	private List<WWZStatistic> maximalStats;

	private double dave;
	private double dcc;
	private double dcon;
	private double dcw;
	private double df;
	private double dmat[][] = new double[3][3];
	private double dsig;
	private double dss;
	private double dsw;
	private double dt[];
	private double dvar;
	private double dx[];
	private double dxw;
	private double fhi;
	private double flo;
	private double freq[];
	private int nfreq;
	private int ntau;
	private int numdat;
	private double tau[];
	private double taucheck;

	/**
	 * Constructor
	 * 
	 * @param observations
	 *            The observations over which to perform a period analysis.
	 * @param minFreq
	 *            The minimum frequency to test.
	 * @param maxFreq
	 *            The maximum frequency to test.
	 * @param deltaFreq
	 *            The frequency step.
	 * @param decay
	 *            The decay constant of the wavelet window. This determines how
	 *            wide the window is. Smaller values yield wider windows.
	 */
	public WeightedWaveletZTransform(List<ValidObservation> observations,
			double minFreq, double maxFreq, double deltaFreq, double decay) {

		dataread(observations);

		flo = minFreq;
		fhi = maxFreq;
		df = deltaFreq;
		dcon = decay;

		stats = new ArrayList<WWZStatistic>();
		maximalStats = new ArrayList<WWZStatistic>();

		maketau();
		makefreq();
	}

	/**
	 * Construct a WWZ algorithm object with a default decay value.
	 * 
	 * @param observations
	 *            The observations over which to perform a period analysis.
	 * @param minFreq
	 *            The minimum frequency to test.
	 * @param maxFreq
	 *            The maximum frequency to test.
	 */
	public WeightedWaveletZTransform(List<ValidObservation> observations,
			double minFreq, double maxFreq, double deltaFreq) {
		this(observations, minFreq, maxFreq, deltaFreq, 0.001);
	}

	/**
	 * Execute the WWZ algorithm on the specified observations with the
	 * specified frequency range and window size.
	 */
	@Override
	public void execute() throws AlgorithmError {
		wwt();
	}

	/**
	 * @return the stats
	 */
	public List<WWZStatistic> getStats() {
		return stats;
	}

	/**
	 * @return the maximalStats
	 */
	public List<WWZStatistic> getMaximalStats() {
		return maximalStats;
	}

	/**
	 * Reads data from the specified observation list.
	 * 
	 * TODO: We may want to consider saving memory by simply reading from the
	 * observation list directly. How much would this slow things down? At the
	 * very least, we should size the arrays according to the size of the obs
	 * list!
	 * 
	 * <p>
	 * This method also computes the number of data points (numdat) the average
	 * (dave), the variance (dvar), and the standard deviation (dsig).
	 * </p>
	 */
	private void dataread(List<ValidObservation> observations) {

		numdat = observations.size();

		dt = new double[numdat + 1];
		dx = new double[numdat + 1];

		freq = new double[numdat + 1];
		tau = new double[numdat + 1];

		for (int i = 1; i <= observations.size(); i++) {
			ValidObservation ob = observations.get(i - 1);
			dt[i] = ob.getJD();
			dx[i] = ob.getMag();

			dave = dave + dx[i];
			dvar = dvar + (dx[i] * dx[i]);
		}

		// Note: unlikely to make use of these in VStar, since we do these kinds
		// of computations elsewhere.
		dave = dave / ((double) numdat);
		dvar = (dvar / ((double) numdat)) - (dave * dave);
		dsig = Math.sqrt(dvar * ((double) (numdat)) / (double) (numdat - 1));
	}

	/**
	 * make your array of time lags, tau, here. In BASIC, you could just step
	 * from whatever taus you choose, like "FOR DTAU = DTAU1 TO DTAU2 STEP DT",
	 * but FORTRAN only lets you loop over ints. So we determine the taus ahead
	 * of time. TODO: notice that we could have done this the Basic for-step way
	 * here.
	 */
	private void maketau() {
		double dtspan, dtstep, dtaulo, dtauhi;

		dtspan = dt[numdat] - dt[1];
		dtstep = round(dtspan / 50.0);
		dtaulo = dt[1];
		dtauhi = dt[numdat];
		dtaulo = dtstep * (double) ((int) ((dtaulo / dtstep) + 0.5));
		dtauhi = dtstep * (double) ((int) ((dtauhi / dtstep) + 0.5));
		tau[1] = dtaulo;
		ntau = 1;
		for (int i = 2; i <= numdat; i++) {
			taucheck = tau[1] + (double) (i - 1) * dtstep;
			if (taucheck > dtauhi)
				break;
			tau[i] = taucheck;
			ntau++;
		}
	}

	/**
	 * Rounds the taus... from G. Foster's code.
	 * 
	 * TODO: we may be able to replace this with a Math class function, but
	 * possibly not, since this seems to be categorising its arguments into
	 * ranges >2..5, >1..2, <=1
	 * 
	 * @param darg
	 *            The argument to be rounded.
	 * @return The rounded value.
	 */
	private double round(double darg) {
		double dex = Math.log10(darg);
		int nex = (int) dex;

		darg = darg / Math.pow(10.0, nex);

		if (darg >= 5.0) {
			darg = 5.0;
		} else if (darg >= 2.0) {
			darg = 2.0;
		} else {
			darg = 1.0;
		}

		darg = darg * Math.pow(10, nex);

		return darg;
	}

	private void makefreq() {
		// TODO: guard against 0 frequency, initially throw exception, but later
		// in UI; then assert non-zero here; we'll get NaNs with 0.

		nfreq = (int) ((fhi - flo) / df) + 1;

		// depending upon the user inputs, you may wind up with a lot of
		// frequencies.
		// this will query in case nfreq>1000 (nfreq=1000 is a reasonable
		// number)
		if (nfreq > 1000) {
			// TODO: throw exception for now; instead, do this check in WWZ
			// parameter dialog, opening a JOptionPane to ask whether this is
			// okay
			throw new IllegalArgumentException(
					"Number of frequencies is greater than 1000.");
		}

		freq[1] = flo;
		for (int i = 2; i <= nfreq; i++) {
			freq[i] = freq[1] + (double) (i - 1) * df;
		}
	}

	/**
	 * Invert the matrix of the wwz equations...
	 */
	private void matinv() {
		double dsol[][] = new double[3][3];// (0:2,0:2);
		double dfac;

		int ndim = 2;

		for (int i = 0; i <= 2; i++) {
			for (int j = 0; j <= 2; j++) {
				dsol[i][j] = 0.0;
			}
			dsol[i][i] = 1.0;
		}

		for (int i = 0; i <= ndim; i++) {
			if (dmat[i][i] == 0.0) {
				if (i == ndim)
					return;
				for (int j = i + 1; j <= ndim; j++) {
					if (dmat[j][i] != 0.0) {
						for (int k = 0; k <= ndim; k++) {
							dmat[i][k] = dmat[i][k] + dmat[j][k];
							dsol[i][j] = dsol[i][j] + dsol[j][k];
						}
					}
				}
			}
			dfac = dmat[i][i];
			for (int j = 0; j <= ndim; j++) {
				dmat[i][j] = dmat[i][j] / dfac;
				dsol[i][j] = dsol[i][j] / dfac;
			}
			for (int j = 0; j <= ndim; j++) {
				if (j != i) {
					dfac = dmat[j][i];
					for (int k = 0; k <= ndim; k++) {
						dmat[j][k] = dmat[j][k] - (dmat[i][k] * dfac);
						dsol[j][k] = dsol[j][k] - (dsol[i][k] * dfac);
					}
				}
			}
		}
		for (int i = 0; i <= ndim; i++) {
			for (int j = 0; j <= ndim; j++) {
				dmat[i][j] = dsol[i][j];
			}
		}
	}

	private void wwt() {
		double dvec[] = new double[3];
		double dcoef[] = new double[3];
		int itau, ifreq, idat;
		double domega, dweight2, dz, dweight;
		double dcc, dcw, dss, dsw, dxw, dvarw;
		double dtau;
		double dpower, dpowz, damp, dneff, davew;
		double dfre;
		// dnefff should probably be dneff, but we'll keep it for strict
		// consistency...
		// TODO: Is the use of this variable a bug? It is set to zero once below
		// but never used otherwise. It may be that it should be dneff indeed!
		double dnefff;
		int n1, n2;

		double dmz, dmfre, dmamp, dmcon, dmneff;

		dvarw = 0.0; // TODO: added
		dweight2 = 0.0; // TODO: added
		dfre = 0.0; // TODO: added

		double twopi = 2.0 * Math.PI;

		int ndim = 2;
		int itau1 = 1;
		int itau2 = ntau;
		int ifreq1 = 1;
		int ifreq2 = nfreq;
		int nstart = 1;

		for (itau = itau1; itau <= itau2; itau++) {
			nstart = 1;
			dtau = tau[itau];

			// TODO: added
			// Initialise maximal stat values at the start of each tau.
			dmfre = 0.0;
			dmamp = 0.0;
			dmcon = 0.0;
			dmneff = 0.0;
			dmz = -1.0; // less than the smallest WWZ

			for (ifreq = ifreq1; ifreq <= ifreq2; ifreq++) {
				dfre = freq[ifreq];
				domega = dfre * twopi;
				for (int i = 0; i <= ndim; i++) {
					dvec[i] = 0.0;
					for (int j = 0; j <= ndim; j++) {
						dmat[i][j] = 0.0;
					}
				}
				dweight2 = 0.0;

				for (idat = nstart; idat <= numdat; idat++) {
					dz = domega * (dt[idat] - dtau);
					dweight = Math.exp(-1.0 * dcon * dz * dz);
					if (dweight > 1.0e-9) {
						dcc = Math.cos(dz);
						dcw = dweight * dcc;
						dss = Math.sin(dz);
						dsw = dweight * dss;
						dmat[0][0] = dmat[0][0] + dweight;
						dweight2 = dweight2 + (dweight * dweight);
						dmat[0][1] = dmat[0][1] + dcw;
						dmat[0][2] = dmat[0][2] + dsw;
						dmat[1][1] = dmat[1][1] + (dcw * dcc);
						dmat[1][2] = dmat[1][2] + (dcw * dss);
						dmat[2][2] = dmat[2][2] + (dsw * dss);
						dxw = dweight * dx[idat];
						dvec[0] = dvec[0] + dxw;
						dvarw = dvarw + (dxw * dx[idat]);
						dvec[1] = dvec[1] + (dcw * dx[idat]);
						dvec[2] = dvec[2] + (dsw * dx[idat]);
					} else if (dz > 0.0) {
						break;
					} else {
						nstart = idat + 1;
					}
				}

				dpower = 0.0;
				damp = 0.0;
				for (n1 = 0; n1 <= ndim; n1++) {
					dcoef[n1] = 0.0;
				}
				if (dweight2 > 0.0) {
					dneff = (dmat[0][0] * dmat[0][0]) / dweight2;
				} else {
					dneff = 0.0;
				}
				if (dneff > 3.0) {
					for (n1 = 0; n1 <= ndim; n1++) {
						dvec[n1] = dvec[n1] / dmat[0][0];
						for (n2 = 1; n2 <= ndim; n2++) {
							dmat[n1][n2] = dmat[n1][n2] / dmat[0][0];
						}
					}
					if (dmat[0][0] > 0.0) {
						dvarw = dvarw / dmat[0][0];
					} else {
						dvarw = 0.0;
					}
					dmat[0][0] = 1.0;
					davew = dvec[0];
					dvarw = dvarw - (davew * davew);
					if (dvarw <= 0.0)
						dvarw = 1.0e-12;
					for (n1 = 1; n1 <= ndim; n1++) {
						for (n2 = 0; n2 <= n1 - 1; n2++) {
							dmat[n1][n2] = dmat[n2][n1];
						}
					}

					matinv();

					for (n1 = 0; n1 <= ndim; n1++) {
						for (n2 = 0; n2 <= ndim; n2++) {
							dcoef[n1] = dcoef[n1] + dmat[n1][n2] * dvec[n2];
						}
						dpower = dpower + (dcoef[n1] * dvec[n1]);
					}
					dpower = dpower - (davew * davew);
					dpowz = (dneff - 3.0) * dpower / (dvarw - dpower) / 2.0;
					dpower = (dneff - 1.0) * dpower / dvarw / 2.0;
					damp = Math.sqrt(dcoef[1] * dcoef[1] + dcoef[2] * dcoef[2]);
				} else {
					dpowz = 0.0;
					dpower = 0.0;
					damp = 0.0;
					if (dneff < 1.0e-9)
						// TODO: this looks like a bug! should be dneff
						// dnefff = 0.0;
						dneff = 0.0;
				}
				if (damp < 1.0e-9)
					damp = 0.0;
				if (dpower < 1.0e-9)
					dpower = 0.0;
				if (dpowz < 1.0e-9)
					dpowz = 0.0;

				// Record one WWZ statistic per frequency per tau.
				WWZStatistic stat = new WWZStatistic(dtau, dfre, dpowz, damp,
						dcoef[0], dneff);

				stats.add(stat);

				if (dpowz > dmz) {
					dmz = dpowz;
					dmfre = dfre;
					dmamp = damp;
					dmcon = dcoef[0];
					dmneff = dneff;
				}
			}

			// Record the frequency for which the WWZ is maximal.
			WWZStatistic maximalStat = new WWZStatistic(dtau, dmfre, dmz,
					dmamp, dmcon, dmneff);

			maximalStats.add(maximalStat);
		}
	}
}
