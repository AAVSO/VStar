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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

import junit.framework.TestCase;

/**
 * Unit tests for {@link PeriodAnalysisDataTableModel}.
 *
 * The model wraps period-analysis results (frequency, period, power,
 * semi-amplitude columns) in an {@code AbstractTableModel}. No display
 * or Mediator required — pure data logic.
 *
 * Part of issue #579 (GUI code coverage).
 */
public class PeriodAnalysisDataTableModelTest extends TestCase {

	private static final double FREQ = 0.123;
	private static final double PERIOD = 1.0 / FREQ;
	private static final double POWER = 42.7;
	private static final double SEMI_AMP = 0.35;

	private PeriodAnalysisCoordinateType[] columnTypes;
	private Map<PeriodAnalysisCoordinateType, List<Double>> data;
	private PeriodAnalysisDataTableModel model;

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);

		columnTypes = new PeriodAnalysisCoordinateType[] {
			PeriodAnalysisCoordinateType.FREQUENCY,
			PeriodAnalysisCoordinateType.PERIOD,
			PeriodAnalysisCoordinateType.POWER,
			PeriodAnalysisCoordinateType.SEMI_AMPLITUDE
		};

		data = new LinkedHashMap<>();
		data.put(PeriodAnalysisCoordinateType.FREQUENCY, Arrays.asList(FREQ, 0.456));
		data.put(PeriodAnalysisCoordinateType.PERIOD, Arrays.asList(PERIOD, 1.0 / 0.456));
		data.put(PeriodAnalysisCoordinateType.POWER, Arrays.asList(POWER, 10.0));
		data.put(PeriodAnalysisCoordinateType.SEMI_AMPLITUDE, Arrays.asList(SEMI_AMP, 0.12));

		model = new PeriodAnalysisDataTableModel(columnTypes, data);
	}

	public void testColumnCount() {
		assertEquals(4, model.getColumnCount());
	}

	public void testRowCount() {
		assertEquals(2, model.getRowCount());
	}

	public void testColumnNames() {
		assertEquals(PeriodAnalysisCoordinateType.FREQUENCY.getDescription(),
				model.getColumnName(0));
		assertEquals(PeriodAnalysisCoordinateType.PERIOD.getDescription(),
				model.getColumnName(1));
		assertEquals(PeriodAnalysisCoordinateType.POWER.getDescription(),
				model.getColumnName(2));
		assertEquals(PeriodAnalysisCoordinateType.SEMI_AMPLITUDE.getDescription(),
				model.getColumnName(3));
	}

	public void testColumnClassIsAlwaysString() {
		for (int col = 0; col < model.getColumnCount(); col++) {
			assertEquals(String.class, model.getColumnClass(col));
		}
	}

	public void testGetValueAtReturnsFormattedString() {
		String expected = NumericPrecisionPrefs.formatOther(FREQ);
		assertEquals(expected, model.getValueAt(0, 0));
	}

	public void testGetValueAtPowerColumn() {
		String expected = NumericPrecisionPrefs.formatOther(POWER);
		assertEquals(expected, model.getValueAt(0, 2));
	}

	public void testGetPeriodValueInRow() {
		String expected = NumericPrecisionPrefs.formatOther(PERIOD);
		assertEquals(expected, model.getPeriodValueInRow(0));
	}

	public void testGetFrequencyValueInRow() {
		assertEquals(FREQ, model.getFrequencyValueInRow(0), 1e-15);
	}

	public void testGetDataPointFromRow() {
		var dp = model.getDataPointFromRow(0);
		assertNotNull(dp);
		assertEquals(FREQ, dp.getFrequency(), 1e-15);
		assertEquals(PERIOD, dp.getPeriod(), 1e-15);
		assertEquals(POWER, dp.getPower(), 1e-15);
	}

	public void testGetData() {
		assertSame(data, model.getData());
	}

	public void testSetDataUpdatesModel() {
		Map<PeriodAnalysisCoordinateType, List<Double>> newData = new LinkedHashMap<>();
		newData.put(PeriodAnalysisCoordinateType.FREQUENCY, Arrays.asList(0.9));
		newData.put(PeriodAnalysisCoordinateType.PERIOD, Arrays.asList(1.0 / 0.9));
		newData.put(PeriodAnalysisCoordinateType.POWER, Arrays.asList(5.0));
		newData.put(PeriodAnalysisCoordinateType.SEMI_AMPLITUDE, Arrays.asList(0.05));

		model.setData(newData);

		assertEquals(1, model.getRowCount());
		assertSame(newData, model.getData());
	}
}
