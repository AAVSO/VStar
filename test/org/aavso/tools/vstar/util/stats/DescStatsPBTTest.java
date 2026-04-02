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
package org.aavso.tools.vstar.util.stats;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.MagnitudeModifier;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;
import org.quicktheories.WithQuickTheories;

/**
 * Property-based tests for DescStats.
 *
 * These tests express universally quantified properties of descriptive
 * statistics that hold for all valid inputs, forming a specification
 * suitable for later promotion to formal proofs.
 */
public class DescStatsPBTTest extends TestCase implements WithQuickTheories {

	public DescStatsPBTTest(String name) {
		super(name);
	}

	/**
	 * Sample variance is always non-negative for any list of 2+ observations.
	 */
	public void testSampleVarianceNonNegativeProperty() {
		qt().forAll(
				lists().of(doubles().between(-30.0, 30.0)).ofSizeBetween(2, 50))
				.check(mags -> {
					List<ValidObservation> obs = magsToObs(mags);
					double var = DescStats.calcMagSampleVarianceInRange(obs, 0,
							obs.size() - 1);
					return var >= 0;
				});
	}

	/**
	 * Population variance is always non-negative for any list of 1+ observations.
	 */
	public void testPopulationVarianceNonNegativeProperty() {
		qt().forAll(
				lists().of(doubles().between(-30.0, 30.0)).ofSizeBetween(1, 50))
				.check(mags -> {
					List<ValidObservation> obs = magsToObs(mags);
					double var = DescStats.calcMagPopulationVarianceInRange(obs,
							0, obs.size() - 1);
					return var >= 0;
				});
	}

	/**
	 * Sample variance >= population variance (Bessel's correction increases
	 * the denominator from N to N-1, so sample variance is always at least as
	 * large).
	 */
	public void testSampleVarianceGeqPopulationVarianceProperty() {
		qt().forAll(
				lists().of(doubles().between(-30.0, 30.0)).ofSizeBetween(2, 50))
				.check(mags -> {
					List<ValidObservation> obs = magsToObs(mags);
					int last = obs.size() - 1;
					double sampleVar = DescStats
							.calcMagSampleVarianceInRange(obs, 0, last);
					double popVar = DescStats
							.calcMagPopulationVarianceInRange(obs, 0, last);
					return sampleVar >= popVar - 1e-12;
				});
	}

	/**
	 * Standard deviation equals square root of variance (sample).
	 */
	public void testStdDevEqualsSqrtVarianceProperty() {
		qt().forAll(
				lists().of(doubles().between(-30.0, 30.0)).ofSizeBetween(2, 50))
				.check(mags -> {
					List<ValidObservation> obs = magsToObs(mags);
					int last = obs.size() - 1;
					double var = DescStats.calcMagSampleVarianceInRange(obs, 0,
							last);
					double stdDev = DescStats.calcMagSampleStdDevInRange(obs,
							0, last);
					return Math.abs(stdDev - Math.sqrt(var)) < 1e-12;
				});
	}

	/**
	 * Variance is translation-invariant: adding a constant to every magnitude
	 * should not change the variance.
	 */
	public void testVarianceTranslationInvariantProperty() {
		qt().forAll(
				lists().of(doubles().between(-15.0, 15.0)).ofSizeBetween(2, 50),
				doubles().between(-100.0, 100.0))
				.check((mags, shift) -> {
					List<ValidObservation> obs = magsToObs(mags);
					List<Double> shifted = new ArrayList<Double>();
					for (double m : mags) {
						shifted.add(m + shift);
					}
					List<ValidObservation> shiftedObs = magsToObs(shifted);
					int last = obs.size() - 1;
					double var1 = DescStats.calcMagSampleVarianceInRange(obs,
							0, last);
					double var2 = DescStats.calcMagSampleVarianceInRange(
							shiftedObs, 0, last);
					return Math.abs(var1 - var2) < 1e-8;
				});
	}

	/**
	 * Mean is translation-equivariant: adding a constant to every magnitude
	 * should shift the mean by the same constant.
	 */
	public void testMeanTranslationEquivariantProperty() {
		qt().forAll(
				lists().of(doubles().between(-15.0, 15.0)).ofSizeBetween(1, 50),
				doubles().between(-100.0, 100.0))
				.check((mags, shift) -> {
					List<ValidObservation> obs = magsToObs(mags);
					List<Double> shifted = new ArrayList<Double>();
					for (double m : mags) {
						shifted.add(m + shift);
					}
					List<ValidObservation> shiftedObs = magsToObs(shifted);
					int last = obs.size() - 1;
					double mean1 = DescStats.calcMagMeanInRange(obs,
							JDTimeElementEntity.instance, 0, last)[DescStats.MEAN_MAG_INDEX];
					double mean2 = DescStats.calcMagMeanInRange(shiftedObs,
							JDTimeElementEntity.instance, 0, last)[DescStats.MEAN_MAG_INDEX];
					return Math.abs((mean2 - mean1) - shift) < 1e-8;
				});
	}

	/**
	 * Variance of a constant sequence is zero.
	 */
	public void testConstantSequenceVarianceZeroProperty() {
		qt().forAll(
				doubles().between(-30.0, 30.0),
				integers().between(2, 50))
				.check((val, size) -> {
					List<Double> mags = new ArrayList<Double>();
					for (int i = 0; i < size; i++) {
						mags.add(val);
					}
					List<ValidObservation> obs = magsToObs(mags);
					double var = DescStats.calcMagPopulationVarianceInRange(obs,
							0, obs.size() - 1);
					return Math.abs(var) < 1e-12;
				});
	}

	// Helpers

	private List<ValidObservation> magsToObs(List<Double> mags) {
		double jd = 2450000.0;
		List<ValidObservation> observations = new ArrayList<ValidObservation>();
		for (double mag : mags) {
			ValidObservation obs = new ValidObservation();
			obs.setMagnitude(new Magnitude(mag, MagnitudeModifier.NO_DELTA,
					false));
			obs.setDateInfo(new DateInfo(jd));
			observations.add(obs);
			jd += 1.0;
		}
		return observations;
	}
}
