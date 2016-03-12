/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2013  AAVSO (http://www.aavso.org/)
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

import junit.framework.TestCase;

import org.aavso.tools.vstar.ui.mediator.StarInfo;

/**
 * VSX web service unit tests.
 * @deprecated
 */
public class VSXWebServiceVOTableStarNameAndAUIDSourceTest extends TestCase {

	private VSXWebServiceVOTableStarNameAndAUIDSource source;
	
	public VSXWebServiceVOTableStarNameAndAUIDSourceTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		source = new VSXWebServiceVOTableStarNameAndAUIDSource();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetStarByAUIDRCar() throws Exception {
		StarInfo info = source.getStarByAUID(null, "000-BBQ-500");
		assertEquals("R Car", info.getDesignation());
	}

	public void testGetStarByAUIDEpsAur() throws Exception {
		StarInfo info = source.getStarByAUID(null, "000-BCT-905");
		assertEquals("eps Aur", info.getDesignation());
	}

	public void testGetStarByNameRCar() throws Exception {
		StarInfo info = source.getStarByName(null, "R Car");
		assertEquals("000-BBQ-500", info.getAuid());
	}
	
	public void testGetStarByNameEpsAur() throws Exception {
		StarInfo info = source.getStarByName(null, "eps Aur");
		assertEquals("000-BCT-905", info.getAuid());
	}
}
