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

import java.util.Map;

import org.aavso.tools.vstar.exception.AuthenticationError;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.input.database.IAuthenticationSource;
import org.aavso.tools.vstar.ui.resources.LoginInfo;
import org.aavso.tools.vstar.ui.resources.LoginType;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;

import junit.framework.TestCase;

public class AuthenticationSourceTest extends TestCase {

	public AuthenticationSourceTest(String name) {
		super(name);
	}

	public void testAAVSOPostUserPassXMLAuthenticateFailure() throws ConnectionException {

		boolean authenticated = false;

		try {
			IAuthenticationSource authenticator = new AAVSOPostUserPassXMLAuthenticationSource();
			authenticated = authenticator.authenticate("foo", "bar");
			fail("Expected to fail");
		} catch (AuthenticationError e) {
			assertFalse(authenticated);
		}
	}

	public void testJSONParser() {
		String s = "{\"token\": \"966gkxj0ohy7nmf63io5kh3xvcv5nfr9\", \"user_id\": 8, \"email\": \"dbenn@computer.org\", \"obscode\": \"BDJB\", \"is_member\": true}\n";

		Map<String, String> map = Auth0JSONAutheticationSource.parseJSONString(s);

		assertTrue(map.keySet().contains("user_id"));

		for (String key : map.keySet()) {
			String val = map.get(key);
			try {
				int m = Integer.parseInt(val);
				System.out.println(m);
			} catch (Exception e) {
			}
			System.out.println(String.format("%s:%s [%s => %s]", key, val, key.getClass(), val.getClass()));
		}

		assertEquals(5, map.size());
	}

	public void testAuth0JSONAuthenticateFailure() throws ConnectionException {
		boolean authenticated = false;

		try {
			IAuthenticationSource authenticator = new Auth0JSONAutheticationSource();
			String uuid = "b4951d75-a0e6-4d2d-a718-0fad0a0c4bae";
			Integer code = 277628;
			authenticated = authenticator.authenticate(uuid, code.toString());
			assertTrue(authenticated);
			LoginInfo info = ResourceAccessor.getLoginInfo();
			assertEquals(LoginType.AAVSO, info.getType());
		} catch (AuthenticationError e) {
			// If the code above is valid, the block above will not fail,
			// otherwise we test whether there was an auth error. This
			// allows us to interactively test the functionality above
			// that will often fail in automated test runs.
			assertFalse(authenticated);
		}
	}
}
