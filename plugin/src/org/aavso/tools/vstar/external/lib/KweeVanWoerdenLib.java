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
package org.aavso.tools.vstar.external.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aavso.tools.vstar.exception.AlgorithmError;

/**
 * Kwee–van Woerden eclipse / transit minimum-time determination with Deeg
 * (2020) revised timing-error estimates.
 *
 * <p>
 * This is a Java implementation based on Deeg, H.J. 2020/2021, Galaxies 9, 1
 * (arXiv:2011.09231), not a transpile of the reference Python/IDL code. The
 * <a href="https://github.com/hdeeg/KvW">hdeeg/KvW</a> code was used for
 * golden-value verification and to clarify procedural details not fully
 * specified in the paper (half-integer fold pairing and S(T) branch cropping).
 * </p>
 *
 * <p>
 * Input light curves should contain only the eclipse (no off-eclipse points)
 * and be near-equidistant in time. {@code values} must be lower at the event of
 * interest (e.g. normalised flux for a minimum, or negated magnitude).
 * </p>
 */
public final class KweeVanWoerdenLib {

	private KweeVanWoerdenLib() {
	}

	public enum T1Mode {
		/** Use the central point of the light curve as the initial estimate. */
		MIDPOINT,
		/** Use the point of extreme (lowest) value as the initial estimate. */
		EXTREMUM
	}

	public enum EventType {
		/**
		 * Eclipse / transit minimum: values are expected to be lower at the event
		 * (flux), or will be inverted when working from magnitudes via the tool.
		 */
		MINIMUM,
		/** Maximum: values are inverted before folding so the peak is treated as a minimum. */
		MAXIMUM
	}

	/**
	 * Parameters for a KvW analysis.
	 */
	public static class Params {
		public int nfold = 5;
		public T1Mode t1Mode = T1Mode.MIDPOINT;
		/** Mean photometric noise μ; null ⇒ estimate from min S. */
		public Double mu = null;
		/** Relative spacing tolerance (Deeg default 1%). */
		public double equidistanceTolerance = 0.01;
		/** If true, linearly resample to median Δt when spacing is uneven. */
		public boolean resampleIfNeeded = true;
		public EventType eventType = EventType.MINIMUM;

		public Params() {
		}

		public Params(int nfold, T1Mode t1Mode, Double mu) {
			this.nfold = nfold;
			this.t1Mode = t1Mode;
			this.mu = mu;
		}
	}

	/**
	 * One S(T) fold sample used in the parabolic fit.
	 */
	public static class FoldSample {
		public final double time;
		public final double s;

		public FoldSample(double time, double s) {
			this.time = time;
			this.s = s;
		}
	}

	/**
	 * Result of a KvW analysis.
	 */
	public static class Result {
		public final double t0;
		/** Deeg (2020) timing uncertainty (primary). */
		public final double sigmaDeeg;
		/** Classic KvW (1956) timing uncertainty; may be NaN. */
		public final double sigmaClassic;
		public final double dt;
		public final int n;
		public final int z;
		public final int nfoldUsed;
		public final double a;
		public final double b;
		public final double c;
		public final double muUsed;
		public final boolean equidistanceWarning;
		public final boolean wasResampled;
		public final List<FoldSample> foldSamples;

		public Result(double t0, double sigmaDeeg, double sigmaClassic, double dt, int n, int z, int nfoldUsed,
				double a, double b, double c, double muUsed, boolean equidistanceWarning, boolean wasResampled,
				List<FoldSample> foldSamples) {
			this.t0 = t0;
			this.sigmaDeeg = sigmaDeeg;
			this.sigmaClassic = sigmaClassic;
			this.dt = dt;
			this.n = n;
			this.z = z;
			this.nfoldUsed = nfoldUsed;
			this.a = a;
			this.b = b;
			this.c = c;
			this.muUsed = muUsed;
			this.equidistanceWarning = equidistanceWarning;
			this.wasResampled = wasResampled;
			this.foldSamples = foldSamples;
		}
	}

	/**
	 * Analyse an eclipse light curve: validate input, optionally invert maxima,
	 * optionally resample to uniform spacing, then run the equidistant KvW core.
	 *
	 * @param times  ascending times (e.g. JD / BJD)
	 * @param values brightness measure that is lower at a MINIMUM event
	 * @param params algorithm parameters
	 * @return timing result
	 * @throws AlgorithmError if the data are unsuitable
	 */
	public static Result analyze(double[] times, double[] values, Params params) throws AlgorithmError {
		if (times == null || values == null) {
			throw new AlgorithmError("Time and value arrays must not be null.");
		}
		if (times.length != values.length) {
			throw new AlgorithmError("Time and value arrays must have the same length.");
		}
		if (times.length < 7) {
			throw new AlgorithmError("Need at least 7 points for Kwee–van Woerden analysis.");
		}
		if (params.nfold != 3 && params.nfold != 5 && params.nfold != 7) {
			throw new AlgorithmError("nfold must be 3, 5, or 7.");
		}

		double[] tIn = Arrays.copyOf(times, times.length);
		double[] vIn = Arrays.copyOf(values, values.length);
		sortByTime(tIn, vIn);

		// KvW always minimises the value array; flip maxima into minima.
		if (params.eventType == EventType.MAXIMUM) {
			for (int i = 0; i < vIn.length; i++) {
				vIn[i] = -vIn[i];
			}
		}

		boolean uneven = !isEquidistant(tIn, params.equidistanceTolerance);
		boolean wasResampled = false;
		if (uneven) {
			if (params.resampleIfNeeded) {
				double[][] resampled = resampleLinear(tIn, vIn);
				tIn = resampled[0];
				vIn = resampled[1];
				wasResampled = true;
				uneven = !isEquidistant(tIn, params.equidistanceTolerance);
			}
		}

		return analyzeEquidistant(tIn, vIn, params, uneven, wasResampled);
	}

	/**
	 * Core KvW on a near-equidistant eclipse segment.
	 * <ol>
	 * <li>Choose provisional mid-index T₁ (midpoint or flux extremum).</li>
	 * <li>At nfold reflection axes spaced by Δt/2, form S = Σ(left−right)²
	 * using the largest balanced pairing count Z.</li>
	 * <li>Map fold indices to time; optionally crop an asymmetric S(T) branch.</li>
	 * <li>Fit S = aT² + bT + c; T₀ = −b/(2a).</li>
	 * <li>Classic σ from the original discriminant (may be NaN); Deeg σ from
	 * σ² = 2μ²/a after anchoring c to the noise floor.</li>
	 * </ol>
	 */
	private static Result analyzeEquidistant(double[] time, double[] flux, Params params, boolean equidistanceWarning,
			boolean wasResampled) throws AlgorithmError {
		int npts = time.length;
		int minid = npts / 2;

		// Subtract a coarse origin so parabola coefficients stay well-conditioned.
		double time0 = Math.floor(time[minid] * 10.0) / 10.0;
		double[] t = new double[npts];
		for (int i = 0; i < npts; i++) {
			t[i] = time[i] - time0;
		}

		if (params.t1Mode == T1Mode.EXTREMUM) {
			minid = indexOfMinimum(flux);
		}

		int nfold = params.nfold;
		// Half-steps of 0.5 index: folds at minid + {-noffr, ..., +noffr}.
		double noffr = (nfold - 1) / 4.0;
		int noff = (int) noffr;
		int minfoldid = minid - noff;
		int maxfoldid = minid + noff;
		// Z = how many point-pairs fit on both sides of every fold axis.
		int nleft = minfoldid;
		int nright = npts - maxfoldid - 1;
		int z = Math.min(nleft, nright);

		if (z < 3) {
			throw new AlgorithmError(
					"Fewer than 3 points can be paired on each side of the eclipse. Decrease nfold or provide more in-/egress points.");
		}

		double[] s = new double[nfold];
		double[] foldidf = new double[nfold];
		for (int segid = 0; segid < nfold; segid++) {
			foldidf[segid] = minid - noffr + segid / 2.0;
			int foldidi = (int) (foldidf[segid] + 0.0001);
			if (Math.abs(foldidf[segid] - foldidi) <= 0.01) {
				// Fold on a data point: pair ±i about that index.
				for (int i = 1; i <= z; i++) {
					int idlo = foldidi - i;
					int idhi = foldidi + i;
					double d = flux[idlo] - flux[idhi];
					s[segid] += d * d;
				}
			} else {
				// Fold halfway between points: pair (foldidi−i+1) with (foldidi+i).
				for (int i = 1; i <= z; i++) {
					int idlo = foldidi - i + 1;
					int idhi = foldidi + i;
					double d = flux[idlo] - flux[idhi];
					s[segid] += d * d;
				}
			}
		}

		// Linear map from fold index → time (handles half-integer fold positions).
		int minidf = (int) Math.floor(foldidf[0]);
		int maxidf = (int) Math.floor(foldidf[nfold - 1] + 0.50001);
		int nLin = maxidf - minidf + 1;
		double[] xs = new double[nLin];
		double[] ys = new double[nLin];
		for (int i = 0; i < nLin; i++) {
			xs[i] = minidf + i;
			ys[i] = t[minidf + i];
		}
		double[] lin = fitLinear(xs, ys);
		double[] timef = new double[nfold];
		for (int i = 0; i < nfold; i++) {
			timef[i] = lin[0] + lin[1] * foldidf[i];
		}

		// If min S is off-centre, crop the longer S(T) wing so weights stay balanced.
		if (nfold >= 5) {
			int minSid = indexOfMinimum(s);
			int nSleft = minSid;
			int nSright = nfold - minSid - 1;
			if (nSleft - nSright >= 2) {
				int drop = nSleft - nSright - 1;
				timef = Arrays.copyOfRange(timef, drop, timef.length);
				s = Arrays.copyOfRange(s, drop, s.length);
			} else if (nSright - nSleft >= 2) {
				int drop = nSright - nSleft - 1;
				timef = Arrays.copyOfRange(timef, 0, timef.length - drop);
				s = Arrays.copyOfRange(s, 0, s.length - drop);
			}
		}

		if (timef.length < 3) {
			throw new AlgorithmError("Not enough S(T) folds remain after symmetrisation.");
		}

		double[] abc = fitQuadratic(timef, s);
		double a = abc[0];
		double b = abc[1];
		double c = abc[2];

		if (!(a > 0) || Double.isNaN(a) || Double.isInfinite(a)) {
			throw new AlgorithmError("Parabola fit to S(T) did not yield a usable minimum (a <= 0).");
		}

		double t0 = -b / (2.0 * a) + time0;

		// Classic KvW (1956): requires 4ac−b² > 0; else σ is undefined (common on low noise).
		double disc = 4.0 * a * c - b * b;
		double sigmaClassic = Double.NaN;
		if (disc > 0) {
			sigmaClassic = Math.sqrt(disc / (4.0 * a * a * (z - 1)));
		}

		double muUsed;
		if (params.mu != null) {
			muUsed = params.mu;
		} else {
			// Estimate μ from the best fold, assumed noise-dominated (Deeg).
			double minS = min(s);
			muUsed = Math.sqrt(minS / (2.0 * (z - 1)));
		}

		// Deeg: reset c so S_fit(T₀) matches the noise floor, then σ² = 2μ²/a.
		double cRevised = (z - 1) * 2.0 * muUsed * muUsed + (b * b) / (4.0 * a);
		double sigmaDeeg = Math.sqrt((4.0 * a * cRevised - b * b) / (4.0 * a * a * (z - 1)));

		double dt = medianDiff(t);

		List<FoldSample> samples = new ArrayList<FoldSample>();
		for (int i = 0; i < timef.length; i++) {
			samples.add(new FoldSample(timef[i] + time0, s[i]));
		}

		return new Result(t0, sigmaDeeg, sigmaClassic, dt, npts, z, timef.length, a, b, c, muUsed, equidistanceWarning,
				wasResampled, samples);
	}

	/**
	 * True if successive spacings stay within {@code tolerance} of the median
	 * spacing (Deeg’s 1% default).
	 */
	static boolean isEquidistant(double[] time, double tolerance) {
		if (time.length < 2) {
			return true;
		}
		double[] diffs = new double[time.length - 1];
		for (int i = 0; i < diffs.length; i++) {
			diffs[i] = time[i + 1] - time[i];
			if (diffs[i] <= 0) {
				return false;
			}
		}
		double med = median(diffs);
		if (med <= 0) {
			return false;
		}
		double max = diffs[0];
		double min = diffs[0];
		for (double d : diffs) {
			if (d > max) {
				max = d;
			}
			if (d < min) {
				min = d;
			}
		}
		double tolFactor = 1.0 + tolerance;
		return max / med < tolFactor && med / min < tolFactor;
	}

	/**
	 * Linearly interpolate onto a uniform grid with spacing = median Δt.
	 *
	 * @return {times, values} of the resampled series
	 */
	static double[][] resampleLinear(double[] time, double[] values) throws AlgorithmError {
		double dt = medianDiff(time);
		if (dt <= 0) {
			throw new AlgorithmError("Cannot resample: non-positive median spacing.");
		}
		double tStart = time[0];
		double tEnd = time[time.length - 1];
		int n = (int) Math.floor((tEnd - tStart) / dt) + 1;
		if (n < 7) {
			throw new AlgorithmError("Resampling produced too few points.");
		}
		double[] tOut = new double[n];
		double[] vOut = new double[n];
		for (int i = 0; i < n; i++) {
			double ti = tStart + i * dt;
			tOut[i] = ti;
			vOut[i] = interpolateLinear(time, values, ti);
		}
		return new double[][] { tOut, vOut };
	}

	/** Piecewise-linear interpolation; clamps outside the input range. */
	private static double interpolateLinear(double[] time, double[] values, double t) {
		if (t <= time[0]) {
			return values[0];
		}
		if (t >= time[time.length - 1]) {
			return values[values.length - 1];
		}
		int i = 0;
		while (i < time.length - 1 && time[i + 1] < t) {
			i++;
		}
		double t0 = time[i];
		double t1 = time[i + 1];
		double f = (t - t0) / (t1 - t0);
		return values[i] + f * (values[i + 1] - values[i]);
	}

	private static void sortByTime(double[] time, double[] values) {
		Integer[] idx = new Integer[time.length];
		for (int i = 0; i < idx.length; i++) {
			idx[i] = i;
		}
		Arrays.sort(idx, (i, j) -> Double.compare(time[i], time[j]));
		double[] t2 = new double[time.length];
		double[] v2 = new double[values.length];
		for (int i = 0; i < idx.length; i++) {
			t2[i] = time[idx[i]];
			v2[i] = values[idx[i]];
		}
		System.arraycopy(t2, 0, time, 0, time.length);
		System.arraycopy(v2, 0, values, 0, values.length);
	}

	private static int indexOfMinimum(double[] x) {
		int minid = 0;
		for (int i = 1; i < x.length; i++) {
			if (x[i] < x[minid]) {
				minid = i;
			}
		}
		return minid;
	}

	private static double min(double[] x) {
		double m = x[0];
		for (int i = 1; i < x.length; i++) {
			if (x[i] < m) {
				m = x[i];
			}
		}
		return m;
	}

	private static double medianDiff(double[] time) {
		double[] diffs = new double[time.length - 1];
		for (int i = 0; i < diffs.length; i++) {
			diffs[i] = time[i + 1] - time[i];
		}
		return median(diffs);
	}

	private static double median(double[] x) {
		double[] y = Arrays.copyOf(x, x.length);
		Arrays.sort(y);
		int n = y.length;
		if (n % 2 == 1) {
			return y[n / 2];
		}
		return 0.5 * (y[n / 2 - 1] + y[n / 2]);
	}

	/**
	 * Ordinary least-squares fit of y = a + b x.
	 *
	 * @return {a, b}
	 */
	static double[] fitLinear(double[] x, double[] y) {
		int n = x.length;
		double sx = 0, sy = 0, sxx = 0, sxy = 0;
		for (int i = 0; i < n; i++) {
			sx += x[i];
			sy += y[i];
			sxx += x[i] * x[i];
			sxy += x[i] * y[i];
		}
		double den = n * sxx - sx * sx;
		double b = (n * sxy - sx * sy) / den;
		double a = (sy - b * sx) / n;
		return new double[] { a, b };
	}

	/**
	 * Ordinary least-squares fit of y = a x² + b x + c via 3×3 normal equations
	 * (Gaussian elimination with partial pivoting).
	 *
	 * @return {a, b, c}
	 */
	static double[] fitQuadratic(double[] x, double[] y) throws AlgorithmError {
		int n = x.length;
		double sx = 0, sx2 = 0, sx3 = 0, sx4 = 0;
		double sy = 0, sxy = 0, sx2y = 0;
		for (int i = 0; i < n; i++) {
			double xi = x[i];
			double yi = y[i];
			double xi2 = xi * xi;
			sx += xi;
			sx2 += xi2;
			sx3 += xi2 * xi;
			sx4 += xi2 * xi2;
			sy += yi;
			sxy += xi * yi;
			sx2y += xi2 * yi;
		}

		// Augmented matrix for [a, b, c].
		double[][] m = new double[][] {
				{ sx4, sx3, sx2, sx2y },
				{ sx3, sx2, sx, sxy },
				{ sx2, sx, n, sy }
		};

		for (int i = 0; i < 3; i++) {
			int piv = i;
			for (int r = i + 1; r < 3; r++) {
				if (Math.abs(m[r][i]) > Math.abs(m[piv][i])) {
					piv = r;
				}
			}
			double[] tmp = m[i];
			m[i] = m[piv];
			m[piv] = tmp;
			double div = m[i][i];
			if (Math.abs(div) < 1e-30) {
				throw new AlgorithmError("Singular matrix fitting parabola to S(T).");
			}
			for (int j = i; j < 4; j++) {
				m[i][j] /= div;
			}
			for (int r = 0; r < 3; r++) {
				if (r == i) {
					continue;
				}
				double fac = m[r][i];
				for (int j = i; j < 4; j++) {
					m[r][j] -= fac * m[i][j];
				}
			}
		}

		return new double[] { m[0][3], m[1][3], m[2][3] };
	}
}
