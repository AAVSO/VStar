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
package org.aavso.tools.vstar.input.database;

import org.aavso.tools.vstar.exception.AuthenticationError;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.ui.resources.LoginType;

/**
 * All sources of authentication must implement this interface.
 */
public interface IAuthenticationSource {

	/**
	 * Authenticate against this authentication source.
	 * 
	 * @param username
	 *            The user name to use for authentication.
	 * @param password
	 *            The password to use for authentication.
	 * @return Whether or not we have successfully authenticated.
	 * 
	 * @throws ConnectionException
	 *             if there was an authentication source connection error.
	 * @throws AuthenticationError
	 *             if there was some other error during authentication.
	 */
	public boolean authenticate(String username, String password)
			throws AuthenticationError, ConnectionException;

	/**
	 * Returns the login type for this authenticator.
	 * @deprecated
	 * 
	 * @return The login type.
	 */
	public LoginType getLoginType();
}
