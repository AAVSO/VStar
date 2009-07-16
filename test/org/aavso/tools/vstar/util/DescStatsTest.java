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
import org.aavso.tools.vstar.data.validation.InclusiveRangePredicate;

/**
 * Unit tests for descriptive stats class.
 */
public class DescStatsTest extends TestCase {

	private static double[] mags1 = {1,2,3,4,5};
	private static double[] mags2 = {2,3,3,3,4};
	
	private List<ValidObservation> observations1;
	private List<ValidObservation> observations2;
	
	private InclusiveRangePredicate jdRange1;
	private InclusiveRangePredicate jdRange2;
	
	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public DescStatsTest(String name) {
		super(name);
	
		int jdIncrement1 = 0;		
		this.observations1 = new ArrayList<ValidObservation>();
		for (double mag : mags1) {
			ValidObservation obs = new ValidObservation();
			obs.setMagnitude(new Magnitude(mag, MagnitudeModifier.NO_DELTA, false));
			obs.setDateInfo(new DateInfo(2450000));
			this.observations1.add(obs);
			jdIncrement1 += 0.5;
		}
		
		this.jdRange1 = new InclusiveRangePredicate(2450000, 2450002);
		
		int jdIncrement2 = 0;		
		this.observations2 = new ArrayList<ValidObservation>();
		for (double mag : mags2) {
			ValidObservation obs = new ValidObservation();
			obs.setMagnitude(new Magnitude(mag, MagnitudeModifier.NO_DELTA, false));
			obs.setDateInfo(new DateInfo(2450000));
			this.observations2.add(obs);
			jdIncrement2 += 0.5;
		}

		this.jdRange2 = new InclusiveRangePredicate(2450000, 2450002);
	}

	// Valid
	
	public void testMeanSample1() {
		double magMean = DescStats.calcMagMeanInJDRange(this.observations1, this.jdRange1);
		
		assertEquals(3.0, magMean);
	}

	public void testMeanSample2() {
		double magMean = DescStats.calcMagMeanInJDRange(this.observations2, this.jdRange2);
		
		assertEquals(3.0, magMean);
	}

	public void testStdDevSample1() {
		double magStdDev = DescStats.calcMagSampleStdDevInJDRange(this.observations1, this.jdRange1);
		String magStdDevStr = String.format("%1.1f", magStdDev);
		assertEquals("1.6", magStdDevStr);
	}

	public void testStdDevSample2() {
		double magStdDev = DescStats.calcMagSampleStdDevInJDRange(this.observations2, this.jdRange2);		
		String magStdDevStr = String.format("%1.2f", magStdDev);
		assertEquals("0.71", magStdDevStr);
	}
	
//	public void testStdErrorOfAverageSample3() {
//		double magStdDev = DescStats.calcMagSampleStdDevInJDRange(this.observations3, this.jdRange3);
//		String magStdDevStr = String.format("%1.1f", magStdDev);
//		assertEquals("1.6", magStdDevStr);
//	}
	
}
