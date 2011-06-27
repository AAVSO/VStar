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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisComponentFactory;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisDialogBase;
import org.aavso.tools.vstar.ui.NamedComponent;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodChangeMessage;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.aavso.tools.vstar.util.locale.NumberParser;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisAlgorithm;

/**
 * This class is used to visualise period analysis results.
 */
public class PeriodAnalysis2DResultDialog extends PeriodAnalysisDialogBase {

	private String seriesTitle;
	private String chartTitle;

	private List<PeriodAnalysis2DPlotModel> plotModels;
	private PeriodAnalysisDataTableModel dataTableModel;
	private PeriodAnalysisDataTableModel topHitsTableModel;

	private IPeriodAnalysisAlgorithm algorithm;

	private PeriodAnalysisTopHitsTablePane topHitsTablePane;

	private int selectedRow = -1;

	/**
	 * Constructor.
	 * 
	 * @param title
	 *            The title for the chart.
	 * @param subTitle
	 *            The source series sub-title for the chart.
	 * @param plotModels
	 *            The data plotModels on which to base plots.
	 * @param dataTableModel
	 *            A model with which to display all data in a table.
	 * @param algorithm
	 *            The period analysis algorithm.
	 */
	public PeriodAnalysis2DResultDialog(String title, String seriesTitle,
			List<PeriodAnalysis2DPlotModel> plotModels,
			PeriodAnalysisDataTableModel dataTableModel,
			PeriodAnalysisDataTableModel topHitsTableModel,
			IPeriodAnalysisAlgorithm algorithm) {
		super(title, false, true);

		this.seriesTitle = seriesTitle;
		this.chartTitle = title;
		this.plotModels = plotModels;
		this.dataTableModel = dataTableModel;
		this.topHitsTableModel = topHitsTableModel;
		this.algorithm = algorithm;
		
		Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
				.addListener(this.createPeriodAnalysisListener());

		prepareDialog();
	}

	protected Component createContent() {
		return createTabs();
	}

	// Return the tabs containing table and plots of frequency vs one of the
	// dependent variables of period, power, or amplitude. Is this what we want,
	// or something different?
	private JTabbedPane createTabs() {
		List<NamedComponent> namedComponents = new ArrayList<NamedComponent>();

		// Add plots.
		for (PeriodAnalysis2DPlotModel model : plotModels) {
			Component plot = PeriodAnalysisComponentFactory.createLinePlot(
					chartTitle, seriesTitle, model);

			String tabName = model.getRangeType() + " vs "
					+ model.getDomainType();

			namedComponents.add(new NamedComponent(tabName, plot));
		}

		// Add data table view.
		namedComponents.add(new NamedComponent("Data",
				new PeriodAnalysisDataTablePane(dataTableModel)));

		// Add top-hits table view.
		topHitsTablePane = new PeriodAnalysisTopHitsTablePane(
				topHitsTableModel, dataTableModel, algorithm);
		namedComponents.add(new NamedComponent("Top Hits", topHitsTablePane));

		return PluginComponentFactory.createTabs(namedComponents);
	}

	// The new phase plot button will only be enabled when a period
	// analysis selection message has been received by this class,
	// so we *know* without having to ask that there is a selected
	// row in the data table.
	protected void newPhasePlotButtonAction() {
		String periodStr = (String) dataTableModel
				.getPeriodValueInRow(selectedRow);
		double period = NumberParser.parseDouble(periodStr);

		PeriodChangeMessage message = new PeriodChangeMessage(this, period);
		Mediator.getInstance().getPeriodChangeNotifier()
				.notifyListeners(message);
	}

	// Enable the new phase plot button and store the selected
	// item number.
	//
	// There is a 1:1 correspondence between the item value
	// in the message and the selected data table row since
	// the item denotes an index into an observation sequence.
	private Listener<PeriodAnalysisSelectionMessage> createPeriodAnalysisListener() {
		return new Listener<PeriodAnalysisSelectionMessage>() {
			public void update(PeriodAnalysisSelectionMessage info) {
				setNewPhasePlotButtonState(true);
				selectedRow = info.getItem();
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}
}
