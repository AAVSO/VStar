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
package org.aavso.tools.vstar.external.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;

/**
 * ASAS observation source unit tests.
 */
public class ASASObservationSourceTest extends TestCase {

	private ASASObservationSource source;

	public ASASObservationSourceTest(String name) {
		super(name);
		source = new ASASObservationSource();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// Valid tests.

	public void testEtaAql() throws FileNotFoundException {
		File file = new File("plugin/test/data/eta_aql_asas.txt");

		AbstractObservationRetriever retriever = commonTest(file,
				"eta_aql_asas.txt", 792, 1);

		assertTrue(retriever.isHeliocentric());
		
		Map<SeriesType, List<ValidObservation>> seriesMap = retriever
				.getValidObservationCategoryMap();
		
		List<ValidObservation> asas1Obs = seriesMap.get(SeriesType
				.getSeriesFromDescription("ASAS-1"));
		assertEquals(1, asas1Obs.size());

		List<ValidObservation> asas2Obs = seriesMap.get(SeriesType
				.getSeriesFromDescription("ASAS-2"));
		assertEquals(788, asas2Obs.size());

		List<ValidObservation> asas3Obs = seriesMap.get(SeriesType
				.getSeriesFromDescription("ASAS-3"));
		assertEquals(3, asas3Obs.size());

		ValidObservation ob = asas2Obs.get(0);
		assertEquals("195228+0100.5", ob.getDetail("DESIGNATION"));
		assertEquals("12341", ob.getDetail("FRAME"));
		assertEquals("B", ob.getDetail("CLASS"));
		assertEquals(2451979.899420, ob.getJD());
		assertEquals(5.113, ob.getMag());
		assertEquals(0.045, ob.getMagnitude().getUncertainty());
	}

	// Helpers.

	private AbstractObservationRetriever commonTest(File file,
			String inputName, int numObs, int numSeries)
			throws FileNotFoundException {

		InputStream in = new FileInputStream(file);
		List<InputStream> streams = new ArrayList<InputStream>();
		streams.add(in);
		source.setInputInfo(streams, inputName);

		AbstractObservationRetriever retriever = source
				.getObservationRetriever();
		try {
			retriever.retrieveObservations();
			assertEquals(numObs, retriever.getValidObservations().size());
		} catch (Exception e) {
			fail();
		}

		return retriever;
	}
}
