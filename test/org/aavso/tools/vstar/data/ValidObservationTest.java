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

import junit.framework.TestCase;

/**
 * ValidObservation unit tests<br/>
 * 
 * Note: Should be more tests here given the centrality of this class. Had there
 * been a test case for what happens when a magnitude is changed, the Fly-weight
 * pattern derived mutable observation bug present before 2.21.1 would have been
 * picked up earlier; of course that assumes I had thought to write such a test
 * case :| (dbenn, Apr 15 2022)
 */
public class ValidObservationTest extends TestCase {

	private ValidObservation ob;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ob = new ValidObservation();
		ob.setMagnitude(new Magnitude(2, 0.02));
		ob.setJD(2459684.50764);
		ob.setBand(SeriesType.Johnson_V);
		ob.setName("FooStar");
		// Note: add more setters; the focus at time of writing was copy() and
		// non-mutable magnitude, and other class-based members such as CommentCodes,
		// SeriesType, ... have no instance setter methods
	}

	// copying an observation should lead to different magnitude and details map
	// references
	public void testObCopyNotSameRefs() {
		ValidObservation obCopy = ob.copy();
		assertEquals(ob.getMagnitude(), obCopy.getMagnitude());
		assertNotSame(ob.getMagnitude(), obCopy.getMagnitude());
		assertNotSame(ob.getDetails(), obCopy.getDetails());
	}

	// copying an observation should lead to different magnitude and details map
	// references; we arbitrarily set series to Visual as opposed to user defined
	// (the only requirement for testing is that it be different to band, Johnson V
	// in this case)
	public void testObCopyWithSeries() {
		ValidObservation obCopy = ob.copy(SeriesType.Filtered);
		assertTrue(ob.getBand() == obCopy.getBand());
		assertTrue(ob.getSeries() != obCopy.getSeries());
		assertTrue(obCopy.getBand() != obCopy.getSeries());
	}

	// changing one observation's magnitude should not change another's
	public void testObMagNotMutable() {
		ValidObservation obCopy = ob.copy();
		obCopy.setMagnitude(new Magnitude(3, 0.03));
		assertFalse(ob.getMagnitude() == obCopy.getMagnitude());
	}
}
