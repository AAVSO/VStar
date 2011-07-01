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

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.model.MultiPeriodicFit;
import org.aavso.tools.vstar.util.model.PeriodFitParameters;

public class MultiPeriodicModelDcDftTestBase extends DataTestBase {

	public MultiPeriodicModelDcDftTestBase(String name, double[][] jdAndMagPairs) {
		super(name, jdAndMagPairs);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected void commonTest(TSDcDft dcdft, List<Double> periods,
			List<PeriodFitParameters> expectedParamsList,
			double[][] expectedModelData, double[][] expectedResidualData) {

		// Create a multi-periodic fit model based upon the specified periods.
		MultiPeriodicFit fit = dcdft.multiPeriodicFit(periods);

		// Check the model parameters.
		assertEquals(expectedParamsList.size(), fit.getParameters().size());
		
		for (int i = 0; i < expectedParamsList.size(); i++) {
			assertTrue(expectedParamsList.get(i).equals(
					fit.getParameters().get(i)));
		}

		// Check the model data.
		checkData(expectedModelData, fit.getFit());

		// Check the residual data.
		checkData(expectedResidualData, fit.getResiduals());
	}

	protected void checkData(double[][] expectedData, List<ValidObservation> obs) {
		assertEquals(expectedData.length, obs.size());
		
		for (int i = 0; i < expectedData.length; i++) {
			// JD
			assertEquals(String.format("%1.4f", expectedData[i][0]), String
					.format("%1.4f", obs.get(i).getJD()));

			// Magnitude
			assertEquals(String.format("%1.4f", expectedData[i][1]), String
					.format("%1.4f", obs.get(i).getMag()));
		}
	}
}