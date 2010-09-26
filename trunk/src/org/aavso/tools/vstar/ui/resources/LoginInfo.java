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
package org.aavso.tools.vstar.ui.resources;

/**
 * Information relating to login.
 */
public class LoginInfo {

	private LoginType type;
	private String userName;
	private String observerCode;
	
	
	/**
	 * Constructor.
	 */
	public LoginInfo() {
		super();
		this.type = null;
		this.userName = null;
		this.observerCode = null;
	}

	/**
	 * @return the type
	 */
	public LoginType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(LoginType type) {
		this.type = type;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the observerCode
	 */
	public String getObserverCode() {
		return observerCode;
	}

	/**
	 * @param observerCode the observerCode to set
	 */
	public void setObserverCode(String observerCode) {
		this.observerCode = observerCode;
	}

}
