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

import junit.framework.TestCase;

import org.aavso.tools.vstar.ui.resources.LoginInfo;

/**
 * Unit tests for VSX member info web service class.
 */
public class VSXWebServiceMemberInfoTest extends TestCase {

	public void testMemberInfoUserIsMember() throws Exception {
		VSXWebServiceMemberInfo memberInfo = new VSXWebServiceMemberInfo();
		LoginInfo info = new LoginInfo();
		memberInfo.retrieveUserInfo("3949", info);
		assertEquals("WLP", info.getObserverCode());
		assertEquals(true, info.isMember());
	}

	public void testMemberInfoUserIsNotMember() throws Exception {
		VSXWebServiceMemberInfo memberInfo = new VSXWebServiceMemberInfo();
		LoginInfo info = new LoginInfo();
		memberInfo.retrieveUserInfo("5603", info);
		assertEquals("GNT", info.getObserverCode());
		assertEquals(false, info.isMember());
	}

	public void testMemberInfoUserDoesNotExist() throws Exception {
		VSXWebServiceMemberInfo memberInfo = new VSXWebServiceMemberInfo();
		LoginInfo info = new LoginInfo();
		memberInfo.retrieveUserInfo("1", info);
		assertEquals("", info.getObserverCode());
		assertEquals(false, info.isMember());
	}
}
