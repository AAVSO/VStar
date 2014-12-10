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
import java.util.Map;

import junit.framework.TestCase;

import org.aavso.tools.vstar.ui.mediator.StarInfo;

/**
 * AAVSODatabaseConnector unit test.
 */
public class DatabaseConnectorTest extends TestCase {

	private static IStarNameAndAUIDSource aidStarNameAndAUIDRetriever = new AIDStarNameAndAUIDSource();
	private static IStarNameAndAUIDSource vsxStarNameAndAUIDRetriever = new VSXStarNameAndAUIDSource();
	
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
		assertEquals("acbd18db4cc2f85cedef654fccc4a4d8", Authenticator
				.generateHexDigest("foo"));
	}

	// AID accessor tests.
	
	// 'epsilon aur' should be found in the aliases table, but not in the
	// validation table.
	// At the end of the day, we see a single AUID arising from the getAUID()
	// method.
	public void testGetEpsilonAurAUIDViaAID() {
		try {
			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = obsConnector.createConnection();
			StarInfo info = aidStarNameAndAUIDRetriever.getStarByName(connection, "epsilon aur");
			assertEquals("000-BCT-905", info.getAuid());
		} catch (Exception e) {
			fail();
		}
	}

	// 'eps aur' should be found in the validation table before we ever hit the
	// aliases
	// table. At the end of the day, we see a single AUID arising from the
	// getAUID() method.
	public void testGetEpsAurAUIDViaAID() {
		try {
			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = obsConnector.createConnection();
			StarInfo info = aidStarNameAndAUIDRetriever.getStarByName(connection, "eps aur");
			assertEquals("000-BCT-905", info.getAuid());
		} catch (Exception e) {
			fail();
		}
	}

	// '000-BCT-905' should be found in the validation table as corresponding to
	// Epsilon Aurigae.
	public void testGetEpsAurFromAUIDViaAID() {
		try {
			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = obsConnector.createConnection();
			StarInfo info = aidStarNameAndAUIDRetriever.getStarByAUID(connection,
					"000-BCT-905");
			// Test for equality, trimming and ignoring case.
			// TODO: trim at source!
			assertTrue("Eps Aur".equalsIgnoreCase(info.getDesignation().trim()));
		} catch (Exception e) {
			fail();
		}
	}

	// VSX accessor tests.

	// TODO: switch to AAVSODatabaseConnector.utDBConnector when VSX tables 
	// are added to test database.
	
	// Retrieve 'epsilon aur' AUID..
	public void testGetEpsilonAurAUIDViaVSX() {
		try {
			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = obsConnector.createConnection();
			StarInfo info = vsxStarNameAndAUIDRetriever.getStarByName(connection, "epsilon aur");
			assertEquals("000-BCT-905", info.getAuid());
		} catch (Exception e) {
			fail();
		}
	}

	// Retrieve 'eps aur' AUID.
	public void testGetEpsAurAUIDViaVSX() {
		try {
			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = obsConnector.createConnection();
			StarInfo info = vsxStarNameAndAUIDRetriever.getStarByName(connection, "eps aur");
			assertEquals("000-BCT-905", info.getAuid());
		} catch (Exception e) {
			fail();
		}
	}
	
	// Find star name corresponding to '000-BCT-905' (Eps Aur).
	public void testGetEpsAurFromAUIDViaVSX() {
		try {
			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = obsConnector.createConnection();
			StarInfo info = vsxStarNameAndAUIDRetriever.getStarByAUID(connection,
					"000-BCT-905");
			// Test for equality, trimming and ignoring case.
			assertTrue("Eps Aur".equalsIgnoreCase(info.getDesignation().trim()));
		} catch (Exception e) {
			fail();
		}
	}

	// Retrieve 'Nova Eridani 2009' AUID.
	public void testGetNEridaniAUIDViaVSX() {
		try {
			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = obsConnector.createConnection();
			StarInfo info = vsxStarNameAndAUIDRetriever.getStarByName(connection, "N Eri 2009");
			assertEquals("000-BJR-847", info.getAuid());
		} catch (Exception e) {
			fail();
		}
	}

	// Retrieve 'KT Eri' (same as 'N Eri 2009') AUID.
	public void testGetKTEridaniAUIDViaVSX() {
		try {
			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = obsConnector.createConnection();
			StarInfo info = vsxStarNameAndAUIDRetriever.getStarByName(connection, "KT Eri");
			assertEquals("000-BJR-847", info.getAuid());
		} catch (Exception e) {
			fail();
		}
	}
	
	// Find star name corresponding to '000-BJR-847' (Nova Eridani 2009).
	public void testGetNovaEridani2009FromAUIDViaVSX() {
		try {
			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = obsConnector.createConnection();
			StarInfo info = vsxStarNameAndAUIDRetriever.getStarByAUID(connection,
					"000-BJR-847");
			// Test for equality, trimming and ignoring case.
			// TODO: trim at source (in dialog)
			assertTrue("KT Eri".equalsIgnoreCase(info.getDesignation().trim()));
		} catch (Exception e) {
			fail();
		}
	}
	
	// Raw observation data reading tests.
	
	// Read a result set from the database for Epsilon Aurigae in the
	// Julian Day range 2454000.5..2454939.56597.
	// TODO: UT database doesn't have most recent fields: pubref, digitizer, ...
	public void noTestSampleRead1() {
		try {
			AAVSODatabaseConnector connector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = connector.createConnection();
			assertNotNull(connection);

			PreparedStatement stmt = connector
					.createObservationWithJDRangeQuery(connection);
			assertNotNull(stmt);

			connector.setObservationWithJDRangeQueryParams(stmt, "000-BCT-905", 2454000,
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

			results.close();
		} catch (Exception e) {
			fail();
		}
	}
	
	// Read a result set from the database for Epsilon Aurigae in the
	// Julian Day range 2455113.34255..2454486.50292.
	// TODO: UT database doesn't have most recent fields: pubref, digitizer, ...
	public void noTestSampleRead2() {
		try {
			AAVSODatabaseConnector connector = AAVSODatabaseConnector.utDBConnector;
			Connection connection = connector.createConnection();
			assertNotNull(connection);

			PreparedStatement stmt = connector
					.createObservationWithJDRangeQuery(connection);
			assertNotNull(stmt);

			connector.setObservationWithJDRangeQueryParams(stmt, "000-BCT-905", 2445231.9900,
					2445237.9700);

			ResultSet results = stmt.executeQuery();

			double x = Double.parseDouble(".007");
			
			// Look for a particular observation that we know exists
			// in the database and check the magnitude we find there.
			boolean found = false;
			while (results.next()) {
				double jd = results.getDouble("JD");
				double mag = results.getDouble("magnitude");
				if (jd == 2445231.9900 && mag == 3.954) {
					double uncertainty = results.getDouble("uncertainty");
					assertEquals(0.007, uncertainty);
					found = true;
				}
			}
			assertTrue(found);
			
			results.close();
		} catch (Exception e) {
			fail();			
		}
	}
	
	public void testRetrieveCreditMap() throws Exception {
		AAVSODatabaseConnector connector = AAVSODatabaseConnector.observationDBConnector;
		Map<Integer, String> creditMap = connector.retrieveCreditMap();
		assertEquals(4, creditMap.size());
	}
}
