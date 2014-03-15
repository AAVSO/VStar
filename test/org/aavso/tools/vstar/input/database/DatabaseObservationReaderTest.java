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
package org.aavso.tools.vstar.input.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;

/**
 * This is a unit test for reading observations from a text file. This is really
 * more of an integration test than a unit test since we are asking for data
 * from an AAVSO database.
 */
public class DatabaseObservationReaderTest extends TestCase {

	/**
	 * Constructor
	 * 
	 * @param name
	 *            The test case name.
	 */
	public DatabaseObservationReaderTest(String name) {
		super(name);
	}

	// Read a result set from the database for Eps Aur in the
	// Julian Day range 2454000.5..2454939.56597 and check some
	// of the known observation's values.
	public void testReadValidObservationEpsAur() {
		try {
			AAVSODatabaseConnector connector = AAVSODatabaseConnector.observationDBConnector;
			Connection connection = connector.createConnection();
			assertNotNull(connection);

			PreparedStatement stmt = connector
					.createObservationWithJDRangeQuery(connection);
			assertNotNull(stmt);

			connector.setObservationWithJDRangeQueryParams(stmt, "000-BCT-905",
					2454000.5, 2454939.56597);

			ResultSet results = stmt.executeQuery();

			AAVSODatabaseObservationReader reader = new AAVSODatabaseObservationReader(
					results);
			reader.retrieveObservations();
			List<ValidObservation> obs = reader.getValidObservations();

			boolean found = false;
			for (ValidObservation ob : obs) {
				SeriesType band = ob.getBand();
				if (band == SeriesType.Johnson_V) {
					double jd = ob.getDateInfo().getJulianDay();
					assertEquals(2454001.8325, jd);
					double mag = ob.getMag();
					assertEquals(3.0544, mag);
					double error = ob.getMagnitude().getUncertainty();
					assertEquals(0.0105, error);
					found = true;
					break;
				}
			}

			assertTrue(found);
		} catch (Exception e) {
			fail();
		}
	}
}
