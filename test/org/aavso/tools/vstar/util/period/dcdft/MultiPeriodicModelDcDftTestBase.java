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
package org.aavso.tools.vstar.util.period.dcdft;

import java.util.List;
import java.util.Map;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.util.Tolerance;
import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.model.PeriodAnalysisDerivedMultiPeriodicModel;
import org.aavso.tools.vstar.util.model.PeriodFitParameters;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisAlgorithm;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;

/**
 * Base class for multi-period model creation unit tests.
 */
public abstract class MultiPeriodicModelDcDftTestBase extends DataTestBase {

	protected static double DELTA = 1e-4;

	protected PeriodAnalysisDerivedMultiPeriodicModel model;

	public MultiPeriodicModelDcDftTestBase(String name, double[][] jdAndMagPairs) {
		super(name, jdAndMagPairs);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected void commonTest(IPeriodAnalysisAlgorithm algorithm,
			List<Harmonic> harmonics, List<PeriodFitParameters> expectedParamsList, double[][] expectedModelData,
			double[][] expectedResidualData) {

		Map<PeriodAnalysisCoordinateType, List<Double>> resultDataMap = algorithm.getTopHits();

		double freq = resultDataMap.get(PeriodAnalysisCoordinateType.FREQUENCY).get(0);
		double period = resultDataMap.get(PeriodAnalysisCoordinateType.PERIOD).get(0);
		double power = resultDataMap.get(PeriodAnalysisCoordinateType.POWER).get(0);
		double semiAmplitude = resultDataMap.get(PeriodAnalysisCoordinateType.SEMI_AMPLITUDE).get(0);

		PeriodAnalysisDataPoint topHitDataPoint = new PeriodAnalysisDataPoint(freq, period, power, semiAmplitude);

		// Create a multi-periodic fit model based upon the specified periods.
		model = new PeriodAnalysisDerivedMultiPeriodicModel(topHitDataPoint, harmonics, algorithm);
		try {
			try {
				algorithm.multiPeriodicFit(harmonics, model);
			} catch (InterruptedException e) {
				// We should never end up here in the course of this unit test
				// since there's no user in the loop to cause an interruption.
				fail();
			}

			// Check the model parameters.
			assertEquals(expectedParamsList.size(), model.getParameters().size());

			for (int i = 0; i < expectedParamsList.size(); i++) {
				assertTrue(expectedParamsList.get(i).equals(model.getParameters().get(i)));
			}

			// Check the model and residual data.
			checkData(expectedModelData, model.getFit());
			checkData(expectedResidualData, model.getResiduals());

		} catch (AlgorithmError e) {
			fail();
		}
	}

	protected void checkData(double[][] expectedData, List<ValidObservation> obs) {
		assertEquals(expectedData.length, obs.size());

		for (int i = 0; i < expectedData.length; i++) {
			// JD
			assertTrue(Tolerance.areClose(expectedData[i][0], obs.get(i).getJD(), DELTA, true));

			// Magnitude
			assertTrue(Tolerance.areClose(expectedData[i][1], obs.get(i).getMag(), DELTA, true));
		}
	}
}