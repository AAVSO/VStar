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

import org.aavso.tools.vstar.util.period.wwz.WWZCoordinateType;
import org.aavso.tools.vstar.util.period.wwz.WWZStatistic;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

import junit.framework.TestCase;

/**
 * Unit tests for {@link WWZDataTableModel}.
 *
 * The model wraps WWZ time-frequency analysis results in an
 * {@code AbstractTableModel}. No display or Mediator required.
 *
 * Part of issue #579 (GUI code coverage).
 */
public class WWZDataTableModelTest extends TestCase {

	private static final double TAU = 2451545.0;
	private static final double FREQ = 0.25;
	private static final double WWZ_VAL = 8.3;
	private static final double AMP = 0.45;
	private static final double MAVE = 10.2;
	private static final double NEFF = 42.0;

	private WWZStatistic stat1;
	private WWZStatistic stat2;
	private List<WWZStatistic> stats;
	private WWZDataTableModel model;

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);

		stat1 = new WWZStatistic(TAU, FREQ, WWZ_VAL, AMP, MAVE, NEFF);
		stat2 = new WWZStatistic(TAU + 1, 0.5, 3.1, 0.2, 11.0, 30.0);
		stats = new ArrayList<>(Arrays.asList(stat1, stat2));

		model = new WWZDataTableModel(stats, null);
	}

	public void testColumnCount() {
		assertEquals(WWZCoordinateType.values().length, model.getColumnCount());
	}

	public void testRowCount() {
		assertEquals(2, model.getRowCount());
	}

	public void testColumnNames() {
		assertEquals(WWZCoordinateType.TAU.toString(), model.getColumnName(0));
		assertEquals(WWZCoordinateType.FREQUENCY.toString(), model.getColumnName(1));
		assertEquals(WWZCoordinateType.PERIOD.toString(), model.getColumnName(2));
		assertEquals(WWZCoordinateType.WWZ.toString(), model.getColumnName(3));
		assertEquals(WWZCoordinateType.SEMI_AMPLITUDE.toString(), model.getColumnName(4));
		assertEquals(WWZCoordinateType.MEAN_MAG.toString(), model.getColumnName(5));
		assertEquals(WWZCoordinateType.EFFECTIVE_NUM_DATA.toString(), model.getColumnName(6));
	}

	public void testColumnClassIsAlwaysString() {
		for (int col = 0; col < model.getColumnCount(); col++) {
			assertEquals(String.class, model.getColumnClass(col));
		}
	}

	public void testGetValueAtTauColumn() {
		String expected = NumericPrecisionPrefs.formatOther(TAU);
		assertEquals(expected, model.getValueAt(0, 0));
	}

	public void testGetValueAtFrequencyColumn() {
		String expected = NumericPrecisionPrefs.formatOther(FREQ);
		assertEquals(expected, model.getValueAt(0, 1));
	}

	public void testGetValueAtPeriodColumn() {
		String expected = NumericPrecisionPrefs.formatOther(1.0 / FREQ);
		assertEquals(expected, model.getValueAt(0, 2));
	}

	public void testGetValueAtWWZColumn() {
		String expected = NumericPrecisionPrefs.formatOther(WWZ_VAL);
		assertEquals(expected, model.getValueAt(0, 3));
	}

	public void testGetValueAtSemiAmplitudeColumn() {
		String expected = NumericPrecisionPrefs.formatOther(AMP);
		assertEquals(expected, model.getValueAt(0, 4));
	}

	public void testGetDataPointFromRow() {
		WWZStatistic dp = model.getDataPointFromRow(0);
		assertSame(stat1, dp);
	}

	public void testGetDataPointFromSecondRow() {
		WWZStatistic dp = model.getDataPointFromRow(1);
		assertSame(stat2, dp);
	}

	public void testGetStats() {
		assertSame(stats, model.getStats());
	}

	public void testEmptyModel() {
		WWZDataTableModel empty = new WWZDataTableModel(
				new ArrayList<>(), null);
		assertEquals(0, empty.getRowCount());
		assertEquals(WWZCoordinateType.values().length, empty.getColumnCount());
	}
}
