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

import junit.framework.TestCase;

/**
 * AAVSODatabaseConnector unit test.
 */
public class DatabaseConnectorTest extends TestCase {

	/**
	 * Constructor
	 * 
	 * @param name
	 *            Test name.
	 */
	public DatabaseConnectorTest(String name) {
		super(name);
	}

	// Valid tests

	public void testGenerateMessageDigest() {
		assertEquals("acbd18db4cc2f85cedef654fccc4a4d8", AAVSODatabaseConnector
				.generateHexDigest("foo"));
	}

	// 'epsilon aur' should be found in the aliases table, but not in the
	// validation table.
	// At the end of the day, we see a single AUID arising from the getAUID()
	// method.
	public void testGetEpsilonAurAUID() {
		try {
			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = obsConnector.createConnection();
			String auid = obsConnector.getAUID(connection, "epsilon aur");
			assertEquals("000-BCT-905", auid);
		} catch (Exception e) {
			fail();
		}
	}

	// 'eps aur' should be found in the validation table before we ever hit the
	// aliases
	// table. At the end of the day, we see a single AUID arising from the
	// getAUID() method.
	public void testGetEpsAurAUID() {
		try {
			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = obsConnector.createConnection();
			String auid = obsConnector.getAUID(connection, "eps aur");
			assertEquals("000-BCT-905", auid);
		} catch (Exception e) {
			fail();
		}
	}

	// '000-BCT-905' should be found in the validation table as corresponding to
	// Epsilon Aurigae.
	public void testGetEpsAurFromAUID() {
		try {
			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = obsConnector.createConnection();
			String starName = obsConnector.getStarName(connection,
					"000-BCT-905");
			// Test for equality, trimming and ignoring case.
			assertTrue("Eps Aur".equalsIgnoreCase(starName.trim()));
		} catch (Exception e) {
			fail();
		}
	}

	// Read a result set from the database for Epsilon Aurigae in the
	// Julian Day range 2454000.5..2454939.56597.
	public void testSampleRead() {
		try {
			AAVSODatabaseConnector connector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = connector.createConnection();
			assertNotNull(connection);

			PreparedStatement stmt = connector
					.createObservationQuery(connection);
			assertNotNull(stmt);

			connector.setObservationQueryParams(stmt, "000-BCT-905", 2454000,
					2454150);

			ResultSet results = stmt.executeQuery();

			// Look for a particular observation that we know exists
			// in the database and check the magnitude we find there.
			boolean found = false;
			while (results.next()) {
				double jd = results.getDouble("JD");
				if (jd == 2454134.3819) {
					double mag = results.getDouble("magnitude");
					assertEquals(3.0, mag);
					int band = results.getInt("band");
					assertEquals(0, band); // Visual band
					found = true;
				}
			}
			assertTrue(found);
		} catch (Exception e) {
			fail();
		}
	}
}
