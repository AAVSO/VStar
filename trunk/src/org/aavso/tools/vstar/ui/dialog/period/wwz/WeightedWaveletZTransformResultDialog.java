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
package org.aavso.tools.vstar.ui.dialog.period.wwz;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisDialogBase;
import org.aavso.tools.vstar.ui.NamedComponent;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodChangeMessage;
import org.aavso.tools.vstar.ui.model.plot.WWZLinePlotModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPoint;
import org.aavso.tools.vstar.util.period.wwz.WWZCoordinateType;
import org.aavso.tools.vstar.util.period.wwz.WWZStatistic;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

/**
 * This dialog class is used to visualise WWZ algorithm results.
 */
public class WeightedWaveletZTransformResultDialog extends
		PeriodAnalysisDialogBase {

	private String chartTitle;
	private PeriodAnalysisDataPoint selectedDataPoint;
	private List<WWZStatistic> stats;

	/**
	 * Constructor.
	 * 
	 * @param title
	 *            The title for the dialog.
	 * @param chartTitle
	 *            The title for the chart.
	 */
	public WeightedWaveletZTransformResultDialog(String title,
			String chartTitle, List<WWZStatistic> stats) {
		super(title, false, true);

		this.chartTitle = chartTitle;
		this.stats = stats;

		selectedDataPoint = null;

		Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
				.addListener(this.createPeriodAnalysisListener());

		prepareDialog();
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.period.PeriodAnalysisDialogBase#createContent()
	 */
	@Override
	protected Component createContent() {
		return createTabs();
	}

	private JTabbedPane createTabs() {
		List<NamedComponent> namedComponents = new ArrayList<NamedComponent>();

		// Period vs time plot.
		namedComponents.add(createChart(new WWZLinePlotModel(stats,
				WWZCoordinateType.TAU, WWZCoordinateType.PERIOD)));

		// Semi-amplitude vs time plot.
		namedComponents.add(createChart(new WWZLinePlotModel(stats,
				WWZCoordinateType.TAU, WWZCoordinateType.SEMI_AMPLITUDE)));

		// TODO: tables x 2
		
		// TODO: note that we could extract a best fit sinusoid (i.e. model)
		// from
		// mave for some tau/frequency combination; residuals could be created
		// for
		// those datapoints from the observed data.
		// => make this available via a Create Model button on the data tabs?
		// (should that button be pushed down to the base dialog class rather
		// than being embedded in a table pane?)

		return PluginComponentFactory.createTabs(namedComponents);
	}

	/**
	 * The new phase plot button will only be enabled when a period analysis
	 * selection message has been received by this class, so we *know* without
	 * having to ask that there is a selected row in the data table.
	 */
	@Override
	protected void newPhasePlotButtonAction() {
		PeriodChangeMessage message = new PeriodChangeMessage(this,
				selectedDataPoint.getPeriod());
		Mediator.getInstance().getPeriodChangeNotifier().notifyListeners(
				message);
	}

	// Enable the new phase plot button and store the selected
	// period analysis data point.
	private Listener<PeriodAnalysisSelectionMessage> createPeriodAnalysisListener() {
		return new Listener<PeriodAnalysisSelectionMessage>() {
			public void update(PeriodAnalysisSelectionMessage info) {
				setNewPhasePlotButtonState(true);
				selectedDataPoint = info.getDataPoint();
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	// Helpers

	private NamedComponent createChart(WWZLinePlotModel model) {
		JFreeChart chart = ChartFactory.createXYLineChart(chartTitle, model
				.getDomainType().toString(), model.getRangeType().toString(),
				model, PlotOrientation.VERTICAL, true, true, false);

		String tabName = model.getRangeType().toString() + " vs "
				+ model.getDomainType().toString();

		return new NamedComponent(tabName, new WWZLinePlotPane(chart, model));
	}
}
