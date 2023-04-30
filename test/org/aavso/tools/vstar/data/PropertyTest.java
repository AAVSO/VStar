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
package org.aavso.tools.vstar.data;

import static org.junit.Assert.*;

import org.aavso.tools.vstar.util.Tolerance;
import org.junit.Test;

/**
 * Property class unit tests.
 */
public class PropertyTest {

	@Test
	public void testIntProp() {
		Property prop = new Property(42);
		assertEquals(Property.propType.INTEGER, prop.getType());
		assertEquals(42, prop.getIntVal());
		assertEquals("42", prop.toString());
	}

	@Test
	public void testRealProp() {
		Property prop = new Property(42.0);
		assertEquals(Property.propType.REAL, prop.getType());
		Tolerance.areClose(42.0, prop.getRealVal(), 1e-6, true);
		assertEquals("42.0", prop.toString());
	}
	
	@Test
	public void testBooleanProp() {
		Property prop = new Property(true);
		assertEquals(Property.propType.BOOLEAN, prop.getType());
		assertEquals(true, prop.getBoolVal());
		assertEquals("true", prop.toString());
	}
	
	@Test
	public void testStringProp() {
		Property prop = new Property("42");
		assertEquals(Property.propType.STRING, prop.getType());
		assertEquals("42", prop.getStrVal());
		assertEquals("42", prop.toString());
	}
}
