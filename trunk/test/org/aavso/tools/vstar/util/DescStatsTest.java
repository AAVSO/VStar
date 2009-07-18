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
package org.aavso.tools.vstar.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.MagnitudeModifier;
import org.aavso.tools.vstar.data.ValidObservation;

/**
 * Unit tests for descriptive stats class. Sample data taken from chapter 10 
 * of the AAVSO's Hands-on Astrophysics.
 */
public class DescStatsTest extends TestCase {

	private static double[] mags1 = { 1, 2, 3, 4, 5 };
	private static double[] mags2 = { 2, 3, 3, 3, 4 };
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
		double magMean = DescStats.calcMagMeanInJDRange(this.observations1, 0,
				mags1.length - 1);

		assertEquals(3.0, magMean);
	}

	public void testMeanSample2() {
		double magMean = DescStats.calcMagMeanInJDRange(this.observations2, 0,
				mags2.length - 1);

		assertEquals(3.0, magMean);
	}

	public void testMeanSample3() {
		double magMean = DescStats.calcMagMeanInJDRange(this.observations3, 0,
				mags3.length - 1);
		String magMeanStr = String.format("%1.1f", magMean);
		assertEquals("4.0", magMeanStr);
	}

	public void testStdDevSample1() {
		double magStdDev = DescStats.calcMagSampleStdDevInJDRange(
				this.observations1, 0, mags1.length - 1);
		String magStdDevStr = String.format("%1.1f", magStdDev);
		assertEquals("1.6", magStdDevStr);
	}

	public void testStdDevSample2() {
		double magStdDev = DescStats.calcMagSampleStdDevInJDRange(
				this.observations2, 0, mags2.length - 1);
		String magStdDevStr = String.format("%1.2f", magStdDev);
		assertEquals("0.71", magStdDevStr);
	}

	public void testStdDevSample3() {
		double magStdDev = DescStats.calcMagSampleStdDevInJDRange(
				this.observations3, 0, mags3.length - 1);
		String magStdDevStr = String.format("%1.2f", magStdDev);
		assertEquals("0.12", magStdDevStr);
	}

	public void testMeanObservationSample3() {
		ValidObservation observation = DescStats
				.createMeanObservationForJDRange(this.observations3, 0,
						mags3.length - 1);

		double magMean = observation.getMagnitude().getMagValue();
		String magMeanStr = String.format("%1.1f", magMean);
		assertEquals("4.0", magMeanStr);

		double magStdErr = observation.getMagnitude().getUncertainty();
		String magStdErrStr = String.format("%1.3f", magStdErr);
		assertEquals("0.038", magStdErrStr);
	}

	public void testObservationBinning1() {
		List<ValidObservation> observations = DescStats
				.createdBinnedObservations(this.observations1,
						this.observations1.size());

		assertTrue(observations.size() == 1);

		double magMean = observations.get(0).getMagnitude().getMagValue();
		String magMeanStr = String.format("%1.1f", magMean);
		assertEquals("3.0", magMeanStr);

		double magStdErr = observations.get(0).getMagnitude().getUncertainty();
		String magStdErrStr = String.format("%1.3f", magStdErr);
		assertEquals("0.707", magStdErrStr);
	}

	public void testObservationBinning2() {
		List<ValidObservation> observations = DescStats
				.createdBinnedObservations(this.observations2,
						this.observations2.size());

		assertTrue(observations.size() == 1);

		double magMean = observations.get(0).getMagnitude().getMagValue();
		String magMeanStr = String.format("%1.1f", magMean);
		assertEquals("3.0", magMeanStr);

		double magStdErr = observations.get(0).getMagnitude().getUncertainty();
		String magStdErrStr = String.format("%1.3f", magStdErr);
		assertEquals("0.316", magStdErrStr);
	}

	public void testObservationBinning3() {
		List<ValidObservation> observations = DescStats
				.createdBinnedObservations(this.observations3,
						this.observations3.size());

		assertTrue(observations.size() == 1);

		double magMean = observations.get(0).getMagnitude().getMagValue();
		String magMeanStr = String.format("%1.1f", magMean);
		assertEquals("4.0", magMeanStr);

		double magStdErr = observations.get(0).getMagnitude().getUncertainty();
		String magStdErrStr = String.format("%1.3f", magStdErr);
		assertEquals("0.038", magStdErrStr);
	}

	// Helpers

	// Populates and returns a list of valid observations with supplied
	// magnitude values and bogus JD values.
	private List<ValidObservation> populateObservations(double[] mags) {
		int jdIncrement = 0;
		List<ValidObservation> observations = new ArrayList<ValidObservation>();
		for (double mag : mags) {
			ValidObservation obs = new ValidObservation();
			obs.setMagnitude(new Magnitude(mag, MagnitudeModifier.NO_DELTA,
					false));
			obs.setDateInfo(new DateInfo(2450000));
			observations.add(obs);
			jdIncrement += 0.5;
		}

		return observations;
	}
}
