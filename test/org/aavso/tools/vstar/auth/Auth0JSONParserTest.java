/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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

import junit.framework.TestCase;

public class Auth0JSONParserTest extends TestCase {

	public Auth0JSONParserTest(String name) {
		super(name);
	}

	public void testSimpleKeyValue() {
		Map<String, String> m = Auth0JSONAutheticationSource
				.parseJSONString("{\"key\": \"value\"}");
		assertEquals("value", m.get("key"));
	}

	public void testMultipleKeyValues() {
		Map<String, String> m = Auth0JSONAutheticationSource
				.parseJSONString("{\"a\": \"1\", \"b\": \"2\"}");
		assertEquals("1", m.get("a"));
		assertEquals("2", m.get("b"));
	}

	public void testBooleanValue() {
		Map<String, String> m = Auth0JSONAutheticationSource
				.parseJSONString("{\"is_member\": true}");
		assertEquals("true", m.get("is_member"));
	}

	public void testNumericValue() {
		Map<String, String> m = Auth0JSONAutheticationSource
				.parseJSONString("{\"user_id\": 8}");
		assertEquals("8", m.get("user_id"));
	}

	public void testWithWhitespace() {
		Map<String, String> m = Auth0JSONAutheticationSource
				.parseJSONString("  {  \"a\"  :  \"1\"  ,  \"b\"  :  \"2\"  }  ");
		assertEquals("1", m.get("a"));
		assertEquals("2", m.get("b"));
	}

	public void testRealWorldResponse() {
		String json = "{\"is_member\": true, \"user_id\": 8, \"obscode\": \"BDJB\", \"email\": \"test@test.org\", \"token\": \"abc123\"}";
		Map<String, String> m = Auth0JSONAutheticationSource
				.parseJSONString(json);
		assertEquals("true", m.get("is_member"));
		assertEquals("8", m.get("user_id"));
		assertEquals("BDJB", m.get("obscode"));
		assertEquals("test@test.org", m.get("email"));
		assertEquals("abc123", m.get("token"));
	}
}
