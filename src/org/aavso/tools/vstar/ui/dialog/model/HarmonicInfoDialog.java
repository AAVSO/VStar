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
package org.aavso.tools.vstar.ui.dialog.model;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysis2DChartPane;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.message.HarmonicSearchResultMessage;
import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This dialog shows harmonics found from a search for harmonics of some
 * frequency. When an entry is selected, the cross-hair of the corresponding
 * plot is moved to pin-point the frequency.
 */
public class HarmonicInfoDialog extends JDialog implements
		ListSelectionListener {

	private HarmonicSearchResultMessage msg;
	private PeriodAnalysis2DChartPane plotPane;

	private double startX, startY;

	private JList harmonicList;
	private DefaultListModel harmonicListModel;

	private Map<String, Harmonic> harmonicMap;

	private JButton dismissButton;

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *            The harmonic search result message.
	 * @param plotPane
	 *            The corresponding plot pane to set the cross-hair on.
	 */
	public HarmonicInfoDialog(HarmonicSearchResultMessage msg,
			PeriodAnalysis2DChartPane plotPane) {
		super(DocumentManager.findActiveWindow());

		this.setTitle("Harmonics");
		this.setModal(true);

		this.msg = msg;
		this.plotPane = plotPane;

		startX = plotPane.getChart().getXYPlot().getDomainCrosshairValue();
		startY = plotPane.getChart().getXYPlot().getRangeCrosshairValue();

		this.harmonicMap = new TreeMap<String, Harmonic>();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createListPane());
		topPane.add(createButtonPane());

		getContentPane().add(topPane);
		pack();
		setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		setVisible(true);
	}

	private JPanel createListPane() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		harmonicListModel = new DefaultListModel();

		for (Harmonic harmonic : msg.getHarmonics()) {
			String label = "Frequency: "
					+ harmonic.toString()
					+ " ("
					+ harmonic.getHarmonicNumber()
					+ "f), Period: "
					+ String.format(NumericPrecisionPrefs
							.getOtherOutputFormat(), harmonic.getPeriod());
			harmonicListModel.addElement(label);
			harmonicMap.put(label, harmonic);
		}

		harmonicList = new JList(harmonicListModel);
		harmonicList
				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		harmonicList.addListSelectionListener(this);
		JScrollPane modelListScroller = new JScrollPane(harmonicList);

		panel.add(modelListScroller);

		return panel;
	}

	private JPanel createButtonPane() {
		JPanel panel = new JPanel(new FlowLayout());

		dismissButton = new JButton("Dismiss");
		dismissButton.addActionListener(createDismissButtonListener());
		dismissButton.setEnabled(true);
		panel.add(dismissButton);

		this.getRootPane().setDefaultButton(dismissButton);

		return panel;
	}

	// List selection listener to update button states.
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			int index = harmonicList.getSelectedIndex();
			if (index != -1) {
				int selectedModelIndex = harmonicList.getSelectedIndex();
				String desc = (String) harmonicListModel
						.get(selectedModelIndex);
				Harmonic harmonic = harmonicMap.get(desc);
				plotPane
						.setCrossHair(harmonic.getFrequency(),
								findNthRangeValueFromFrequency(harmonic
										.getFrequency()));
			}
		}
	}

	// Return the range value corresponding to the specified frequency.
	private Double findNthRangeValueFromFrequency(double frequency) {
		Double value = null;

		List<Double> freqVals = plotPane.getModel().getDomainValues();
		List<Double> rangeVals = plotPane.getModel().getRangeValues();

		int i = 0;
		while (i < freqVals.size()) {
			if (freqVals.get(i) == frequency) {
				value = rangeVals.get(i);
				break;
			}
			i++;
		}

		if (value == null) {
			throw new IllegalArgumentException("Unknown frequency");
		}

		return value;
	}

	// Return a listener for the "Dismiss" button.
	private ActionListener createDismissButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
				// Restore the plot's cross hair.
				plotPane.setCrossHair(startX, startY);
			}
		};
	}
}
