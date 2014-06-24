/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2014  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.auth;

import junit.framework.TestCase;

import org.aavso.tools.vstar.auth.AAVSOPostAuthenticationSource;
import org.aavso.tools.vstar.exception.AuthenticationError;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;

public class AAVSOPostAuthenticationSourceTest extends TestCase {

	private AAVSOPostAuthenticationSource authenticator;

	public AAVSOPostAuthenticationSourceTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		authenticator = new AAVSOPostAuthenticationSource(
				"http://dev.aavso.org/apps/api-auth/");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAuthenticateNonMember() throws AuthenticationError,
			ConnectionException {

		boolean authenticated = authenticator
				.authenticate("admin", "adminpass");

		assertTrue(authenticated);
		assertFalse(ResourceAccessor.getLoginInfo().isMember());
	}

	public void testAuthenticateMember() throws AuthenticationError,
			ConnectionException {

		boolean authenticated = authenticator.authenticate("will", "foobar");

		assertTrue(authenticated);
		assertTrue(ResourceAccessor.getLoginInfo().isMember());
	}

	public void testAuthenticateFailure() throws ConnectionException {

		boolean authenticated = false;

		try {
			authenticated = authenticator.authenticate("foo", "bar");
			fail("Expected to fail");
		} catch (AuthenticationError e) {
			assertFalse(authenticated);
		}
	}
}
