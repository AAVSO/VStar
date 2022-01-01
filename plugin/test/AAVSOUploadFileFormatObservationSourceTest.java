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


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.MTypeType;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.external.plugin.AAVSOUploadFileFormatObservationSource;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;

import junit.framework.TestCase;

/**
 * AAVSO Upload (Visual and Extended) File Format observation source unit tests.
 */
public class AAVSOUploadFileFormatObservationSourceTest extends TestCase {

	private AAVSOUploadFileFormatObservationSource source;

	public AAVSOUploadFileFormatObservationSourceTest(String name) {
		super(name);
		source = new AAVSOUploadFileFormatObservationSource();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// Valid tests.

	// Visual format.
	// Test cases from http://www.aavso.org/aavso-visual-file-format

	public void testVisualExample2() {
		String[] lines = { "#TYPE=VISUAL\n", "#OBSCODE=TST01\n",
				"#SOFTWARE=WORD\n", "#DELIM=,\n", "#DATE=JD\n",
				"#NAME,DATE,MAG,COMMENTCODE,COMP1,COMP2,CHART,NOTES\n",
				"SS CYG,2450702.1234,<11.1,na,110,113,070613,This is a test\n" };

		List<ValidObservation> obs = commonTest(lines, "Visual example 2", 1, 1);

		ValidObservation ob = obs.get(0);
		assertEquals("SS CYG", ob.getName());
		assertEquals(2450702.1234, ob.getJD());
		assertEquals(11.1, ob.getMag());
		assertTrue(ob.getMagnitude().isFainterThan());
		assertEquals("110", ob.getCompStar1());
		assertEquals("113", ob.getCompStar2());
		assertEquals("070613", ob.getCharts());
	}

	// Extended format.
	// Test cases from http://www.aavso.org/aavso-extended-file-format

	public void testExtendedExample2a() {
		commonExtendedExample2Test(",");
	}

	public void testExtendedExample2b() {
		commonExtendedExample2Test("comma");
	}

	public void testWhitespace() {
		// Whitespace in DELIM directive and data fields.
		
		String[] lines = { "#TYPE=VISUAL\n", "#OBSCODE=TST01\n",
				"#SOFTWARE=WORD\n", "#DELIM = ,\n", "#DATE=JD\n",
				"#NAME,DATE,MAG,COMMENTCODE,COMP1,COMP2,CHART,NOTES\n",
				"SS CYG, 2450702.1234 , <11.1, na , 110 ,113, 070613, This is a test\n" };

		List<ValidObservation> obs = commonTest(lines, "Visual example 2 (WS)", 1, 1);

		ValidObservation ob = obs.get(0);
		assertEquals("SS CYG", ob.getName());
		assertEquals(2450702.1234, ob.getJD());
		assertEquals(11.1, ob.getMag());
		assertTrue(ob.getMagnitude().isFainterThan());
		assertEquals("110", ob.getCompStar1());
		assertEquals("113", ob.getCompStar2());
		assertEquals("070613", ob.getCharts());
	}

	// Helpers.

	private List<ValidObservation> commonTest(String[] lines, String inputName,
			int numObs, int numSeries) {
		StringBuffer content = new StringBuffer();
		for (String line : lines) {
			content.append(line);
		}

		InputStream in = new ByteArrayInputStream(content.toString().getBytes());
		List<InputStream> streams = new ArrayList<InputStream>();
		streams.add(in);
		source.setInputInfo(streams, inputName);

		AbstractObservationRetriever retriever = source
				.getObservationRetriever();
		try {
			retriever.retrieveObservations();
		} catch (Exception e) {
			fail();
		}

		List<ValidObservation> obs = retriever.getValidObservations();
		assertEquals(numObs, obs.size());

		return obs;
	}
	
	private void commonExtendedExample2Test(String delim) {
		String[] lines = {
				"#TYPE=EXTENDED\n",
				"#OBSCODE=TST01\n",
				"#SOFTWARE=GCX 2.0\n",
				"#DELIM="+delim+"\n",
				"#DATE=JD\n",
				"#OBSTYPE=CCD\n",
				"#NAME,DATE,MAG,MERR,FILT,TRANS,MTYPE,CNAME,CMAG,KNAME,KMAG,AMASS,GROUP,CHART,NOTES\n",
				"SS CYG,2450702.1234,11.235,0.003,B,NO,STD,105,10.593,110,11.090,1.561,1,070613,na\n",
				"SS CYG,2450702.1254,11.135,0.003,V,NO,STD,105,10.594,110,10.994,1.563,1,070613,na\n",
				"SS CYG,2450702.1274,11.035,0.003,R,NO,STD,105,10.594,110,10.896,1.564,1,070613,na\n",
				"SS CYG,2450702.1294,10.935,0.003,I,NO,STD,105,10.592,110,10.793,1.567,1,070613,na\n" };

		List<ValidObservation> obs = commonTest(lines, "Extended example 2", 4, 4);

		assertEquals(4, obs.size());

		// Check first and last observations.

		ValidObservation ob1 = obs.get(0);
		assertEquals("SS CYG", ob1.getName());
		assertEquals(2450702.1234, ob1.getJD());
		assertEquals(11.235, ob1.getMag());
		assertEquals(0.003, ob1.getMagnitude().getUncertainty());
		assertEquals(SeriesType.Johnson_B, ob1.getBand());
		assertFalse(ob1.isTransformed());
		assertEquals(MTypeType.STD, ob1.getMType());
		assertEquals("10.593", ob1.getCMag());
		assertEquals("11.09", ob1.getKMag());
		assertEquals("1.561", ob1.getAirmass());
		assertEquals("070613", ob1.getCharts());

		ValidObservation ob4 = obs.get(3);
		assertEquals("SS CYG", ob4.getName());
		assertEquals(2450702.1294, ob4.getJD());
		assertEquals(10.935, ob4.getMag());
		assertEquals(0.003, ob4.getMagnitude().getUncertainty());
		assertEquals(SeriesType.Cousins_I, ob4.getBand());
		assertFalse(ob4.isTransformed());
		assertEquals(MTypeType.STD, ob4.getMType());
		assertEquals("10.592", ob4.getCMag());
		assertEquals("10.793", ob4.getKMag());
		assertEquals("1.567", ob4.getAirmass());
		assertEquals("070613", ob4.getCharts());
	}
}
