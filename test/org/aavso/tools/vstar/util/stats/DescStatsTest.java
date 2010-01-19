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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.MagnitudeModifier;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;
import org.aavso.tools.vstar.ui.model.plot.PhaseTimeElementEntity;

/**
 * Unit tests for descriptive stats class. Sample data taken from chapter 10 of
 * the AAVSO's Hands-on Astrophysics.
 */
public class DescStatsTest extends TestCase {

	private final static double[] mags1 = { 1, 2, 3, 4, 5 };
	private final static double[] mags2 = { 2, 3, 3, 3, 4 };
	private static double[] mags3 = { 4.0, 3.9, 4.1, 4.0, 4.2, 3.9, 3.9, 4.1,
			3.8, 4.0 };

	private List<ValidObservation> observations1;
	private List<ValidObservation> observations2;
	private List<ValidObservation> observations3;

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public DescStatsTest(String name) {
		super(name);

		this.observations1 = populateObservations(mags1);
		this.observations2 = populateObservations(mags2);
		this.observations3 = populateObservations(mags3);
	}

	// Valid

	public void testMeanSample1() {
		double magMean = DescStats.calcMagMeanInRange(this.observations1, 0,
				mags1.length - 1);

		assertEquals(3.0, magMean);
	}

	public void testMeanSample2() {
		double magMean = DescStats.calcMagMeanInRange(this.observations2, 0,
				mags2.length - 1);

		assertEquals(3.0, magMean);
	}

	public void testMeanSample3() {
		double magMean = DescStats.calcMagMeanInRange(this.observations3, 0,
				mags3.length - 1);
		String magMeanStr = String.format("%1.1f", magMean);
		assertEquals("4.0", magMeanStr);
	}

	public void testStdDevSample1() {
		double magStdDev = DescStats.calcMagSampleStdDevInRange(
				this.observations1, 0, mags1.length - 1);
		String magStdDevStr = String.format("%1.1f", magStdDev);
		assertEquals("1.6", magStdDevStr);
	}

	public void testStdDevSample2() {
		double magStdDev = DescStats.calcMagSampleStdDevInRange(
				this.observations2, 0, mags2.length - 1);
		String magStdDevStr = String.format("%1.2f", magStdDev);
		assertEquals("0.71", magStdDevStr);
	}

	public void testStdDevSample3() {
		double magStdDev = DescStats.calcMagSampleStdDevInRange(
				this.observations3, 0, mags3.length - 1);
		String magStdDevStr = String.format("%1.2f", magStdDev);
		assertEquals("0.12", magStdDevStr);
	}

	public void testMeanObservationSample3() {
		ValidObservation observation = DescStats.createMeanObservationForRange(
				this.observations3, JDTimeElementEntity.instance, 0,
				mags3.length - 1);

		double magMean = observation.getMagnitude().getMagValue();
		String magMeanStr = String.format("%1.1f", magMean);
		assertEquals("4.0", magMeanStr);

		double magStdErr = observation.getMagnitude().getUncertainty();
		String magStdErrStr = String.format("%1.3f", magStdErr);
		assertEquals("0.038", magStdErrStr);
	}

	public void testObservationLeftToRightBinning1() {
		// Use a bin that is greater than the number of days in the observation
		// set to ensure we don't exclude some values at the upper end of the
		// range.
		List<ValidObservation> observations = DescStats
				.createLeftToRightBinnedObservations(this.observations1,
						JDTimeElementEntity.instance, 3);

		assertTrue(observations.size() == 1);

		double magMean = observations.get(0).getMagnitude().getMagValue();
		String magMeanStr = String.format("%1.1f", magMean);
		assertEquals("3.0", magMeanStr);

		double magStdErr = observations.get(0).getMagnitude().getUncertainty();
		String magStdErrStr = String.format("%1.3f", magStdErr);
		assertEquals("0.707", magStdErrStr);
	}

	public void testObservationLeftToRightBinning2() {
		List<ValidObservation> observations = DescStats
				.createLeftToRightBinnedObservations(this.observations2,
						JDTimeElementEntity.instance, this.observations2.size());

		assertTrue(observations.size() == 1);

		double magMean = observations.get(0).getMagnitude().getMagValue();
		String magMeanStr = String.format("%1.1f", magMean);
		assertEquals("3.0", magMeanStr);

		double magStdErr = observations.get(0).getMagnitude().getUncertainty();
		String magStdErrStr = String.format("%1.3f", magStdErr);
		assertEquals("0.316", magStdErrStr);
	}

	public void testObservationLeftToRightBinning3() {
		// If we choose a bin that is too small, we should
		// just get all the data.
		List<ValidObservation> observations = DescStats
				.createLeftToRightBinnedObservations(this.observations3,
						JDTimeElementEntity.instance, this.observations3.size());

		assertTrue(observations.size() == 1);

		double magMean = observations.get(0).getMagnitude().getMagValue();
		String magMeanStr = String.format("%1.1f", magMean);
		assertEquals("4.0", magMeanStr);

		double magStdErr = observations.get(0).getMagnitude().getUncertainty();
		String magStdErrStr = String.format("%1.3f", magStdErr);
		assertEquals("0.038", magStdErrStr);
	}

	// A bin size of 2.5 JDs for observations3 should give us
	// a list of two ValidObservations.
	public void testObservationLeftToRightBinning4() {
		double binSize = 2.5;

		List<ValidObservation> observations = DescStats
				.createLeftToRightBinnedObservations(this.observations3,
						JDTimeElementEntity.instance, binSize);

		// Two ValidObservation elements?
		assertTrue(observations.size() == 2);

		// Check the magnitude mean and standard error of the average
		// for the first element.
		double magMean1 = observations.get(0).getMagnitude().getMagValue();
		String magMean1Str = String.format("%1.2f", magMean1);
		assertEquals("4.04", magMean1Str);

		double magStdErr1 = observations.get(0).getMagnitude().getUncertainty();
		String magStdErr1Str = String.format("%1.3f", magStdErr1);
		assertEquals("0.051", magStdErr1Str);

		// Check the magnitude mean and standard error of the average
		// for the second element.
		double magMean2 = observations.get(1).getMagnitude().getMagValue();
		String magMean2Str = String.format("%1.2f", magMean2);
		assertEquals("3.94", magMean2Str);

		double magStdErr2 = observations.get(1).getMagnitude().getUncertainty();
		String magStdErr2Str = String.format("%1.3f", magStdErr2);
		assertEquals("0.051", magStdErr2Str);
	}

	// Create binned observations of phase plot data.
	public void testObservationLeftToRightBinning5() {
		List<ValidObservation> obs = new ArrayList<ValidObservation>();
		obs.addAll(this.observations3);
		double epoch = (obs.get(obs.size() - 1).getJD() + obs.get(0).getJD()) / 2;
		double period = 2;
		PhaseCalcs.setPhases(obs, epoch, period);
		Collections.sort(obs, StandardPhaseComparator.instance);
		obs.addAll(obs);

		List<ValidObservation> binnedObs = DescStats
				.createLeftToRightBinnedObservations(obs,
						PhaseTimeElementEntity.instance, obs.size());

		// TODO: complete

		// assertTrue(binnedObs.size() == 1);

		// double magMean = observations.get(0).getMagnitude().getMagValue();
		// String magMeanStr = String.format("%1.1f", magMean);
		// assertEquals("3.0", magMeanStr);
		//
		// double magStdErr =
		// observations.get(0).getMagnitude().getUncertainty();
		// String magStdErrStr = String.format("%1.3f", magStdErr);
		// assertEquals("0.316", magStdErrStr);
	}

	public void testLeftmostBinning1() {
		double[] times = { 43, 44, 45, 46, 47, 48, 49 };
		double[] mags = { 1, 2, 3, 4, 5, 6, 7 };

		List<ValidObservation> obs = populateTimeMagObs(times, mags);

		List<ValidObservation> binnedObs = new LinkedList<ValidObservation>();

		DescStats.createLeftmostBinnedObservations(obs, times.length - 1,
				JDTimeElementEntity.instance, 4, binnedObs);

		assertEquals(2, binnedObs.size());
		assertEquals(2.0, binnedObs.get(0).getMag());
		assertEquals(5.5, binnedObs.get(1).getMag());
	}

	public void testLeftmostBinning2() {
		double[] times = { 35, 36, 37, 38, 39, 40, 41, 42 };
		double[] mags = { 1, 2, 3, 4, 5, 6, 7, 8 };

		List<ValidObservation> obs = populateTimeMagObs(times, mags);

		List<ValidObservation> binnedObs = new LinkedList<ValidObservation>();

		DescStats.createLeftmostBinnedObservations(obs, times.length - 1,
				JDTimeElementEntity.instance, 4, binnedObs);

		assertEquals(2, binnedObs.size());
		assertEquals(2.5, binnedObs.get(0).getMag());
		assertEquals(6.5, binnedObs.get(1).getMag());
	}

	public void testRightmostBinning1() {
		double[] times = { 37, 38, 39, 40, 41, 42 };
		double[] mags = { 1, 2, 3, 4, 5, 6 };

		List<ValidObservation> obs = populateTimeMagObs(times, mags);

		List<ValidObservation> binnedObs = new LinkedList<ValidObservation>();

		DescStats.createRightmostBinnedObservations(obs, 0,
				JDTimeElementEntity.instance, 4, binnedObs);

		assertEquals(2, binnedObs.size());
		assertEquals(2.5, binnedObs.get(0).getMag());
		assertEquals(5.5, binnedObs.get(1).getMag());
	}

	// Should be same as for testLeftmostBinning2().
	public void testRightmostBinning2() {
		double[] times = { 35, 36, 37, 38, 39, 40, 41, 42 };
		double[] mags = { 1, 2, 3, 4, 5, 6, 7, 8 };

		List<ValidObservation> obs = populateTimeMagObs(times, mags);

		List<ValidObservation> binnedObs = new LinkedList<ValidObservation>();

		DescStats.createRightmostBinnedObservations(obs, 0,
				JDTimeElementEntity.instance, 4, binnedObs);

		assertEquals(2, binnedObs.size());
		assertEquals(2.5, binnedObs.get(0).getMag());
		assertEquals(6.5, binnedObs.get(1).getMag());
	}

	// Tests of the top-level symmetric binning function.

	public void testSymmetricBinning1() {
		double[] times = { 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49 };
		double[] mags = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 };

		List<ValidObservation> obs = populateTimeMagObs(times, mags);

		List<ValidObservation> binnedObs = DescStats
				.createSymmetricBinnedObservations(obs,
						JDTimeElementEntity.instance, 4);

		assertEquals(4, binnedObs.size());
		assertEquals(1.5, binnedObs.get(0).getMag());
		assertEquals(4.5, binnedObs.get(1).getMag());
		assertEquals(8.5, binnedObs.get(2).getMag());
		assertEquals(12.0, binnedObs.get(3).getMag());
	}

	// public void testObservationSymmetricBinning1() {
	// // Use a bin that is greater than the number of days in the observation
	// // set to ensure we don't exclude some values at the upper end of the
	// // range.
	// List<ValidObservation> observations = DescStats
	// .createSymmetricBinnedObservations(this.observations1,
	// JDTimeElementEntity.instance, 3);
	//
	// assertTrue(observations.size() == 1);
	//
	// double magMean = observations.get(0).getMagnitude().getMagValue();
	// String magMeanStr = String.format("%1.1f", magMean);
	// assertEquals("3.0", magMeanStr);
	//
	// double magStdErr = observations.get(0).getMagnitude().getUncertainty();
	// String magStdErrStr = String.format("%1.3f", magStdErr);
	// assertEquals("0.707", magStdErrStr);
	// }

	// public void testObservationSymmetricBinning2() {
	// List<ValidObservation> observations = DescStats
	// .createSymmetricBinnedObservations(this.observations2,
	// JDTimeElementEntity.instance, this.observations2.size());
	//
	// assertTrue(observations.size() == 1);
	//
	// double magMean = observations.get(0).getMagnitude().getMagValue();
	// String magMeanStr = String.format("%1.1f", magMean);
	// assertEquals("3.0", magMeanStr);
	//
	// double magStdErr = observations.get(0).getMagnitude().getUncertainty();
	// String magStdErrStr = String.format("%1.3f", magStdErr);
	// assertEquals("0.316", magStdErrStr);
	// }

	// public void testObservationSymmetricBinning3() {
	// // If we choose a bin that is too small, we should
	// // just get all the data.
	// List<ValidObservation> observations = DescStats
	// .createSymmetricBinnedObservations(this.observations3,
	// JDTimeElementEntity.instance, this.observations3.size());
	//
	// assertTrue(observations.size() == 1);
	//
	// double magMean = observations.get(0).getMagnitude().getMagValue();
	// String magMeanStr = String.format("%1.1f", magMean);
	// assertEquals("4.0", magMeanStr);
	//
	// double magStdErr = observations.get(0).getMagnitude().getUncertainty();
	// String magStdErrStr = String.format("%1.3f", magStdErr);
	// assertEquals("0.038", magStdErrStr);
	// }

	// A bin size of 2.5 JDs for observations3 should give us
	// a list of two ValidObservations.
	// public void testObservationSymmetricBinning4() {
	// double binSize = 2.5;
	//
	// List<ValidObservation> observations = DescStats
	// .createSymmetricBinnedObservations(this.observations3,
	// JDTimeElementEntity.instance, binSize);
	//
	// // Two ValidObservation elements?
	// assertTrue(observations.size() == 2);
	//
	// // Check the magnitude mean and standard error of the average
	// // for the first element.
	// double magMean1 = observations.get(0).getMagnitude().getMagValue();
	// String magMean1Str = String.format("%1.2f", magMean1);
	// assertEquals("4.04", magMean1Str);
	//
	// double magStdErr1 = observations.get(0).getMagnitude().getUncertainty();
	// String magStdErr1Str = String.format("%1.3f", magStdErr1);
	// assertEquals("0.051", magStdErr1Str);
	//
	// // Check the magnitude mean and standard error of the average
	// // for the second element.
	// double magMean2 = observations.get(1).getMagnitude().getMagValue();
	// String magMean2Str = String.format("%1.2f", magMean2);
	// assertEquals("3.94", magMean2Str);
	//
	// double magStdErr2 = observations.get(1).getMagnitude().getUncertainty();
	// String magStdErr2Str = String.format("%1.3f", magStdErr2);
	// assertEquals("0.051", magStdErr2Str);
	// }

	// Helpers

	// Populates and returns a list of valid observations with supplied
	// magnitude values and bogus JD values.
	private List<ValidObservation> populateObservations(double[] mags) {
		double jd = 0;
		List<ValidObservation> observations = new ArrayList<ValidObservation>();
		for (double mag : mags) {
			ValidObservation obs = new ValidObservation();
			obs.setMagnitude(new Magnitude(mag, MagnitudeModifier.NO_DELTA,
					false));
			obs.setDateInfo(new DateInfo(jd));
			observations.add(obs);
			jd += 0.5;
		}

		return observations;
	}

	// Populates and returns a list of valid observations with supplied
	// magnitude values and date-time (JD) values.
	private List<ValidObservation> populateTimeMagObs(double[] times,
			double[] mags) {
		assert times.length == mags.length;

		List<ValidObservation> observations = new ArrayList<ValidObservation>();

		for (int i = 0; i < times.length; i++) {
			ValidObservation obs = new ValidObservation();
			obs.setMagnitude(new Magnitude(mags[i], MagnitudeModifier.NO_DELTA,
					false));
			obs.setDateInfo(new DateInfo(times[i]));
			observations.add(obs);
		}

		return observations;
	}
}
