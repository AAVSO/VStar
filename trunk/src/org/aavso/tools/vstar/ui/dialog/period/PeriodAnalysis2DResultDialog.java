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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.PeriodChangeMessage;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisTopHitsTableModel;
import org.aavso.tools.vstar.ui.model.plot.PeriodAnalysis2DPlotModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;

/**
 * This class is used to visualise period analysis results.
 */
public class PeriodAnalysis2DResultDialog extends JDialog {

	private String seriesTitle;
	private String chartTitle;

	private List<PeriodAnalysis2DPlotModel> plotModels;
	private PeriodAnalysisDataTableModel dataTableModel;
	private PeriodAnalysisTopHitsTableModel topHitsTableModel;

	private int selectedRow = -1;

	private JButton newPhasePlotButton;

	/**
	 * Constructor
	 * 
	 * @param title
	 *            The title for the chart.
	 * @param subTitle
	 *            The source series sub-title for the chart.
	 * @param plotModels
	 *            The data plotModels on which to base plots.
	 * @param dataTableModel
	 *            A model with which to display all data in a table.
	 */
	public PeriodAnalysis2DResultDialog(String title, String seriesTitle,
			List<PeriodAnalysis2DPlotModel> plotModels,
			PeriodAnalysisDataTableModel dataTableModel,
			PeriodAnalysisTopHitsTableModel topHitsTableModel) {
		super();

		this.setTitle(title);
		this.setModal(false);

		this.seriesTitle = seriesTitle;
		this.chartTitle = title;
		this.plotModels = plotModels;
		this.dataTableModel = dataTableModel;
		this.topHitsTableModel = topHitsTableModel;

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createTabs());
		topPane.add(createButtonPanel());

		Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
				.addListener(this.createPeriodAnalysisListener());

		this.getContentPane().add(topPane);

		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setAlwaysOnTop(false);
		this.setVisible(true);
	}

	// Return the tabs containing table and plots of frequency vs one of the
	// dependent variables of period, power, or amplitude. Is this what we want,
	// or something different?
	private JTabbedPane createTabs() {
		JTabbedPane tabs = new JTabbedPane();

		// Add plots.
		for (PeriodAnalysis2DPlotModel model : plotModels) {
			// Create a line chart with legend, tool-tips, and URLs showing
			// and add it to the panel.
			ChartPanel chartPanel = new PeriodAnalysis2DChartPane(ChartFactory
					.createXYLineChart(this.chartTitle, model.getDomainType()
							.getDescription(), model.getRangeType()
							.getDescription(), model, PlotOrientation.VERTICAL,
							true, true, true), model);

			chartPanel.getChart().addSubtitle(new TextTitle(this.seriesTitle));

			String tabName = model.getRangeType() + " vs "
					+ model.getDomainType();
			tabs.addTab(tabName, chartPanel);
		}

		// Add data table view.
		tabs.addTab("Data", new PeriodAnalysisDataTablePane(dataTableModel));

		// Add top-hits table view.
		tabs.addTab("Top Hits", new PeriodAnalysisTopHitsTablePane(
				topHitsTableModel));

		return tabs;
	}

	private JPanel createButtonPanel() {
		JPanel buttonPane = new JPanel();

		newPhasePlotButton = new JButton("New Phase Plot");
		newPhasePlotButton.addActionListener(createNewPhasePlotButtonHandler());
		buttonPane.add(newPhasePlotButton, BorderLayout.LINE_START);
		newPhasePlotButton.setEnabled(false);

		JButton dismissButton = new JButton("Dismiss");
		dismissButton.addActionListener(createDismissButtonHandler());
		buttonPane.add(dismissButton, BorderLayout.LINE_END);

		return buttonPane;
	}

	// Dismiss button listener.

	private ActionListener createDismissButtonHandler() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		};
	}

	// New phase plot button listener.
	//
	// This button will only be enabled when a period analysis
	// selection message has been received by this class, so we
	// *know* without having to ask that there is a selected row
	// in the data table.
	private ActionListener createNewPhasePlotButtonHandler() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String periodStr = (String) dataTableModel.getValueAt(
						selectedRow, PeriodAnalysisCoordinateType.PERIOD
								.getIndex());

				double period = Double.valueOf(periodStr);

				PeriodChangeMessage message = new PeriodChangeMessage(this,
						period);
				Mediator.getInstance().getPeriodChangeMessageNotifier()
						.notifyListeners(message);
			}
		};
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
				newPhasePlotButton.setEnabled(true);
				selectedRow = info.getItem();
			}

			public boolean canBeRemoved() {
				return false;
			}
		};
	}
}
