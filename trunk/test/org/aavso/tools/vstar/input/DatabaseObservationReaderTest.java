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
package org.aavso.tools.vstar.input;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import junit.framework.TestCase;

import org.aavso.tools.vstar.input.database.AAVSODatabaseConnector;

/**
 * This is a unit test for reading observations from a text file. This is really
 * more of an integration test than a unit test since we are asking for data
 * from an AAVSO database.
 */
public class DatabaseObservationReaderTest extends TestCase {

	private AAVSODatabaseConnector connector = AAVSODatabaseConnector.observationDBConnector;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            The test case name.
	 */
	public DatabaseObservationReaderTest(String name) {
		super(name);
	}

	// Valid tests.

	// Read a result set from the database for Epsilon Auriga in the
	// Julian Day range 2454000.5..2454939.56597.
	public void testSampleRead1() {
		try {
			Connection connection = connector.createConnection();
			assertNotNull(connection);

			PreparedStatement stmt = connector
					.createObservationQuery(connection);
			assertNotNull(stmt);

			connector.setObservationQueryParams(stmt, "000-BCT-905", 2454000.5,
					2454939.56597);

			ResultSet results = stmt.executeQuery();
			assertTrue(results.next());

			double mag = results.getDouble("magnitude");
			assertEquals(3.0, mag);
		} catch (Exception e) {
			fail();
		}
	}
}
