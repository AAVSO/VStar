/**
 * VStar: a statistical analysOis tool for variable star data.
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
package org.aavso.tools.vstar.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.NamedComponent;
import org.aavso.tools.vstar.ui.dialog.series.MeanSourcePane;
import org.aavso.tools.vstar.ui.dialog.series.SeriesVisibilityPane;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.ui.pane.plot.ObservationAndMeanPlotPane;
import org.aavso.tools.vstar.ui.pane.plot.TimeElementsInBinSettingPane;
import org.jfree.chart.JFreeChart;

/**
 * A dialog that controls the features of plots.
 */
public class AbstractPlotControlDialog extends JDialog {

	// Series-related panes. 
	protected SeriesVisibilityPane seriesVisibilityPane;
	protected MeanSourcePane meanSourcePane;
	
	// Show error bars?
	protected boolean showErrorBars;
	protected JCheckBox errorBarCheckBox;

	// Show cross-hairs?
	protected boolean showCrossHairs;
	protected JCheckBox crossHairCheckBox;

	// Should the means series elements be joined visually?
	protected boolean joinMeans;
	protected JCheckBox joinMeansCheckBox;

	protected JButton dismissButton;

	protected TimeElementsInBinSettingPane timeElementsInBinSettingPane;

	// Taken from each loaded dataset.
	protected ObservationAndMeanPlotPane plotPane;
	protected ObservationAndMeanPlotModel obsModel;
	protected NamedComponent extra;

	/**
	 * Constructor.
	 * 
	 * @param title
	 *            The dialog title.
	 * @param plotPane
	 *            The plot pane.
	 * @param timeElementsInBinSettingPane
	 *            The component that captures time elements in bin setting (for
	 *            raw or phase plots).
	 * @param extra
	 *            Additional component to be added.
	 */
	public AbstractPlotControlDialog(String title,
			ObservationAndMeanPlotPane plotPane,
			TimeElementsInBinSettingPane timeElementsInBinSettingPane,
			NamedComponent extra) {
		setTitle(title);
		setModal(true);

		this.plotPane = plotPane;
		this.obsModel = plotPane.getObsModel();
		this.extra = extra;

		showErrorBars = true;
		showCrossHairs = true;
		joinMeans = true;

		this.timeElementsInBinSettingPane = timeElementsInBinSettingPane;

		createContent();
	}

	/**
	 * Constructor.
	 * 
	 * @param title
	 *            The dialog title.
	 * @param plotPane
	 *            The plot pane.
	 * @param timeElementsInBinSettingPane
	 *            The component that captures time elements in bin setting (for
	 *            raw or phase plots).
	 */
	public AbstractPlotControlDialog(String title,
			ObservationAndMeanPlotPane plotPane,
			TimeElementsInBinSettingPane timeElementsInBinSettingPane) {
		this(title, plotPane, timeElementsInBinSettingPane, null);
	}

	/**
	 * Close the dialog.
	 */
	public void close() {
		setVisible(false);
		dispose();
	}

	/**
	 * Create the dialog content.
	 */
	protected void createContent() {
		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createChartControlPanel(extra));
		topPane.add(Box.createRigidArea(new Dimension(75, 10)));

		topPane.add(createButtonPane());

		contentPane.add(topPane);
		this.pack();

		setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.getRootPane().setDefaultButton(dismissButton);
	}

	/**
	 * Populate a panel that can contains chart control widgets.
	 * 
	 * @param extra
	 *            Additional component to be added.
	 * @return The chart control panel to be added.
	 */
	protected JPanel createChartControlPanel(NamedComponent extra) {
		JPanel chartControlPanel = new JPanel();
		chartControlPanel.setLayout(new BoxLayout(chartControlPanel,
				BoxLayout.PAGE_AXIS));
		chartControlPanel.setBorder(BorderFactory.createEtchedBorder());

		chartControlPanel.add(createSeriesChangePane());
		
		JPanel showCheckBoxPanel = new JPanel();
		showCheckBoxPanel.setBorder(BorderFactory.createTitledBorder("Show"));
		showCheckBoxPanel.setLayout(new BoxLayout(showCheckBoxPanel,
				BoxLayout.PAGE_AXIS));

		// A checkbox to show/hide error bars.
		errorBarCheckBox = new JCheckBox("Error bars?");
		errorBarCheckBox.setSelected(this.showErrorBars);
		errorBarCheckBox.addActionListener(createErrorBarCheckBoxListener());
		showCheckBoxPanel.add(errorBarCheckBox);

		// A checkbox to show/hide cross hairs.
		crossHairCheckBox = new JCheckBox("Cross-hairs?");
		crossHairCheckBox.setSelected(this.showCrossHairs);
		crossHairCheckBox.addActionListener(createCrossHairCheckBoxListener());
		showCheckBoxPanel.add(crossHairCheckBox);
		chartControlPanel.add(showCheckBoxPanel);

		chartControlPanel.add(Box.createRigidArea(new Dimension(75, 10)));

		JPanel meanChangePanel = new JPanel();
		meanChangePanel.setBorder(BorderFactory
				.createTitledBorder("Mean Series Update"));
		meanChangePanel.setLayout(new BoxLayout(meanChangePanel,
				BoxLayout.PAGE_AXIS));

		// A checkbox to determine whether or not to join mean
		// series elements.
		joinMeansCheckBox = new JCheckBox("Join means?");
		joinMeansCheckBox.setSelected(true);
		joinMeansCheckBox.addActionListener(createJoinMeansCheckBoxListener());
		meanChangePanel.add(joinMeansCheckBox);

		meanChangePanel.add(Box.createRigidArea(new Dimension(75, 10)));

		// Add an update time-elements-in-bin component.
		meanChangePanel.add(timeElementsInBinSettingPane);

		chartControlPanel.add(meanChangePanel);

		// Add extra component, if there is one.
		if (extra != null) {
			JPanel extraPane = new JPanel();
			extraPane.setBorder(BorderFactory.createTitledBorder(extra
					.getName()));
			extraPane.add(extra.getComponent());
			chartControlPanel.add(extraPane);
		}

		return chartControlPanel;
	}

	// Creates and returns the series change (visibility and mean source) pane.
	private JPanel createSeriesChangePane() {
		JPanel seriesChangePane = new JPanel();
		seriesChangePane.setLayout(new BoxLayout(seriesChangePane, BoxLayout.PAGE_AXIS));
		seriesChangePane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel seriesPane = new JPanel();
		seriesPane.setLayout(new BoxLayout(seriesPane, BoxLayout.LINE_AXIS));
		seriesVisibilityPane = new SeriesVisibilityPane(obsModel);
		seriesPane.add(seriesVisibilityPane);
		
		meanSourcePane = new MeanSourcePane(obsModel);
		seriesPane.add(meanSourcePane);
		
		seriesChangePane.add(new JScrollPane(seriesPane));

		seriesChangePane.add(Box.createRigidArea(new Dimension(10, 10)));
		
		return seriesChangePane;
	}
	
	// Returns a listener for the error bar visibility checkbox.
	private ActionListener createErrorBarCheckBoxListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleErrorBars();
			}
		};
	}

	// Returns a listener for the cross-hair visibility checkbox.
	private ActionListener createCrossHairCheckBoxListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleCrossHairs();
			}
		};
	}

	/**
	 * Show/hide the error bars.
	 */
	private void toggleErrorBars() {
		this.showErrorBars = !this.showErrorBars;
		plotPane.getRenderer().setDrawYError(this.showErrorBars);
	}

	/**
	 * Show/hide the cross hairs.
	 */
	private void toggleCrossHairs() {
		this.showCrossHairs = !this.showCrossHairs;
		JFreeChart chart = plotPane.getChartPanel().getChart();
		chart.getXYPlot().setDomainCrosshairVisible(this.showCrossHairs);
		chart.getXYPlot().setRangeCrosshairVisible(this.showCrossHairs);
	}

	// Return a listener for the "join means visually" checkbox.
	private ActionListener createJoinMeansCheckBoxListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				toggleJoinMeansSetting();
			}
		};
	}

	// Toggle the "join means" setting which dictates whether or
	// not the means are visually joined (by lines).
	private void toggleJoinMeansSetting() {
		this.joinMeans = !this.joinMeans;
		plotPane.getRenderer().setSeriesLinesVisible(
				this.obsModel.getMeansSeriesNum(), this.joinMeans);
	}

	protected JPanel createButtonPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		dismissButton = new JButton("Dismiss");
		dismissButton.addActionListener(createDismissButtonListener());
		panel.add(dismissButton);

		this.getRootPane().setDefaultButton(dismissButton);

		return panel;
	}

	// Returns a dismiss button listener.
	protected ActionListener createDismissButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		};
	}
}
