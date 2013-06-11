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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.util.TSBase;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.apache.commons.math.analysis.UnivariateRealFunction;

/**
 * <p>
 * This is a Java translation of the Fortran polymast subroutine from the
 * AAVSO's ts1201.f by Matthew Templeton, which in turn was based upon BASIC
 * code by Grant Foster.
 * </p>
 * 
 * <p>
 * As Matt has said, this is a standard polynomial fit of the form:
 * </p>
 * 
 * <p>
 * f(x) = sum(ax^n)
 * </p>
 */
public class TSPolynomialFitter extends TSBase implements IPolynomialFitter {

	private int degree;
	private int numred;

	private double[] tfit;
	private double[] xfit;

	private List<ValidObservation> fit;
	private List<ValidObservation> residuals;

	private Map<String, String> functionStrMap;

	/**
	 * Constructor
	 * 
	 * @param observations
	 *            The list of observations (a single band makes most sense) to
	 *            which the polynomial fit is to be applied.
	 */
	public TSPolynomialFitter(List<ValidObservation> observations) {
		super(observations);
		functionStrMap = new LinkedHashMap<String, String>();
		degree = 0;
	}

	@Override
	public String getDescription() {
		return LocaleProps.get("MODEL_INFO_POLYNOMIAL_DEGREE_DESC") + degree;
	}

	@Override
	public String getKind() {
		return LocaleProps.get("ANALYSIS_MENU_POLYNOMIAL_FIT");
	}

	// TODO: move to plugin for persistence!

	/**
	 * @see org.aavso.tools.vstar.util.model.IPolynomialFitter#setDegree(int)
	 */
	@Override
	public void setDegree(int degree) {
		this.degree = degree;
	}

	@Override
	public int getMinDegree() {
		return 0;
	}

	@Override
	public int getMaxDegree() {
		// TODO: make this a preference
		return 30;
	}

	/**
	 * @see org.aavso.tools.vstar.util.IAlgorithm#execute()
	 */
	@Override
	public void execute() throws AlgorithmError {
		// Load the observation data and perform a polynomial fitting operation
		// of the specified degree.
		load_raw();
		interrupted = false;
		try {
			polymast(degree);

			functionStrMap.put(LocaleProps.get("MODEL_INFO_FUNCTION_TITLE"),
					toString());

			functionStrMap.put(LocaleProps.get("MODEL_INFO_EXCEL_TITLE"),
					toExcelString());

			functionStrMap.put(LocaleProps.get("MODEL_INFO_R_TITLE"),
					toRString());
		} catch (InterruptedException e) {
			// Do nothing; just return.
		}
	}

	public void interrupt() {
		interrupted = true;
	}

	/**
	 * @see org.aavso.tools.vstar.util.model.IPolynomialFitter#getFit()
	 */
	@Override
	public List<ValidObservation> getFit() {
		return fit;
	}

	/**
	 * @see org.aavso.tools.vstar.util.model.IPolynomialFitter#getResiduals()
	 */
	@Override
	public List<ValidObservation> getResiduals() {
		return residuals;
	}

	void polymast(int polyDeg) throws AlgorithmError, InterruptedException {

		double ds9, dcc, res, dtime, dx;
		int n, nb;

		statcomp();

		assert polyDeg >= 0 && polyDeg <= 50;

		npoly = polyDeg;

		polyfit();

		ds9 = dvar * ((double) (numact - 1) - (double) (npoly) * dpower);
		ds9 = ds9 / (double) (numact - 1) / (double) (numact - 1 - npoly);
		if (ds9 < 0.0)
			ds9 = 0.0;
		ds9 = Math.sqrt(ds9);

		// write(1,230) npoly,dpower,ds9,fprint,numact,dave,dsig,dvar
		// write(1,292) dt0+tvec(nlolim),dt0+tvec(nuplim),dt0+dtzero

		// Note: save constants

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

		// Note: save to file

		// 20 write(1,220) npoly
		// write(1,290) fprint,numact,dave,dsig,dvar
		// write(1,292) dt0+tvec(nlolim),dt0+tvec(nuplim),dt0+dtzero
		// write(1,221) dpower

		// Store the results of the polynomial fit operation as
		// "fit observations".
		fit = new ArrayList<ValidObservation>();

		// TODO: fix wrt locale
		String comment = "From polynomial fit of degree " + degree;

		for (n = 1; n <= numred; n++) {
			// write(1,222)tfit(n)+dt0,xfit(n),ds9*sfit(n)
			ValidObservation fitOb = new ValidObservation();
			fitOb.setDateInfo(new DateInfo(tfit[n] + dt0));
			// double uncertainty = ds9*sfit[n]; // TODO: ask Matt about this;
			// uncertainty?
			fitOb.setMagnitude(new Magnitude(xfit[n], 0));
			fitOb.setComments(comment);
			fitOb.setBand(SeriesType.Model);
			fit.add(fitOb);
		}

		if (fit.isEmpty()) {
			throw new AlgorithmError("No observations in fit list.");
		}

		// Note: save residuals (to list)

		// 40 write(6,*) 'Residuals filename?'
		// read*,ftmp;
		// 41 open(unit=9,file=ftmp,status='unknown',err=42);
		// 42 write(6,*) 'Could not open file.'

		// Store the residuals resulting from the polynomial fit operation as
		// "residual observations".
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
				residualOb.setComments(comment);
				residualOb.setBand(SeriesType.Residuals);
				residuals.add(residualOb);
			}
		}

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

	void polyfit() throws InterruptedException {
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

		tfit = new double[ntt + 1];
		xfit = new double[ntt + 1];

		for (idtime = 1; idtime <= ntt; idtime++) {
			dtime = x + (double) (idtime - 1) * tt;
			dx = smooth(dtime);
			numred = numred + 1;
			tfit[numred] = dtime;
			xfit[numred] = dx;
			dt = (dtime - dtzero) / dtscale;
		}
	}

	@Override
	public List<PeriodFitParameters> getParameters() {
		return null;
	}

	@Override
	public boolean hasFuncDesc() {
		return true;
	}

	public String toString() {
		String strRepr = functionStrMap.get(LocaleProps
				.get("MODEL_INFO_FUNCTION_TITLE"));

		if (strRepr == null) {
			strRepr = "f(t) = ";

			String fmt = NumericPrecisionPrefs.getOtherOutputFormat();

			// sum(a[i]t^n), where n >= 1
			for (int i = npoly; i >= 1; i--) {
				strRepr += String.format(fmt, dcoef[i]) + "(t-"
						+ getZeroPointOffset() + ")^" + i + " + \n";
			}

			// The zeroth (constant) coefficient, where n = 0 since t^0 = 1.
			strRepr += String.format(fmt, dcoef[0]);
		}

		return strRepr;
	}

	private String toExcelString() {
		String strRepr = functionStrMap.get(LocaleProps
				.get("MODEL_INFO_EXCEL_TITLE"));

		if (strRepr == null) {
			strRepr = "=SUM(";

			String fmt = NumericPrecisionPrefs.getOtherOutputFormat();

			// sum(a[i]t^n), where n >= 1
			for (int i = npoly; i >= 1; i--) {
				strRepr += String.format(fmt, dcoef[i]) + "*(A1-"
						+ getZeroPointOffset() + ")^" + i + ",\n";
			}

			// The zeroth (constant) coefficient, where n = 0 since t^0 = 1.
			strRepr += String.format(fmt, dcoef[0]);

			strRepr += ")";
		}

		return strRepr;
	}

	public String toRString() {
		String strRepr = functionStrMap.get(LocaleProps
				.get("MODEL_INFO_R_TITLE"));

		if (strRepr == null) {
			strRepr = "model <- function(t) ";

			String fmt = NumericPrecisionPrefs.getOtherOutputFormat();

			// sum(a[i]t^n), where n >= 1
			for (int i = npoly; i >= 1; i--) {
				strRepr += String.format("%1.20f", dcoef[i]) + "*(t-"
						+ getZeroPointOffset() + ")^" + i + " + \n";
			}

			// The zeroth (constant) coefficient, where n = 0 since t^0 = 1.
			strRepr += String.format(fmt, dcoef[0]);
		}

		return strRepr;
	}

	@Override
	public Map<String, String> getFunctionStrings() {
		return functionStrMap;
	}
	
	@Override
	public UnivariateRealFunction getModelFunction() {
		return null;
	}
}
