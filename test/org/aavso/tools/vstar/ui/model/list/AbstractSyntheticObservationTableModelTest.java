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
package org.aavso.tools.vstar.ui.model.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.util.stats.BinningResult;

import junit.framework.TestCase;

// Uses SeriesType.Visual as an arbitrary series for BinningResult construction.

/**
 * Tests for the four concrete subclasses of
 * {@link AbstractSyntheticObservationTableModel}:
 * <ul>
 *   <li>{@link RawDataMeanObservationTableModel}</li>
 *   <li>{@link PhasePlotMeanObservationTableModel}</li>
 *   <li>{@link RawDataModelObservationTableModel}</li>
 *   <li>{@link PhasePlotModelObservationTableModel}</li>
 * </ul>
 *
 * These exercise the abstract base-class machinery (observation storage,
 * row-index mapping, reverse lookup) through real production classes
 * rather than an artificial stub.
 *
 * Part of issue #579 (GUI code coverage).
 */
public class AbstractSyntheticObservationTableModelTest extends TestCase {

	private ValidObservation ob1;
	private ValidObservation ob2;
	private List<ValidObservation> observations;

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);

		ob1 = new ValidObservation();
		ob1.setDateInfo(new DateInfo(2451545.0));
		ob1.setMagnitude(new Magnitude(5.5, 0.01));
		ob1.setStandardPhase(0.25);

		ob2 = new ValidObservation();
		ob2.setDateInfo(new DateInfo(2451546.0));
		ob2.setMagnitude(new Magnitude(6.0, 0.02));
		ob2.setStandardPhase(0.75);

		observations = new ArrayList<>(Arrays.asList(ob1, ob2));
	}

	// -----------------------------------------------------------
	// RawDataMeanObservationTableModel
	// -----------------------------------------------------------

	public void testRawMeanColumnCount() {
		RawDataMeanObservationTableModel m =
				new RawDataMeanObservationTableModel(observations);
		assertEquals(4, m.getColumnCount());
	}

	public void testRawMeanRowCount() {
		RawDataMeanObservationTableModel m =
				new RawDataMeanObservationTableModel(observations);
		assertEquals(2, m.getRowCount());
	}

	public void testRawMeanColumnNames() {
		RawDataMeanObservationTableModel m =
				new RawDataMeanObservationTableModel(observations);
		assertEquals("Julian Day", m.getColumnName(0));
		assertEquals("Calendar Date", m.getColumnName(1));
		assertEquals("Mean Magnitude", m.getColumnName(2));
		assertEquals("Standard Error of the Average", m.getColumnName(3));
	}

	public void testRawMeanColumnClasses() {
		RawDataMeanObservationTableModel m =
				new RawDataMeanObservationTableModel(observations);
		for (int col = 0; col < 4; col++) {
			assertEquals(String.class, m.getColumnClass(col));
		}
	}

	public void testRawMeanGetValueAtJD() {
		RawDataMeanObservationTableModel m =
				new RawDataMeanObservationTableModel(observations);
		String expected = NumericPrecisionPrefs.formatTime(2451545.0);
		assertEquals(expected, m.getValueAt(0, 0));
	}

	public void testRawMeanGetValueAtMagnitude() {
		RawDataMeanObservationTableModel m =
				new RawDataMeanObservationTableModel(observations);
		String expected = NumericPrecisionPrefs.formatMag(5.5);
		assertEquals(expected, m.getValueAt(0, 2));
	}

	public void testRawMeanGetValueAtStdErr() {
		RawDataMeanObservationTableModel m =
				new RawDataMeanObservationTableModel(observations);
		String expected = NumericPrecisionPrefs.formatMag(0.01);
		assertEquals(expected, m.getValueAt(0, 3));
	}

	// -----------------------------------------------------------
	// PhasePlotMeanObservationTableModel
	// -----------------------------------------------------------

	public void testPhaseMeanColumnCount() {
		PhasePlotMeanObservationTableModel m =
				new PhasePlotMeanObservationTableModel(observations);
		assertEquals(3, m.getColumnCount());
	}

	public void testPhaseMeanRowCount() {
		PhasePlotMeanObservationTableModel m =
				new PhasePlotMeanObservationTableModel(observations);
		assertEquals(2, m.getRowCount());
	}

	public void testPhaseMeanColumnNames() {
		PhasePlotMeanObservationTableModel m =
				new PhasePlotMeanObservationTableModel(observations);
		assertEquals("Phase", m.getColumnName(0));
		assertEquals("Mean Magnitude", m.getColumnName(1));
		assertEquals("Standard Error of the Average", m.getColumnName(2));
	}

	public void testPhaseMeanGetValueAtPhase() {
		PhasePlotMeanObservationTableModel m =
				new PhasePlotMeanObservationTableModel(observations);
		String expected = NumericPrecisionPrefs.formatTime(0.25);
		assertEquals(expected, m.getValueAt(0, 0));
	}

	public void testPhaseMeanGetValueAtMag() {
		PhasePlotMeanObservationTableModel m =
				new PhasePlotMeanObservationTableModel(observations);
		String expected = NumericPrecisionPrefs.formatMag(5.5);
		assertEquals(expected, m.getValueAt(0, 1));
	}

	// -----------------------------------------------------------
	// RawDataModelObservationTableModel
	// -----------------------------------------------------------

	public void testRawModelColumnCount() {
		RawDataModelObservationTableModel m =
				new RawDataModelObservationTableModel(observations,
						SeriesType.Model);
		assertEquals(3, m.getColumnCount());
	}

	public void testRawModelRowCount() {
		RawDataModelObservationTableModel m =
				new RawDataModelObservationTableModel(observations,
						SeriesType.Model);
		assertEquals(2, m.getRowCount());
	}

	public void testRawModelColumnNames() {
		RawDataModelObservationTableModel m =
				new RawDataModelObservationTableModel(observations,
						SeriesType.Model);
		assertEquals("Julian Day", m.getColumnName(0));
		assertEquals("Calendar Date", m.getColumnName(1));
		assertEquals("Magnitude", m.getColumnName(2));
	}

	public void testRawModelGetValueAtJD() {
		RawDataModelObservationTableModel m =
				new RawDataModelObservationTableModel(observations,
						SeriesType.Model);
		String expected = NumericPrecisionPrefs.formatTime(2451545.0);
		assertEquals(expected, m.getValueAt(0, 0));
	}

	public void testRawModelGetValueAtMag() {
		RawDataModelObservationTableModel m =
				new RawDataModelObservationTableModel(observations,
						SeriesType.Model);
		String expected = NumericPrecisionPrefs.formatMag(5.5);
		assertEquals(expected, m.getValueAt(0, 2));
	}

	public void testRawModelWithResidualsSeries() {
		RawDataModelObservationTableModel m =
				new RawDataModelObservationTableModel(observations,
						SeriesType.Residuals);
		assertEquals(2, m.getRowCount());
	}

	// -----------------------------------------------------------
	// PhasePlotModelObservationTableModel
	// -----------------------------------------------------------

	public void testPhaseModelColumnCount() {
		PhasePlotModelObservationTableModel m =
				new PhasePlotModelObservationTableModel(observations,
						SeriesType.Model);
		assertEquals(2, m.getColumnCount());
	}

	public void testPhaseModelRowCountIsDoubled() {
		PhasePlotModelObservationTableModel m =
				new PhasePlotModelObservationTableModel(observations,
						SeriesType.Model);
		assertEquals(4, m.getRowCount());
	}

	public void testPhaseModelColumnNames() {
		PhasePlotModelObservationTableModel m =
				new PhasePlotModelObservationTableModel(observations,
						SeriesType.Model);
		assertEquals("Phase", m.getColumnName(0));
		assertEquals("Magnitude", m.getColumnName(1));
	}

	public void testPhaseModelGetValueAtPhase() {
		PhasePlotModelObservationTableModel m =
				new PhasePlotModelObservationTableModel(observations,
						SeriesType.Model);
		String expected = NumericPrecisionPrefs.formatTime(0.25);
		assertEquals(expected, m.getValueAt(0, 0));
	}

	public void testPhaseModelWrapsAroundForDoubledRows() {
		PhasePlotModelObservationTableModel m =
				new PhasePlotModelObservationTableModel(observations,
						SeriesType.Model);
		assertEquals(m.getValueAt(0, 0), m.getValueAt(2, 0));
		assertEquals(m.getValueAt(1, 0), m.getValueAt(3, 0));
	}

	// -----------------------------------------------------------
	// Base-class features via RawDataMeanObservationTableModel
	// -----------------------------------------------------------

	public void testGetObs() {
		RawDataMeanObservationTableModel m =
				new RawDataMeanObservationTableModel(observations);
		assertSame(ob1, m.getObs().get(0));
		assertSame(ob2, m.getObs().get(1));
	}

	public void testGetRowIndexFromObservation() {
		RawDataMeanObservationTableModel m =
				new RawDataMeanObservationTableModel(observations);
		assertEquals(Integer.valueOf(0), m.getRowIndexFromObservation(ob1));
		assertEquals(Integer.valueOf(1), m.getRowIndexFromObservation(ob2));
	}

	public void testGetRowIndexFromUnknownObservationReturnsNull() {
		RawDataMeanObservationTableModel m =
				new RawDataMeanObservationTableModel(observations);
		ValidObservation unknown = new ValidObservation();
		assertNull(m.getRowIndexFromObservation(unknown));
	}

	public void testEmptyModel() {
		RawDataMeanObservationTableModel m =
				new RawDataMeanObservationTableModel(new ArrayList<>());
		assertEquals(0, m.getRowCount());
		assertTrue(m.getObs().isEmpty());
	}

	public void testMeanModelUpdateViaListener() {
		RawDataMeanObservationTableModel m =
				new RawDataMeanObservationTableModel(observations);
		assertEquals(2, m.getRowCount());

		ValidObservation ob3 = new ValidObservation();
		ob3.setDateInfo(new DateInfo(2451547.0));
		ob3.setMagnitude(new Magnitude(7.0, 0.03));
		List<ValidObservation> newObs = Arrays.asList(ob3);

		BinningResult result = new BinningResult(
				SeriesType.Visual, 1, newObs, null);
		m.update(result);

		assertEquals(1, m.getRowCount());
		assertSame(ob3, m.getObs().get(0));
		assertEquals(Integer.valueOf(0), m.getRowIndexFromObservation(ob3));
	}

	public void testCanBeRemovedMean() {
		RawDataMeanObservationTableModel m =
				new RawDataMeanObservationTableModel(observations);
		assertTrue(m.canBeRemoved());
	}

	public void testCanBeRemovedModel() {
		RawDataModelObservationTableModel m =
				new RawDataModelObservationTableModel(observations,
						SeriesType.Model);
		assertTrue(m.canBeRemoved());
	}
}
