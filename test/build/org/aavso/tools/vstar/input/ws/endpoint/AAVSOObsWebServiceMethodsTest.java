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
package build.org.aavso.tools.vstar.input.ws.endpoint;

import java.util.Iterator;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.UnknownAUIDError;
import org.aavso.tools.vstar.exception.UnknownStarError;
import org.aavso.tools.vstar.input.database.DatabaseType;
import org.aavso.tools.vstar.input.ws.endpoint.AAVSOObsWebService;
import org.aavso.tools.vstar.input.ws.endpoint.IObsWebService;
import org.aavso.tools.vstar.ui.mediator.StarInfo;

/**
 * This JUnit test class tests the AID web service methods.
 */
public class AAVSOObsWebServiceMethodsTest extends TestCase {

	public AAVSOObsWebServiceMethodsTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// Valid tests.

	public void testGetEpsAurInfoByName() {
		IObsWebService service = new AAVSOObsWebService(DatabaseType.UT);

		try {
			StarInfo info = service.getStarInfoByName("eps Aur");
			assertEquals("eps Aur", info.getDesignation());
		} catch (ConnectionException e) {
			fail();
		} catch (UnknownStarError e) {
			fail();
		}
	}

	public void testGetEpsAurInfoByAUID() {
		IObsWebService service = new AAVSOObsWebService(DatabaseType.UT);

		try {
			StarInfo info = service.getStarInfoByAUID("000-BCT-905");
			assertEquals("eps Aur", info.getDesignation());
		} catch (ConnectionException e) {
			fail();
		} catch (UnknownAUIDError e) {
			fail();
		}
	}

	public void testGetEpsAurObsWithJDRange() {
		IObsWebService service = new AAVSOObsWebService(DatabaseType.UT);

		try {
			StarInfo info = service.getStarInfoByAUID("000-BCT-905");
			assertEquals("eps Aur", info.getDesignation());

			ValidObservation[] obs = service.getObservationsWithJDRange(info,
					2454000, 2454010);
			
			assertTrue(obs.length != 0);
			ValidObservation ob = obs[0];
			assertTrue(ob.getDateInfo() != null);
		} catch (ConnectionException e) {
			fail();
		} catch (UnknownAUIDError e) {
			fail();
		} catch (ObservationReadError e) {
			fail();
		}
	}

	// Invalid tests.
}
