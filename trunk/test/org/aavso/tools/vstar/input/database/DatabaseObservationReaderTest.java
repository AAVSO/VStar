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
import org.aavso.tools.vstar.data.ValidationType;

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
	// Julian Day range 2454000.5..2454939.56597 and check one
	// of the known observation's values.
	// TODO: UT database doesn't have most recent fields: pubref, digitizer, ...
	public void testReadValidObservationEpsAur1() {
		if (false) {
			try {
				AAVSODatabaseConnector connector = AAVSODatabaseConnector.utDBConnector;
				Connection connection = connector.createConnection();
				assertNotNull(connection);

				PreparedStatement stmt = connector
						.createObservationWithJDRangeQuery(connection);
				assertNotNull(stmt);

				connector.setObservationWithJDRangeQueryParams(stmt,
						"000-BCT-905", 2454000.5, 2454939.56597);

				ResultSet results = stmt.executeQuery();

				AAVSODatabaseObservationReader reader = new AAVSODatabaseObservationReader(
						results);
				reader.retrieveObservations();
				List<ValidObservation> obs = reader.getValidObservations();

				boolean found = false;
				for (ValidObservation ob : obs) {
					double jd = ob.getDateInfo().getJulianDay();
					if (jd == 2454134.3819) {
						double mag = ob.getMag();
						assertEquals(3.0, mag);
						SeriesType band = ob.getBand();
						assertEquals(SeriesType.Visual, band);
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

	// Read an observation that is known to have been marked as deleted in the
	// the test database.
	// See
	// http://sourceforge.net/tracker/?func=detail&aid=2858633&group_id=263306&atid=1152052
	// TODO: UT database doesn't have most recent fields: pubref, digitizer, ...
	public void noTestReadDeletedEpsAurObservation1() {
		try {
			AAVSODatabaseConnector connector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = connector.createConnection();
			assertNotNull(connection);

			PreparedStatement stmt = connector
					.createObservationWithJDRangeQuery(connection);
			assertNotNull(stmt);

			// U Scorpii
			connector.setObservationWithJDRangeQueryParams(stmt, "000-BBX-412",
					2455139.89306, 2455139.89306);

			ResultSet results = stmt.executeQuery();

			AAVSODatabaseObservationReader reader = new AAVSODatabaseObservationReader(
					results);
			reader.retrieveObservations();
			List<ValidObservation> obs = reader.getValidObservations();

			// There are two observations, the first of which is deleted.
			//
			// select name,jd,valflag from observations where auid =
			// '000-BBX-412'
			// and jd >= 2455139.89306 and jd <= 2455139.89306;
			// +-------+---------------+---------+
			// | name | jd | valflag |
			// +-------+---------------+---------+
			// | U SCO | 2455139.89306 | Y |
			// | U SCO | 2455139.89306 | Z |
			// +-------+---------------+---------+
			//
			// We should only see 1 since we are excluding it via the query.

			assertEquals(1, obs.size());
			assertEquals(ValidationType.PREVALIDATION, obs.get(0)
					.getValidationType());

		} catch (Exception e) {
			fail();
		}
	}
}
