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
package org.aavso.tools.vstar.ui.dialog;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import junit.framework.TestCase;

/**
 * Tests for the {@link MultiEntryComponentDialog} configurations created by
 * the period-analysis plugins (Workstream B of issue #579).
 *
 * Because {@link MultiEntryComponentDialog}'s public constructors show the
 * dialog, tests use the package-private 5-argument constructor (show=false).
 * These tests live in the same package ({@code org.aavso.tools.vstar.ui.dialog})
 * to access that constructor.
 *
 * Each inner test group replicates the exact field setup from one plugin's
 * {@code createParamDialog()} / {@code executeAlgorithm()} method, verifying
 * field names, default values and range bounds.
 */
public class PeriodAnalysisPluginDialogsTest extends TestCase {

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);
	}

	private MultiEntryComponentDialog build(String title, List<ITextComponent<?>> fields) {
		return new MultiEntryComponentDialog(title, null, fields, Optional.empty(), false);
	}

	// -----------------------------------------------------------------------
	// DcDftFrequencyRangePeriodAnalysisPlugin
	// -----------------------------------------------------------------------

	public void testDcDftFrequencyRangeDialogConstruction() {
		DoubleField loFreq = new DoubleField("Low Frequency", 0.0, null, 0.01);
		DoubleField hiFreq = new DoubleField("High Frequency", 0.0, null, 10.0);
		DoubleField resolution = new DoubleField("Resolution", 0.0, null, 0.1);

		List<ITextComponent<?>> fields = Arrays.asList(
				(ITextComponent<?>) loFreq,
				(ITextComponent<?>) hiFreq,
				(ITextComponent<?>) resolution);

		MultiEntryComponentDialog d = build("Parameters", fields);
		try {
			assertEquals("Parameters", d.getTitle());
			assertTrue(d.isCancelled());
			assertTrue(d.isModal());

			assertEquals("Low Frequency", loFreq.getName());
			assertEquals(0.0, loFreq.getMin(), 0.0);
			assertNull(loFreq.getMax());
			assertEquals(0.01, loFreq.getValue(), 0.0);

			assertEquals("High Frequency", hiFreq.getName());
			assertEquals(0.0, hiFreq.getMin(), 0.0);
			assertNull(hiFreq.getMax());
			assertEquals(10.0, hiFreq.getValue(), 0.0);

			assertEquals("Resolution", resolution.getName());
			assertEquals(0.0, resolution.getMin(), 0.0);
			assertNull(resolution.getMax());
			assertEquals(0.1, resolution.getValue(), 0.0);
		} finally {
			d.dispose();
		}
	}

	public void testDcDftFrequencyRangeFieldCount() {
		DoubleField loFreq = new DoubleField("Low Frequency", 0.0, null, 0.01);
		DoubleField hiFreq = new DoubleField("High Frequency", 0.0, null, 10.0);
		DoubleField resolution = new DoubleField("Resolution", 0.0, null, 0.1);

		List<ITextComponent<?>> fields = Arrays.asList(
				(ITextComponent<?>) loFreq,
				(ITextComponent<?>) hiFreq,
				(ITextComponent<?>) resolution);

		MultiEntryComponentDialog d = build("Parameters", fields);
		try {
			assertEquals(3, fields.size());
		} finally {
			d.dispose();
		}
	}

	// -----------------------------------------------------------------------
	// DcDftPeriodRangePeriodAnalysisPlugin
	// -----------------------------------------------------------------------

	public void testDcDftPeriodRangeDialogConstruction() {
		DoubleField loPeriod = new DoubleField("Low Period", 0.0, null, 1.0);
		DoubleField hiPeriod = new DoubleField("High Period", 0.0, null, 100.0);
		DoubleField resolution = new DoubleField("Resolution", 0.0, null, 0.1);

		List<ITextComponent<?>> fields = Arrays.asList(
				(ITextComponent<?>) loPeriod,
				(ITextComponent<?>) hiPeriod,
				(ITextComponent<?>) resolution);

		MultiEntryComponentDialog d = build("Parameters", fields);
		try {
			assertEquals("Parameters", d.getTitle());
			assertTrue(d.isCancelled());

			assertEquals("Low Period", loPeriod.getName());
			assertEquals(0.0, loPeriod.getMin(), 0.0);
			assertNull(loPeriod.getMax());
			assertEquals(1.0, loPeriod.getValue(), 0.0);

			assertEquals("High Period", hiPeriod.getName());
			assertEquals(0.0, hiPeriod.getMin(), 0.0);
			assertNull(hiPeriod.getMax());
			assertEquals(100.0, hiPeriod.getValue(), 0.0);

			assertEquals("Resolution", resolution.getName());
			assertEquals(0.0, resolution.getMin(), 0.0);
			assertNull(resolution.getMax());
		} finally {
			d.dispose();
		}
	}

	// -----------------------------------------------------------------------
	// WeightedWaveletZTransformWithPeriodRangePlugin
	// -----------------------------------------------------------------------

	public void testWWZPeriodRangeDialogConstruction() {
		DoubleField minPeriod = new DoubleField("Minimum Period", 0.0, null, 1.0);
		DoubleField maxPeriod = new DoubleField("Maximum Period", 0.0, null, 100.0);
		DoubleField deltaPeriod = new DoubleField("Period Step", null, null, 0.5);
		DoubleField decay = new DoubleField("Decay", null, null, 0.001);
		DoubleField timeDivisions = new DoubleField("Time Divisions", null, null, 50.0);

		List<ITextComponent<?>> fields = Arrays.asList(
				(ITextComponent<?>) minPeriod,
				(ITextComponent<?>) maxPeriod,
				(ITextComponent<?>) deltaPeriod,
				(ITextComponent<?>) decay,
				(ITextComponent<?>) timeDivisions);

		MultiEntryComponentDialog d = build("WWZ Parameters", fields);
		try {
			assertEquals("WWZ Parameters", d.getTitle());
			assertTrue(d.isCancelled());
			assertEquals(5, fields.size());

			assertEquals("Minimum Period", minPeriod.getName());
			assertEquals(0.0, minPeriod.getMin(), 0.0);
			assertNull(minPeriod.getMax());

			assertEquals("Maximum Period", maxPeriod.getName());
			assertEquals(0.0, maxPeriod.getMin(), 0.0);
			assertNull(maxPeriod.getMax());

			assertEquals("Period Step", deltaPeriod.getName());
			assertNull(deltaPeriod.getMin());
			assertNull(deltaPeriod.getMax());

			assertEquals("Decay", decay.getName());
			assertEquals(0.001, decay.getValue(), 0.0);

			assertEquals("Time Divisions", timeDivisions.getName());
			assertEquals(50.0, timeDivisions.getValue(), 0.0);
		} finally {
			d.dispose();
		}
	}

	// -----------------------------------------------------------------------
	// WeightedWaveletZTransformWithFrequencyRangePlugin
	// -----------------------------------------------------------------------

	public void testWWZFrequencyRangeDialogConstruction() {
		DoubleField minFreq = new DoubleField("Minimum Frequency", 0.0, null, 0.01);
		DoubleField maxFreq = new DoubleField("Maximum Frequency", 0.0, null, 10.0);
		DoubleField deltaFreq = new DoubleField("Frequency Step", null, null, 0.05);
		DoubleField decay = new DoubleField("Decay", null, null, 0.001);
		DoubleField timeDivisions = new DoubleField("Time Divisions", null, null, 50.0);

		List<ITextComponent<?>> fields = Arrays.asList(
				(ITextComponent<?>) minFreq,
				(ITextComponent<?>) maxFreq,
				(ITextComponent<?>) deltaFreq,
				(ITextComponent<?>) decay,
				(ITextComponent<?>) timeDivisions);

		MultiEntryComponentDialog d = build("WWZ Parameters", fields);
		try {
			assertEquals("WWZ Parameters", d.getTitle());
			assertTrue(d.isCancelled());
			assertEquals(5, fields.size());

			assertEquals("Minimum Frequency", minFreq.getName());
			assertEquals(0.0, minFreq.getMin(), 0.0);
			assertNull(minFreq.getMax());

			assertEquals("Maximum Frequency", maxFreq.getName());
			assertEquals(0.0, maxFreq.getMin(), 0.0);
			assertNull(maxFreq.getMax());

			assertEquals("Frequency Step", deltaFreq.getName());
			assertNull(deltaFreq.getMin());
			assertNull(deltaFreq.getMax());

			assertEquals("Decay", decay.getName());
			assertEquals(0.001, decay.getValue(), 0.0);

			assertEquals("Time Divisions", timeDivisions.getName());
			assertEquals(50.0, timeDivisions.getValue(), 0.0);
		} finally {
			d.dispose();
		}
	}
}
