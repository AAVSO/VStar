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
package org.aavso.tools.vstar.util.polyfit;

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.TSBase;

/**
 * This is a Java translation of the Fortran polymast subroutine from the
 * AAVSO's ts1201.f by Matthew Templeton and Grant Foster.
 */
public class TSPolynomialFitter extends TSBase implements IPolynomialFitter {

	private int degree;
	private int numred;

	private double[] tfit;
	private double[] xfit;

	private List<ValidObservation> fit;
	private List<ValidObservation> residuals;

	/**
	 * Constructor.
	 * 
	 * @param observations
	 *            The list of observations (a single band makes most sense) to
	 *            which the polynomial fit is to be applied.
	 * @param degree
	 *            The degree of the polynomial.
	 */
	public TSPolynomialFitter(List<ValidObservation> observations, int degree) {
		super(observations);
		this.degree = degree;
		this.tfit = new double[observations.size() + 1];
		this.xfit = new double[observations.size() + 1];
	}

	/**
	 * @see org.aavso.tools.vstar.util.IAlgorithm#execute()
	 */
	@Override
	public void execute() {
		// Load the observation data and perform a polynomial fitting operation
		// of the specified degree.
		load_raw();
		polymast(degree);
	}

	/**
	 * @see org.aavso.tools.vstar.util.polyfit.IPolynomialFitter#getFit()
	 */
	@Override
	public List<ValidObservation> getFit() {
		return fit;
	}

	/**
	 * @see org.aavso.tools.vstar.util.polyfit.IPolynomialFitter#getResiduals()
	 */
	@Override
	public List<ValidObservation> getResiduals() {
		return residuals;
	}

	void polymast(int polyDeg) {
		// implicit none

		// common arrays

		// String fin,flog,fprint
		// int iname;
		// common/name/fin,flog;

		// double tvec,xvec,wvec;
		// common/datapts/tvec(1000000),xvec(1000000),wvec(1000000);

		// int nfit;
		// double tfit,xfit,sfit;
		// String obs;
		// common/fitpts/tfit(1000000),xfit(1000000),sfit(1000000),;
		// 1 nfit(1000000),obs(1000000);

		// double dmat,dvec,dcoef;
		// String obias;
		// common/matproj/dmat(0:50,0:50),dvec(0:50),dcoef(0:50),obias(0:50);

		// double dgnu,dgper,dgpower,dfre;
		// common/fourarr/dgnu(20),dgper(20),dgpower(20),dfre(20);

		// common scalars
		// double damp,damp2,dangcut,dave,dfpow,dfouramp2,dpower;
		// double dt0,dtave,dtscale,dtsig,dtvar,dtzero,dsig,dvar;
		// double dweight,dxout;
		// int nbias,nfre,npoly;
		// common/scalar1/damp,damp2,dangcut,dave,dfpow,dfouramp2,dpower,;
		// 1 dt0,dtave,dtscale,dtsig,dtvar,dtzero,dsig,dvar,;
		// 2 dweight,dxout,nbias,nfre,npoly;

		// common old scalars
		// double tcur,tlolim,tmark,toff,toffl,tput,tresolv,tsize;
		// double tuplim,tuplimit,tlozoom,tupzoom,xleft,xright,ybottom,ytop;
		// double dlamp,dllamp,dlnu,dlper,dlpower;
		// int ma,mb,magmark,magspan,mflag,mcur,mhigh,mlow,mput;
		// int mazoom,mbzoom,nactual,nbins,nbottom,nbrake,ndigt;
		// int ndim,ndim2,negf,nleft,nlolim,nocol,nxcol,nparseok;
		// int nright,ntcol,nthis,ntop;
		// int numact,numraw,numred,nuplim,nzoom;
		// common/scalar2/tcur,tlolim,tmark,toff,toffl,tput,tresolv,tsize,;
		// 1 tuplim,tuplimit,tlozoom,tupzoom,xleft,xright,;
		// 2 ybottom,ytop,dlamp,dllamp,dlnu,dlper,dlpower,;
		// 3 ma,mb,magmark,magspan,mflag,mcur,mhigh,mlow,mput,;
		// 4 mazoom,mbzoom,nactual,nbins,nbottom,nbrake,ndigt,;
		// 5 ndim,ndim2,negf,nleft,nlolim,nocol,nxcol,nparseok,;
		// 6 nright,ntcol,nthis,ntop,numact,numraw,numred,;
		// 7 nuplim,nzoom;

		// local scalars
		double ds9, dcc, res, dtime, dx;
		// String ftmp;
		int n, nb;

		// iname=index[fin][' '];
		// fprint=fin(1:iname)

		statcomp();

		assert polyDeg > 0 && polyDeg < 50;

		npoly = polyDeg;

		polyfit();

		ds9 = dvar * ((double) (numact - 1) - (double) (npoly) * dpower);
		ds9 = ds9 / (double) (numact - 1) / (double) (numact - 1 - npoly);
		if (ds9 < 0.0)
			ds9 = 0.0;
		ds9 = Math.sqrt(ds9);
		// write(1,230) npoly,dpower,ds9,fprint,numact,dave,dsig,dvar
		// write(1,292) dt0+tvec(nlolim),dt0+tvec(nuplim),dt0+dtzero

		// goto 30;
		// 1 write(6,*) 'POLYNOMIAL options:'
		// write(6,*) '0: EXIT POLYNOMIAL'
		// write(6,*) '1: Save constants'
		// write(6,*) '2: Save to file'
		// write(6,*) '4: Save residuals'

		// read*,nchoice;
		// if (nchoice==0) return;
		// if (nchoice==1) goto 10;
		// if (nchoice==2) goto 20;
		// if (nchoice==4) goto 40;
		// if (nchoice<0||nchoice>4) then {
		// write(6,*) 'Please select a valid option'
		// goto 1;
		// }

		// TODO: create functions for each Note below

		// Note: save constants

		// 10 continue;
		// write(1,290) fprint,numact,dave,dsig,dvar
		// write(1,292) dt0+tvec(nlolim),dt0+tvec(nuplim),dt0+dtzero
		// write(1,211) dtzero+dt0
		// write(1,212) dpower

		for (n = 0; n <= npoly; n++) {
			dcc = dcoef[n] / Math.pow(dtscale, n);
			// write(1,213) n,dcc
		}
		for (n = 1; n <= nbias; n++) {
			// write(1,214) obias(n),dcoef(npoly+n)
		}
		// goto 1;

		// Note: save to file

		// 20 write(1,220) npoly
		// write(1,290) fprint,numact,dave,dsig,dvar
		// write(1,292) dt0+tvec(nlolim),dt0+tvec(nuplim),dt0+dtzero
		// write(1,221) dpower

		// Store the results of the polynomial fit operation as
		// "fit observations".
		// Then we can add this as a plot series. But not in the obs list (same
		// for filtered obs).
		// TODO: Do we also want to be able to save these in a file?
		fit = new ArrayList<ValidObservation>();

		for (n = 1; n <= numred; n++) {
			// write(1,222)tfit(n)+dt0,xfit(n),ds9*sfit(n)
			ValidObservation fitOb = new ValidObservation();
			fitOb.setDateInfo(new DateInfo(tfit[n] + dt0));
			// double uncertainty = ds9*sfit[n]; // TODO: ask Matt about this;
			// uncertainty?
			fitOb.setMagnitude(new Magnitude(xfit[n], 0));
			fit.add(fitOb);
		}
		// goto 1;

		// Note: save residuals (to list)
		// Need to either create a plot or open a file?
		// Ideally we should be able to save only certain bands from he main
		// plot/list

		// 40 write(6,*) 'Residuals filename?'
		// read*,ftmp;
		// 41 open(unit=9,file=ftmp,status='unknown',err=42);
		// goto 43;
		// 42 write(6,*) 'Could not open file.'
		// goto 40;
		// 43 continue;

		// Store the residuals resulting from the polynomial fit operation as
		// "residual observations".
		// Then we can add this as a plot series. But not in the obs list (same
		// for filtered obs).
		// TODO: Do we also want to be able to save these in a file?
		residuals = new ArrayList<ValidObservation>();

		for (n = nlolim; n <= nuplim; n++) {
			if (wvec[n] > 0.0) {
				dtime = tvec[n];
				dx = smooth(dtime);
				res = xvec[n] - dx;
				for (nb = 1; nb <= nbias; nb++) {
					if (obs[n] == obias[nb])
						res = res - dcoef[npoly + nb];
				}
				// write(9,240) tvec(n)+dt0,res
				ValidObservation residualOb = new ValidObservation();
				residualOb.setDateInfo(new DateInfo(tvec[n] + dt0));
				residualOb.setMagnitude(new Magnitude(res, 0));
				residuals.add(residualOb);
			}
		}
		// close[9];
		// goto 1;
		//        
		// return;

		// 211 format(7hTime0= ,f12.4)
		// 212 format(7hPower= ,1pe12.6)
		// 213 format(i2,1x,1pd24.16)
		// 214 format(a4,1x,1pd24.16)
		// 220 format(11hPOLY DEGREE,i2)
		// 221 format(10h***POWER= ,1pd24.16)
		// 222 format(f12.4,2(1x,f10.4))
		// 230 format(4hPOLY,i2,2(1x,f10.4),1x,'File=',a13,'NUM=',i5,' AVE=',
		// 1 f11.4,' SDV=',f11.4,' VAR=',f11.4);
		// 231 format(2(1x,f10.4),1x)
		// 240 format(f12.4,1x,f10.4)

		// 290 format('POLY COEFFICIENTS',/,'File=',a13,'NUM=',i5,' AVE=',f11.4,
		// 1' SDV=',f11.4,' VAR=',f11.4);
		// 292 format(' JD ',f12.4,'-',f12.4,' T.AVE=',f12.4)
	}

	double smooth(double dtime) {
		// implicit none

		// common arrays
		// double tvec,xvec,wvec;
		// common/datapts/tvec(1000000),xvec(1000000),wvec(1000000);

		// int nfit;
		// double tfit,xfit,sfit;
		// String obs;
		// common/fitpts/tfit(1000000),xfit(1000000),sfit(1000000),;
		// 1 nfit(1000000),obs(1000000);

		// double dmat,dvec,dcoef;
		// String obias;
		// common/matproj/dmat(0:50,0:50),dvec(0:50),dcoef(0:50),obias(0:50);

		// double dgnu,dgper,dgpower,dfre;
		// common/fourarr/dgnu(20),dgper(20),dgpower(20),dfre(20);

		// common scalars
		// double damp,damp2,dangcut,dave,dfpow,dfouramp2,dpower;
		// double dt0,dtave,dtscale,dtsig,dtvar,dtzero,dsig,dvar;
		// double dweight,dxout;
		// int nbias,nfre,npoly;
		// common/scalar1/damp,damp2,dangcut,dave,dfpow,dfouramp2,dpower,;
		// 1 dt0,dtave,dtscale,dtsig,dtvar,dtzero,dsig,dvar,;
		// 2 dweight,dxout,nbias,nfre,npoly;

		// common old scalars
		// double tcur,tlolim,tmark,toff,toffl,tput,tresolv,tsize;
		// double tuplim,tuplimit,tlozoom,tupzoom,xleft,xright,ybottom,ytop;
		// double dlamp,dllamp,dlnu,dlper,dlpower;
		// int ma,mb,magmark,magspan,mflag,mcur,mhigh,mlow,mput;
		// int mazoom,mbzoom,nactual,nbins,nbottom,nbrake,ndigt;
		// int ndim,ndim2,negf,nleft,nlolim,nocol,nxcol,nparseok;
		// int nright,ntcol,nthis,ntop;
		// int numact,numraw,numred,nuplim,nzoom;
		// common/scalar2/tcur,tlolim,tmark,toff,toffl,tput,tresolv,tsize,;
		// 1 tuplim,tuplimit,tlozoom,tupzoom,xleft,xright,;
		// 2 ybottom,ytop,dlamp,dllamp,dlnu,dlper,dlpower,;
		// 3 ma,mb,magmark,magspan,mflag,mcur,mhigh,mlow,mput,;
		// 4 mazoom,mbzoom,nactual,nbins,nbottom,nbrake,ndigt,;
		// 5 ndim,ndim2,negf,nleft,nlolim,nocol,nxcol,nparseok,;
		// 6 nright,ntcol,nthis,ntop,numact,numraw,numred,;
		// 7 nuplim,nzoom;

		// local variables
		double dmag;
		double dt, dphase;
		double twopi;
		int np, n2, nf;

		twopi = Math.PI * 2;

		dt = (dtime - dtzero) / dtscale;
		dmag = dcoef[0];
		for (np = 1; np <= npoly; np++) {
			dmag = dmag + (dcoef[np] * (Math.pow(dt, np)));
		}
		n2 = npoly;
		for (nf = 1; nf <= nfre; nf++) {
			n2 = n2 + 2;
			dphase = twopi * dfre[nf] * dtscale * dt;
			dmag = dmag + (dcoef[n2 - 1] * Math.cos(dphase));
			dmag = dmag + (dcoef[n2] * Math.sin(dphase));
		}
		return dmag;
	}

	void polyfit() {
		// implicit none

		// common arrays

		// String fin,flog,fprint
		// int iname;
		// common/name/fin,flog;

		// double tvec,xvec,wvec;
		// common/datapts/tvec(1000000),xvec(1000000),wvec(1000000);

		// int nfit;
		// double tfit,xfit,sfit;
		// String obs;
		// common/fitpts/tfit(1000000),xfit(1000000),sfit(1000000),;
		// 1 nfit(1000000),obs(1000000);

		// double dmat,dvec,dcoef;
		// String obias;
		// common/matproj/dmat(0:50,0:50),dvec(0:50),dcoef(0:50),obias(0:50);

		// double dgnu,dgper,dgpower,dfre;
		// common/fourarr/dgnu(20),dgper(20),dgpower(20),dfre(20);

		// common scalars
		// double damp,damp2,dangcut,dave,dfpow,dfouramp2,dpower;
		// double dt0,dtave,dtscale,dtsig,dtvar,dtzero,dsig,dvar;
		// double dweight,dxout;
		// int nbias,nfre,npoly;
		// common/scalar1/damp,damp2,dangcut,dave,dfpow,dfouramp2,dpower,;
		// 1 dt0,dtave,dtscale,dtsig,dtvar,dtzero,dsig,dvar,;
		// 2 dweight,dxout,nbias,nfre,npoly;

		// common old scalars
		// double tcur,tlolim,tmark,toff,toffl,tput,tresolv,tsize;
		// double tuplim,tuplimit,tlozoom,tupzoom,xleft,xright,ybottom,ytop;
		// double dlamp,dllamp,dlnu,dlper,dlpower;
		// int ma,mb,magmark,magspan,mflag,mcur,mhigh,mlow,mput;
		// int mazoom,mbzoom,nactual,nbins,nbottom,nbrake,ndigt;
		// int ndim,ndim2,negf,nleft,nlolim,nocol,nxcol,nparseok;
		// int nright,ntcol,nthis,ntop;
		// int numact,numraw,numred,nuplim,nzoom;
		// common/scalar2/tcur,tlolim,tmark,toff,toffl,tput,tresolv,tsize,;
		// 1 tuplim,tuplimit,tlozoom,tupzoom,xleft,xright,;
		// 2 ybottom,ytop,dlamp,dllamp,dlnu,dlper,dlpower,;
		// 3 ma,mb,magmark,magspan,mflag,mcur,mhigh,mlow,mput,;
		// 4 mazoom,mbzoom,nactual,nbins,nbottom,nbrake,ndigt,;
		// 5 ndim,ndim2,negf,nleft,nlolim,nocol,nxcol,nparseok,;
		// 6 nright,ntcol,nthis,ntop,numact,numraw,numred,;
		// 7 nuplim,nzoom;

		double[] dzeta = new double[101];
		double d1, d2, tspan, tt, dtime, dt;
		int n1, n2, nt;
		int idtime, ntt;
		double x, xx, dx;

		nfre = 0;
		project();

		for (n1 = 0; n1 <= npoly; n1++) {
			d1 = dmat[0][n1];
			d2 = dmat[npoly - n1][npoly];
			for (n2 = 1; n2 <= n1; n2++) {
				d1 = d1 + dmat[n2][n1 - n2];
				d2 = d2 + dmat[npoly - n1 + n2][npoly - n2];
			}
			dzeta[n1] = d1;
			dzeta[2 * npoly - n1] = d2;
		}

		tspan = tvec[nuplim] - tvec[nlolim];
		nt = (int) (Math.log10(tspan)) - 2;
		tt = Math.pow(10.0, nt);
		numred = 0;
		x = tvec[nlolim] / tt;
		x = tt * ((int) (x) + 1);
		xx = tvec[nuplim];

		ntt = (int) ((xx - x) / tt) + 1;

		for (idtime = 1; idtime <= ntt; idtime++) {
			dtime = x + (float) (idtime - 1) * tt;
			dx = smooth(dtime);
			numred = numred + 1;
			tfit[numred] = dtime;
			xfit[numred] = dx;
			dt = (dtime - dtzero) / dtscale;
		}

		return;
	}
}
