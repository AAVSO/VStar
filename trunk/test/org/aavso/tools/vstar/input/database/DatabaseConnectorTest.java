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

	// 'epsilon aur' should be found in the aliases table, but not in the validation table.
	// At the end of the day, we see a single AUID arising from the getAUID() method.
	public void testGetEpsilonAurAUID() {
		try {
			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.observationDBConnector;
			Connection connection = obsConnector.createConnection();
			String auid = obsConnector.getAUID(connection, "epsilon aur");
			assertEquals("000-BCT-905", auid);
		} catch (Exception e) {
			fail();
		}
	}

	// 'eps aur' should be found in the validation table before we ever hit the aliases 
	// table. At the end of the day, we see a single AUID arising from the getAUID() method.
	public void testGetEpsAurAUID() {
		try {
			AAVSODatabaseConnector obsConnector = AAVSODatabaseConnector.observationDBConnector;
			Connection connection = obsConnector.createConnection();
			String auid = obsConnector.getAUID(connection, "eps aur");
			assertEquals("000-BCT-905", auid);
		} catch (Exception e) {
			fail();
		}
	}
}
