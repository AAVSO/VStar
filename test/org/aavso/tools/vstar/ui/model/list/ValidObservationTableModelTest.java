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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

import junit.framework.TestCase;

/**
 * Pure unit test for {@link ValidObservationTableModel}.
 *
 * The model extends {@code AbstractTableModel} (javax.swing) but its
 * data-access methods are plain logic that needs no display, so we
 * exercise them as a regular JUnit 3 test (no AssertJ Swing needed).
 *
 * Part of issue #579 (prong C): GUI code coverage.
 */
public class ValidObservationTableModelTest extends TestCase {

	private static final double JD = 2451545.0;
	private static final double MAG_VALUE = 5.5;
	private static final double UNCERTAINTY = 0.01;
	private static final String OBS_CODE = "TST";
	private static final int RECORD_NUM = 1;

	private ValidObservation ob1;
	private ValidObservation ob2;
	private List<ValidObservation> observations;
	private ITableColumnInfoSource columnInfoSource;
	private ValidObservationTableModel model;

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);

		ob1 = new ValidObservation();
		ob1.setJD(JD);
		ob1.setMagnitude(new Magnitude(MAG_VALUE, UNCERTAINTY));
		ob1.setObsCode(OBS_CODE);
		ob1.setRecordNumber(RECORD_NUM);
		ob1.setBand(SeriesType.Visual);

		ob2 = new ValidObservation();
		ob2.setJD(JD + 1.0);
		ob2.setMagnitude(new Magnitude(6.0, 0.02));
		ob2.setObsCode("OBS");
		ob2.setRecordNumber(2);
		ob2.setBand(SeriesType.Visual);

		observations = new ArrayList<ValidObservation>(Arrays.asList(ob1, ob2));

		Map<SeriesType, List<ValidObservation>> obsSourceListMap =
				new HashMap<SeriesType, List<ValidObservation>>();
		obsSourceListMap.put(SeriesType.Visual, observations);

		columnInfoSource = new SimpleFormatRawDataColumnInfoSource();
		model = new ValidObservationTableModel(obsSourceListMap, observations, columnInfoSource);
	}

	public void testColumnCount() {
		assertEquals(7, model.getColumnCount());
	}

	public void testColumnNames() {
		assertEquals("Time", model.getColumnName(0));
		assertEquals("Calendar Date", model.getColumnName(1));
		assertEquals("Magnitude", model.getColumnName(2));
		assertEquals("Uncertainty", model.getColumnName(3));
		assertEquals("Observer Code", model.getColumnName(4));
		assertEquals("Line", model.getColumnName(5));
		assertEquals("Discrepant?", model.getColumnName(6));
	}

	public void testRowCount() {
		assertEquals(2, model.getRowCount());
	}

	public void testRowCountWithEmptyList() {
		ValidObservationTableModel empty = new ValidObservationTableModel(
				new HashMap<SeriesType, List<ValidObservation>>(),
				new ArrayList<ValidObservation>(),
				columnInfoSource);
		assertEquals(0, empty.getRowCount());
	}

	public void testGetValueAtTimeColumn() {
		String expected = NumericPrecisionPrefs.formatTime(JD);
		assertEquals(expected, model.getValueAt(0, 0));
	}

	public void testGetValueAtMagnitudeColumn() {
		String expected = NumericPrecisionPrefs.formatMag(MAG_VALUE);
		assertEquals(expected, model.getValueAt(0, 2));
	}

	public void testGetValueAtUncertaintyColumn() {
		String expected = NumericPrecisionPrefs.formatMag(UNCERTAINTY);
		assertEquals(expected, model.getValueAt(0, 3));
	}

	public void testGetValueAtObserverCodeColumn() {
		assertEquals(OBS_CODE, model.getValueAt(0, 4));
	}

	public void testGetValueAtLineColumn() {
		assertEquals(RECORD_NUM, model.getValueAt(0, 5));
	}

	public void testGetValueAtDiscrepantColumn() {
		assertEquals(Boolean.FALSE, model.getValueAt(0, 6));
	}

	public void testGetRowIndexFromObservation() {
		assertEquals(Integer.valueOf(0), model.getRowIndexFromObservation(ob1));
		assertEquals(Integer.valueOf(1), model.getRowIndexFromObservation(ob2));
	}

	public void testGetRowIndexFromUnknownObservationReturnsNull() {
		ValidObservation unknown = new ValidObservation();
		assertNull(model.getRowIndexFromObservation(unknown));
	}

	public void testIsCellEditableOnlyForDiscrepantColumn() {
		for (int col = 0; col < model.getColumnCount(); col++) {
			if (col == 6) {
				assertTrue("Discrepant column should be editable",
						model.isCellEditable(0, col));
			} else {
				assertFalse("Column " + col + " should not be editable",
						model.isCellEditable(0, col));
			}
		}
	}

	public void testGetColumnClass() {
		assertEquals(String.class, model.getColumnClass(0));
		assertEquals(String.class, model.getColumnClass(1));
		assertEquals(String.class, model.getColumnClass(2));
		assertEquals(String.class, model.getColumnClass(3));
		assertEquals(String.class, model.getColumnClass(4));
		assertEquals(Integer.class, model.getColumnClass(5));
		assertEquals(Boolean.class, model.getColumnClass(6));
	}

	public void testGetObservations() {
		List<ValidObservation> result = model.getObservations();
		assertNotNull(result);
		assertEquals(2, result.size());
		assertTrue(result.contains(ob1));
		assertTrue(result.contains(ob2));
	}

	public void testGetColumnInfoSource() {
		assertNotNull(model.getColumnInfoSource());
	}

	public void testGetObsInserter() {
		assertNotNull(model.getObsInserter());
	}
}
