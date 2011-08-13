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
 * granted.
 * </p>
 * 
 * <p>
 * WEIGHTED WAVELET Z-TRANSFORM This is a fortran version of Grant Foster's
 * WWZ1.1.BAS BASIC program, stripped down for speed and readability
 * improvements. It is modestly less flexible than the basic code (esp regarding
 * varying input file formats), but will be easy to modify to suit your needs.
 * In the event you have more than MAX_DATA_POINTS data points, you can resize
 * all arrays to meet your needs -- the only limitation is your free memory.
 * (Note that all variables are double precision, though.) A description of the
 * mathematics can be found in G. Foster, "Wavelets for Period Analysis of
 * Unevenly Sampled Time Series", Astronomical Journal 112, 1709 (Oct 1996).<br/>
 * -Matthew Templeton, August 15, 2002
 * </p>
 * 
 * <p>
 * (C) Copyright 1996, 2002 by the American Association of Variable Star
 * Observers; all rights reserved.
 * </p>
 */
public class WeightedWaveletTransform implements IAlgorithm {
	// TODO: ultimately, we should be able to lift this limit, but
	// on the other hand, we are unlikely to be able load that much data anyway;
	// at least adjust according to data set!
	private final static int MAX_DATA_POINTS = 100000;

	private List<WWZStatistic> stats;
	private List<WWZStatistic> maximalStats;

	// private String choice;
	// private double damp;
	// private double darg;
	private double dave;
	// private double davew;
	private double dcc;
	private double dcon;
	private double dcw;
	// private double dex;
	private double df;
	// private double dfac;
	// private double dfre;
	// private double dmamp;
	private double dmat[][] = new double[3][3];
	// private double dmcon;
	// private double dmfre;
	// private double dmneff;
	// private double dmper;
	// private double dmpow;
	// private double dmz;
	// private double dneff;
	// private double dnefff;
	// private double domega;
	// private double dpower;
	// private double dpowz;
	private double dsig;
	// private double dsol;
	private double dss;
	private double dsw;
	private double dt[] = new double[MAX_DATA_POINTS + 1];
	// private double dtau;
	// private double dtauhi;
	// private double dtaulo;
	// private double dtspan;
	// private double dtstep;
	private double dvar;
	// private double dvarw;
	// private double dvec;
	// private double dweight;
	private double dx[] = new double[MAX_DATA_POINTS + 1];
	// private double dx;
	private double dxw;
	private double fhi;
	// private String filen;
	// private String filen;
	// private String fileo;
	private double flo;
	private double freq[] = new double[MAX_DATA_POINTS + 1];
	// private double freq;
	// private int i;
	// private int i;
	// private int idat;
	// private int ifreq;
	// private int itau;
	// private int j;
	// private int k;
	// private int n;
	// private int ndim;
	// private int ndim;
	// private double nex;
	private int nfreq;
	// private int nfreq;
	// private int ni;
	// private int nj;
	// private int nstart;
	private int ntau;
	// private int ntau;
	private int numdat;
	// private int numdat;
	private double tau[] = new double[MAX_DATA_POINTS + 1];
	private double taucheck;

	// private double twopi;

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
	public WeightedWaveletTransform(List<ValidObservation> observations,
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
	public WeightedWaveletTransform(List<ValidObservation> observations,
			double minFreq, double maxFreq, double deltaFreq) {
		this(observations, minFreq, maxFreq, deltaFreq, 0.001);
	}

	/**
	 * Execute the WWZ algorithm on the specified observations with the
	 * specified frequency range and window size.
	 */
	@Override
	public void execute() throws AlgorithmError {
		// print copyright statement from original program

		// write(6,200)

		// 200 format(/////,"    Weighted Wavelet Z-transform (WWZ)",/,

		// 1 "  (C) Copyright 1996, 2002 by the American Association",/,
		// 2 "    of Variable Star Observers; all rights reserved."
		// 3 ,////)

		// main program
		// dataread();
		// maketau();
		// makefreq();
		// getcon();
		// writehead();
		wwt();
		// write(6,*) "Program complete!"
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

	// subroutine dataread

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
		// implicit none

		// int i,numdat;
		// String filen,fileo;
		// double dt,dx;
		// double dave,dvar,dsig; // TODO: we may not need these
		// common/stardat/dt(MAX_DATA_POINTS),dx(MAX_DATA_POINTS),dave,dvar,dsig,numdat;
		// common/outname/filen;
		// print*,'input data filename'
		// read*,filen

		// open[unit=1,file=filen][status='old']
		numdat = 0;
		for (int i = 1; i <= MAX_DATA_POINTS; i++) {
			// read(1,*,end=999)dt(i),dx(i)
			ValidObservation ob = observations.get(i - 1);
			dt[i] = ob.getJD();
			dx[i] = ob.getMag();

			dave = dave + dx[i];
			dvar = dvar + (dx[i] * dx[i]);
			numdat++;
		}
		// when eof(1) is reached, the loop jumps here
		// 999 continue
		// close[1]
		dave = dave / ((double) numdat);
		dvar = (dvar / ((double) numdat)) - (dave * dave);
		dsig = Math.sqrt(dvar * ((double) (numdat)) / (double) (numdat - 1));

		// open your output file (there is only one output file in this version)
		// print*,'output wwz filename (e.g. name.wwz)'

		// read*,fileo

		// open[unit=2,file=fileo][status="unknown"]

		// open[unit=3,file='wwzper.dat'][status="unknown"]
	}

	/**
	 * make your array of time lags, tau, here. In BASIC, you could just step
	 * from whatever taus you choose, like "FOR DTAU = DTAU1 TO DTAU2 STEP DT",
	 * but FORTRAN only lets you loop over ints. So we determine the taus ahead
	 * of time. TODO: notice that we could have done this the Basic for-step way
	 * here.
	 */
	private void maketau() {
		// implicit none

		// double tau,dt,dx
		// double dave,dvar,dsig
		double dtspan, dtstep, dtaulo, dtauhi;
		// double taucheck
		// int numdat,i,j,ntau

		// common/stardat/dt(MAX_DATA_POINTS),dx(MAX_DATA_POINTS),dave,dvar,dsig,numdat
		// common/taudat/tau(MAX_DATA_POINTS),ntau

		dtspan = dt[numdat] - dt[1];
		dtstep = round(dtspan / 50.0);
		dtaulo = dt[1];
		dtauhi = dt[numdat];
		dtaulo = dtstep * (double) ((int) ((dtaulo / dtstep) + 0.5));
		dtauhi = dtstep * (double) ((int) ((dtauhi / dtstep) + 0.5));
		tau[1] = dtaulo;
		ntau = 1;
		for (int i = 2; i <= MAX_DATA_POINTS; i++) {
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
		// implicit none
		// double dex,nex,darg

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
		// query the user for a frequency range, and make an array of
		// frequencies
		// implicit none

		// double flo,fhi,df,freq
		;
		// int nfreq,i
		;
		// String choice
		;
		// common/freqdat/freq(MAX_DATA_POINTS),nfreq
		;

		// 700 continue
		;
		// print*,'low frequency (cyc/d)'

		// read*,flo

		// print*,'high frequency'

		// read*,fhi

		// print*,'delta f'

		// read*,df

		// TODO: guard against 0 frequency, initially throw exception, but later
		// in UI

		nfreq = (int) ((fhi - flo) / df) + 1;

		// depending upon the user inputs, you may wind up with a lot of
		// frequencies.
		// this will query in case nfreq>1000 (nfreq=1000 is a reasonable
		// number)
		if (nfreq > 1000) {
			// 701 print*,'you have more than 1000 frequencies. OK? (y/n)'

			// read*,choice

			// if (choice=='N'||choice=='n') goto 700;
			// if (choice!='Y'&&choice!='y') goto 701;

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
	 * Query for a decay constant, C (as in exp(-c*omega^2)), for the wavelet
	 * window.
	 */
	// private void getcon() {
	// // implicit none
	//
	// // double dcon
	// ;
	// // common/condat/dcon
	// ;
	// dcon = 0.001;
	// // print*,'input decay constant c'
	//
	// // read*,dcon
	//
	// return;
	// }

	/**
	 * Invert the matrix of the wwz equations...
	 */
	private void matinv() {
		// implicit none
		double dsol[][] = new double[3][3];// (0:2,0:2);
		double dfac;
		// int ni,nj;
		// common/matdat/dmat(0:2,0:2);

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

	// subroutine writehead

	/**
	 * Write a similar header for this output file as is done for the formatted
	 * output of the BASIC code.
	 */
	private void writehead() {
		// // implicit none
		//
		// // String filen
		// ;
		// // double tau,freq
		// ;
		// // double dt,dx
		// ;
		// // double dave,dvar,dsig
		// ;
		// // int numdat,ntau,nfreq
		// ;
		// // common/outname/filen
		// ;
		// // common/taudat/tau(MAX_DATA_POINTS),ntau
		// ;
		// // common/freqdat/freq(MAX_DATA_POINTS),nfreq
		// ;
		// //
		// common/stardat/dt(MAX_DATA_POINTS),dx(MAX_DATA_POINTS),dave,dvar,dsig,numdat
		// ;
		//    
		// // write(2,201) filen,numdat,dave,dsig,dvar
		//
		// // write(2,202) tau(1),tau(ntau),tau(2)-tau(1)
		//
		// // write(2,203) freq(1),freq(nfreq),freq(2)-freq(1)
		//
		// // write(2,204)
		//
		//    
		// // 201 format("File=",a13," NUM=",i6," AVE=",f11.4," SDV=",
		//
		// // & f11.4," VAR=",f11.4) dbenn
		// & f11.4," VAR=",f11.4)
		// ;
		// // 202 format("    From JD ",f13.4," to JD ",f13.4," step",f11.4)
		//
		// // 203 format("   From fre",f11.7," to fre",f11.7," step",f11.7)
		//
		// // 204 format(8h tau,13h freq,16h WWZ ,
		//
		// // & 12h Amp,14h m(ave),12h Neff)
		// & 12h Amp,14h m[ave],12h Neff)
		// ;
		// return
		// ;
	}

	private void wwt() {
		// implicit none

		// double dt,dx,tau,freq,twopi,dcon
		// double twopi;
		// double dvec(0:2),dmat,dcoef(0:2)
		double dvec[] = new double[3];
		double dcoef[] = new double[3];
		// double dave,dvar,dsig
		// int ndim;
		// int itau1, itau2, ifreq1, ifreq2;
		// int nfreq,ntau;
		int itau, ifreq, idat;
		// int nstart;
		// int numdat;
		// int i,j,itau,ifreq,idat,nstart,numdat
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

		double dmz, dmfre, dmper, dmpow, dmamp, dmcon, dmneff;

		// common/stardat/dt(MAX_DATA_POINTS),dx(MAX_DATA_POINTS),dave,dvar,dsig,numdat
		// common/taudat/tau(MAX_DATA_POINTS),ntau
		// common/freqdat/freq(MAX_DATA_POINTS),nfreq
		// common/constants/twopi
		// common/condat/dcon
		// common/matdat/dmat(0:2,0:2)

		dvarw = 0.0; // TODO: added; remove?
		dweight2 = 0.0; // TODO: added; remove?
		dfre = 0.0; // TODO: added; remove?

		// double twopi=2.0*Math.acos(-1.0);
		double twopi = 2.0 * Math.PI;

		int ndim = 2;
		int itau1 = 1;
		int itau2 = ntau;
		int ifreq1 = 1;
		int ifreq2 = nfreq;
		int nstart = 1;

		boolean dzIsGreaterThantZero = false;

		WWZStatistic maximalStat = null;
		
		for (itau = itau1; itau <= itau2 && !dzIsGreaterThantZero; itau++) {
			nstart = 1;
			dtau = tau[itau];

			dmz = 0.0;
			for (ifreq = ifreq1; ifreq <= ifreq2 && !dzIsGreaterThantZero; ifreq++) {
				dfre = freq[ifreq];
				domega = dfre * twopi;
				for (int i = 0; i <= ndim; i++) {
					dvec[i] = 0.0;
					for (int j = 0; j <= ndim; j++) {
						dmat[i][j] = 0.0;
					}
				}
				dweight2 = 0.0;

				for (idat = nstart; idat <= numdat && !dzIsGreaterThantZero; idat++) {
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
						// goto 902
						// Break out of all the loops.
						dzIsGreaterThantZero = true;
					} else {
						nstart = idat + 1;
					}
				}

				// go here once dz>0 (In BASIC version, this is the "OUTLOOP"
				// section of SUB WWZ)
				// 902 continue
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
						dnefff = 0.0;
				}
				if (damp < 1.0e-9)
					damp = 0.0;
				if (dpower < 1.0e-9)
					dpower = 0.0;
				if (dpowz < 1.0e-9)
					dpowz = 0.0;

				// now, write everything out -- one write per frequency per
				// tau...
				// write(2,205) dtau,dfre,dpowz,damp,dcoef(0),dneff
				// 205 format(f12.4,2x,f10.7,4(2x,f11.4))
				
				WWZStatistic stat = new WWZStatistic(dtau, dfre, dpowz, damp, dcoef[0],
						dneff); 
				
				stats.add(stat);

				if (dpowz > dmz) {
					dmz = dpowz;
					dmfre = dfre;
					dmper = 1.0 / dfre;
					dmpow = dpower;
					dmamp = damp;
					dmcon = dcoef[0];
					dmneff = dneff;
					maximalStat = stat;
				}
			}
			// write(3,206) dtau,dmper,dmamp,dmcon,dmfre,dmz,dmneff
			// 206 format(f13.4,f11.4,f14.4,f11.4,f11.7,2(f11.4))
			
			if (maximalStat != null) {
				maximalStats.add(maximalStat);
			}
		}
	}
}
