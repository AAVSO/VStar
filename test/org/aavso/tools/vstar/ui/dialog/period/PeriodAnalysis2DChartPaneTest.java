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
package org.aavso.tools.vstar.ui.dialog.period;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

/**
 * Tests for {@link PeriodAnalysis2DChartPane}.
 *
 * The chart pane's constructor does not call {@code Mediator} (only
 * {@link PeriodAnalysis2DChartPane#startup()} and
 * {@link PeriodAnalysis2DChartPane#cleanup()} do), so it can be constructed
 * and inspected in isolation.
 *
 * Note: {@code WeightedWaveletZTransformResultDialog} and
 * {@code PeriodAnalysis2DResultDialog} both call
 * {@code PeriodAnalysisDialogBase#prepareDialog()} in their constructors, which
 * in turn calls {@code Mediator.getUI().getContentPane()}.  Applying a
 * show=false pattern to those dialogs would require changes to the base class
 * and each subclass's {@code startup()} call; the chart pane offers the same
 * coverage at much lower scaffolding cost.
 *
 * Part of issue #579 (GUI code coverage, prong D).
 */
public class PeriodAnalysis2DChartPaneTest extends TestCase {

	private PeriodAnalysis2DPlotModel plotModel;
	private JFreeChart chart;
	private PeriodAnalysis2DChartPane pane;

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);

		// Minimal data: two frequency/power pairs.
		List<Double> frequencies = Arrays.asList(1.0, 2.0, 3.0);
		List<Double> powers      = Arrays.asList(0.3, 0.8, 0.5);

		Map<PeriodAnalysisCoordinateType, List<Double>> data =
				new HashMap<PeriodAnalysisCoordinateType, List<Double>>();
		data.put(PeriodAnalysisCoordinateType.FREQUENCY, frequencies);
		data.put(PeriodAnalysisCoordinateType.POWER, powers);

		plotModel = new PeriodAnalysis2DPlotModel(
				data,
				PeriodAnalysisCoordinateType.FREQUENCY,
				PeriodAnalysisCoordinateType.POWER,
				false);

		chart = ChartFactory.createXYLineChart(
				"Test Chart", "Frequency", "Power",
				plotModel, PlotOrientation.VERTICAL,
				true, true, false);

		pane = new PeriodAnalysis2DChartPane(chart, plotModel, true);
	}

	// --- Basic construction and accessors ---

	public void testGetChartReturnsNonNull() {
		assertNotNull(pane.getChart());
	}

	public void testGetChartReturnsSameChart() {
		assertSame(chart, pane.getChart());
	}

	public void testGetModelReturnsNonNull() {
		assertNotNull(pane.getModel());
	}

	public void testGetModelReturnsSameModel() {
		assertSame(plotModel, pane.getModel());
	}

	// --- Chart pane ID ---

	public void testChartPaneIDInitiallyNull() {
		assertNull(pane.getChartPaneID());
	}

	public void testSetChartPaneID() {
		pane.setChartPaneID("PlotPane0");
		assertEquals("PlotPane0", pane.getChartPaneID());
	}

	// --- Underlying plot model data ---

	public void testPlotModelSeriesCount() {
		assertEquals(1, plotModel.getSeriesCount());
	}

	public void testPlotModelItemCount() {
		assertEquals(3, plotModel.getItemCount(0));
	}

	public void testPlotModelDomainType() {
		assertEquals(PeriodAnalysisCoordinateType.FREQUENCY,
				plotModel.getDomainType());
	}

	public void testPlotModelRangeType() {
		assertEquals(PeriodAnalysisCoordinateType.POWER,
				plotModel.getRangeType());
	}

	public void testPlotModelNotLogarithmicByDefault() {
		assertFalse(plotModel.isLogarithmic());
	}

	public void testPlotModelSetLogarithmic() {
		plotModel.setLogarithmic(true);
		assertTrue(plotModel.isLogarithmic());
		plotModel.setLogarithmic(false);
	}

	public void testPlotModelXValues() {
		assertEquals(1.0, plotModel.getX(0, 0).doubleValue(), 1e-9);
		assertEquals(2.0, plotModel.getX(0, 1).doubleValue(), 1e-9);
		assertEquals(3.0, plotModel.getX(0, 2).doubleValue(), 1e-9);
	}

	public void testPlotModelYValues() {
		assertEquals(0.3, plotModel.getY(0, 0).doubleValue(), 1e-9);
		assertEquals(0.8, plotModel.getY(0, 1).doubleValue(), 1e-9);
		assertEquals(0.5, plotModel.getY(0, 2).doubleValue(), 1e-9);
	}
}
