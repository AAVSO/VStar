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
package org.aavso.tools.vstar.input;

import org.aavso.tools.vstar.ui.mediator.StarInfo;

/**
 * This interface must be implemented by any class wishing to retrieve star name
 * or AUID from a database.
 */
public interface IStarInfoSource {

	/**
	 * Return the AUID of the named star.
	 * 
	 * @param name
	 *            The star name or alias.
	 * @return Information about the star, e.g. name, AUID, period.
	 */
	public StarInfo getStarByName(String name) throws Exception;

	/**
	 * Return the AUID of the named star.
	 * 
	 * @param name
	 *            The star name or alias.
	 * @param minJD
	 *            The minimum JD of the range to be loaded.
	 * @param maxJD
	 *            The maximum JD of the range to be loaded.
	 * @return Information about the star, e.g. name, AUID, period.
	 */
	public StarInfo getStarByName(String name, double minJD, double maxJD)
			throws Exception;

	/**
	 * Return the name of the star given an AUID.
	 * 
	 * @param name
	 *            The AUID.
	 * @return Information about the star, e.g. name, AUID, period.
	 */
	public StarInfo getStarByAUID(String auid) throws Exception;

	/**
	 * Return the name of the star given an AUID.
	 * 
	 * @param name
	 *            The AUID.
	 * @param minJD
	 *            The minimum JD of the range to be loaded.
	 * @param maxJD
	 *            The maximum JD of the range to be loaded.
	 * @return Information about the star, e.g. name, AUID, period.
	 */
	public StarInfo getStarByAUID(String auid, double minJD, double maxJD)
			throws Exception;
}
