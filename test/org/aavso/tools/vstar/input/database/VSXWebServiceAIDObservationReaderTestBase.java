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
package org.aavso.tools.vstar.input.database;

import java.io.FileInputStream;
import java.util.List;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidationType;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.ob.src.impl.AIDWebServiceObservationSourcePluginBase;
import org.aavso.tools.vstar.plugin.ob.src.impl.UTF8FilteringInputStream;
import org.aavso.tools.vstar.ui.mediator.StarInfo;

/**
 * Unit (or integration) base class for tests that read AID observations via the
 * VSX web service.
 */
public abstract class VSXWebServiceAIDObservationReaderTestBase extends TestCase {

	private Class<? extends AIDWebServiceObservationSourcePluginBase> obsSourceClass;
	protected AIDWebServiceObservationSourcePluginBase obsSource;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            Test name
	 */
	public VSXWebServiceAIDObservationReaderTestBase(String name) {
		super(name);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            Test name
	 * @param obsSourceClass
	 *            Name of the observation source class
	 */
	public VSXWebServiceAIDObservationReaderTestBase(
			String name,
			Class<? extends AIDWebServiceObservationSourcePluginBase> obsSourceClass) {
		this(name);
		this.obsSourceClass = obsSourceClass;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		obsSource = obsSourceClass.newInstance();
	}

	// Read a result set from the database for Eps Aur in the
	// Julian Day range 2454000.5..2454939.56597 and check some
	// of the known observation's values.
	public void testReadValidObservationEpsAur() {
		try {
			VSXWebServiceStarInfoSource infoSrc = new VSXWebServiceStarInfoSource();
			StarInfo info = infoSrc.getStarByName("eps Aur");

			obsSource.setInfo(info);

			obsSource.setUrl(obsSource.createAIDUrlForAUID(info.getAuid(),
					2454000.5, 2454939.56597));

			AbstractObservationRetriever reader = obsSource
					.getObservationRetriever();
			reader.retrieveObservations();
			List<ValidObservation> obs = reader.getValidObservations();

			assertEquals(2443, obs.size());

			boolean found = false;
			for (ValidObservation ob : obs) {
				SeriesType band = ob.getBand();
				if (band == SeriesType.Johnson_V) {
					double jd = ob.getDateInfo().getJulianDay();
					assertEquals(2454001.8325, jd);
					double mag = ob.getMag();
					assertEquals(3.0544, mag);
					double error = ob.getMagnitude().getUncertainty();
					assertEquals(0.0105, error);
					found = true;
					break;
				}
			}

			assertTrue(found);
		} catch (Exception e) {
			fail();
		}
	}

	// Read data for TT Cen and check the number.
	public void testReadValidObservationTTCen() {
		try {
			VSXWebServiceStarInfoSource infoSrc = new VSXWebServiceStarInfoSource();
			StarInfo info = infoSrc.getStarByName("TT Cen");

			obsSource.setInfo(info);

			obsSource.setUrl(obsSource.createAIDUrlForAUID(info.getAuid(),
					2454000, 2454100));

			AbstractObservationRetriever reader = obsSource
					.getObservationRetriever();
			reader.retrieveObservations();
			List<ValidObservation> obs = reader.getValidObservations();

			assertEquals(12, obs.size());

		} catch (Exception e) {
			fail();
		}
	}

	// Check that a file containing a non UTF-8 character (so not XML 1.0
	// compliant) can be filtered out.
	public void testNonUTF8Char() throws Exception {
		String xcrbPath = "test/org/aavso/tools/vstar/input/database/xcrb.xml";
		FileInputStream xcrbIn = new FileInputStream(xcrbPath);

		UTF8FilteringInputStream reader = new UTF8FilteringInputStream(
				xcrbIn);

		int b;
		int count = 0;
		boolean foundNotUTF = false;

		while ((b = reader.read()) != -1) {
			count++;
			if (b == 0x1a) {
				foundNotUTF = true;
				break;
			}
		}

		reader.close();

		assertEquals(559, count);
		assertFalse(foundNotUTF);
	}

	// Read data for SS Cyg and check valflags.
	public void testReadValidObservationAndCheckValFlagsSSCyg() {
		try {
			VSXWebServiceStarInfoSource infoSrc = new VSXWebServiceStarInfoSource();
			StarInfo info = infoSrc.getStarByName("SS Cyg");

			obsSource.setInfo(info);

			obsSource.setUrl(obsSource.createAIDUrlForAUID(info.getAuid(),
					2457301.2, 2457301.3));

			AbstractObservationRetriever reader = obsSource
					.getObservationRetriever();
			reader.retrieveObservations();
			List<ValidObservation> obs = reader.getValidObservations();

			assertEquals(2, obs.size());
			assertEquals(ValidationType.GOOD, obs.get(0).getValidationType());
			assertEquals(ValidationType.GOOD, obs.get(1)
					.getValidationType());

		} catch (Exception e) {
			fail();
		}
	}

	// Helpers

	protected void commonReadValidObservationASASSN18ey(String bands,
			String obscodes, boolean minFields, int expectedCount) {
		try {
			VSXWebServiceStarInfoSource infoSrc = new VSXWebServiceStarInfoSource();
			StarInfo info = infoSrc.getStarByName("ASASSN-18ey");

			obsSource.setInfo(info);

			obsSource.setUrl(obsSource.createAIDUrlForAUID(info.getAuid(),
					2458368.5 - 1, 2458368.5, bands, obscodes, false));

			AbstractObservationRetriever reader = obsSource
					.getObservationRetriever();
			reader.retrieveObservations();
			List<ValidObservation> obs = reader.getValidObservations();

			assertEquals(expectedCount, obs.size());

		} catch (Exception e) {
			fail();
		}
	}
}
