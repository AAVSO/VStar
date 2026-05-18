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
package org.aavso.tools.vstar.ui.model.plot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.NewStarType;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.util.comparator.JDComparator;

/**
 * Unit tests for {@link ObservationAndMeanPlotModel} (which is the concrete
 * subclass of the abstract {@link ObservationPlotModel}).
 *
 * Construction requires a live {@link Mediator} singleton with at least one
 * {@link NewStarMessage} registered, because
 * {@link ObservationPlotModel#isSeriesVisibleByDefault} calls
 * {@code Mediator.getInstance().getLatestNewStarMessage()}. We satisfy this by
 * directly adding a minimal message to the Mediator's list before each test and
 * removing it afterwards.
 *
 * Part of issue #579 (GUI code coverage, prong D).
 */
public class ObservationAndMeanPlotModelTest extends TestCase {

	private static final double JD1 = 2451545.0;
	private static final double JD2 = 2451565.0;
	private static final double JD3 = 2451585.0;
	private static final double MAG  = 5.5;
	private static final double ERR  = 0.05;

	private ObservationAndMeanPlotModel model;
	private List<ValidObservation>       obsList;

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);

		// Build a few minimal observations.
		obsList = new ArrayList<ValidObservation>();
		for (double jd : new double[]{JD1, JD2, JD3}) {
			ValidObservation ob = new ValidObservation();
			ob.setJD(jd);
			ob.setMagnitude(new Magnitude(MAG, ERR));
			ob.setBand(SeriesType.Unspecified);
			obsList.add(ob);
		}

		// Register a minimal NewStarMessage with Mediator so that
		// isSeriesVisibleByDefault() does not throw a NullPointerException.
		StarInfo starInfo = new StarInfo("Test Star", "TST0001");
		Map<SeriesType, List<ValidObservation>> categoryMap =
				new HashMap<SeriesType, List<ValidObservation>>();
		categoryMap.put(SeriesType.Unspecified, obsList);

		NewStarMessage msg = new NewStarMessage(
				NewStarType.NEW_STAR_FROM_DATABASE,
				starInfo, obsList, categoryMap,
				MAG - 1.0, MAG + 1.0, null);

		Mediator.getInstance().getNewStarMessageList().add(msg);

		// Build the obs-source map and construct the model.
		Map<SeriesType, List<ValidObservation>> obsSourceListMap =
				new HashMap<SeriesType, List<ValidObservation>>();
		obsSourceListMap.put(SeriesType.Unspecified, obsList);

		model = new ObservationAndMeanPlotModel(
				obsSourceListMap,
				JDCoordSource.instance,
				new JDComparator(),
				JDTimeElementEntity.instance,
				null);
	}

	@Override
	protected void tearDown() {
		// Remove the message we added so the singleton stays clean for other tests.
		Mediator.getInstance().getNewStarMessageList().clear();
	}

	// --- Series structure ---

	public void testSeriesCountIncludesUnspecifiedAndMeans() {
		// At minimum: Unspecified + MEANS series.
		assertTrue("Expected at least 2 series; got " + model.getSeriesCount(),
				model.getSeriesCount() >= 2);
	}

	public void testUnspecifiedSeriesExists() {
		assertTrue(model.seriesExists(SeriesType.Unspecified));
	}

	public void testMeansSeriesExists() {
		assertTrue(model.seriesExists(SeriesType.MEANS));
	}

	// --- Item count ---

	public void testItemCountInUnspecifiedSeries() {
		int seriesNum = model.getSrcTypeToSeriesNumMap().get(SeriesType.Unspecified);
		assertEquals(obsList.size(), model.getItemCount(seriesNum));
	}

	// --- Coordinate retrieval ---

	public void testGetXReturnsJD() {
		int seriesNum = model.getSrcTypeToSeriesNumMap().get(SeriesType.Unspecified);
		// Observations are sorted by JD; item 0 should be JD1.
		Number x = model.getX(seriesNum, 0);
		assertNotNull(x);
		assertEquals(JD1, x.doubleValue(), 1e-6);
	}

	public void testGetYReturnsMagnitude() {
		int seriesNum = model.getSrcTypeToSeriesNumMap().get(SeriesType.Unspecified);
		Number y = model.getY(seriesNum, 0);
		assertNotNull(y);
		assertEquals(MAG, y.doubleValue(), 1e-6);
	}

	// --- ValidObservation retrieval ---

	public void testGetValidObservationReturnsCorrectOb() {
		int seriesNum = model.getSrcTypeToSeriesNumMap().get(SeriesType.Unspecified);
		ValidObservation ob = model.getValidObservation(seriesNum, 0);
		assertNotNull(ob);
		assertEquals(JD1, ob.getJD(), 1e-6);
	}

	// --- Error / uncertainty ---

	public void testGetMagErrorNonNegative() {
		int seriesNum = model.getSrcTypeToSeriesNumMap().get(SeriesType.Unspecified);
		double err = model.getMagError(seriesNum, 0);
		assertTrue("Error should be non-negative", err >= 0.0);
	}

	// --- Mean series number ---

	public void testMeansSeriesNumNotNegative() {
		assertTrue("meansSeriesNum should be >= 0 after construction",
				model.getMeansSeriesNum() >= 0);
	}

	// --- Time element entity ---

	public void testTimeElementEntityIsJD() {
		assertNotNull(model.getTimeElementEntity());
		assertTrue(model.getTimeElementEntity() instanceof JDTimeElementEntity);
	}
}
