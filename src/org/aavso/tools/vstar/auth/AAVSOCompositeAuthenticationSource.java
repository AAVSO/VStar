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

import org.aavso.tools.vstar.exception.AuthenticationError;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.input.database.IAuthenticationSource;
import org.aavso.tools.vstar.ui.resources.LoginType;

/**
 * This authentication source uses two other authenticators to authenticate
 * against AAVSO.
 * @deprecated
 */
public class AAVSOCompositeAuthenticationSource implements
		IAuthenticationSource {

	private AAVSOPostAuthenticationSource aavsoPostAuthSource;
//	private AAVSOAuthenticationSource aavsoDatabaseAuthSource;
	
	public AAVSOCompositeAuthenticationSource() {
		aavsoPostAuthSource = new AAVSOPostAuthenticationSource();
		// TODO: remove this, relying only on AAVSOPostAuthenticationSource
//		aavsoDatabaseAuthSource = new AAVSOAuthenticationSource();
	}

	@Override
	public boolean authenticate(String username, String password)
			throws AuthenticationError, ConnectionException {
		
		boolean authenticated = aavsoPostAuthSource.authenticate(username, password);
		
		// TODO: remove this, relying only on AAVSOPostAuthenticationSource
//		if (authenticated) {
//			aavsoDatabaseAuthSource.setAuthenticated(true);
//			aavsoDatabaseAuthSource.retrieveUserInfo(aavsoPostAuthSource.getUserID());
//		}
		
		return authenticated;
	}

	@Override
	public LoginType getLoginType() {
		return LoginType.AAVSO;
	}
}
