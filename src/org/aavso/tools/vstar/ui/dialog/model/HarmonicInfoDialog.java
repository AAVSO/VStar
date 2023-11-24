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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
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
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysis2DChartPane;
import org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysisDataTablePane;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.HarmonicSearchResultMessage;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.util.Tolerance;
import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPoint;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.jfree.chart.plot.XYPlot;

/**
 * This dialog shows harmonics found from a search for harmonics of some
 * frequency. When an entry is selected, the cross-hair of the corresponding
 * plot is moved to pin-point the frequency.
 */
@SuppressWarnings("serial")
public class HarmonicInfoDialog extends JDialog implements
		ListSelectionListener {

	private HarmonicSearchResultMessage msg;
	//private PeriodAnalysis2DChartPane plotPane;
	private Component interfaceComponent;

	private double startX, startY;
	
	private ArrayList<Integer> startIndices;

	private JList harmonicList;
	private DefaultListModel harmonicListModel;

	private Map<String, Harmonic> harmonicMap;

	/**
	 * Constructor.
	 * 
	 * @param msg
	 *            The harmonic search result message.
	 * @param interfaceComponent
	 *            plot pane or data table.
	 */
	public HarmonicInfoDialog(HarmonicSearchResultMessage msg,
			Component interfaceComponent) {
		super(DocumentManager.findActiveWindow());

		this.setTitle("Harmonics");
		this.setModal(true);

		this.msg = msg;
		
		this.interfaceComponent = interfaceComponent;
		if (interfaceComponent instanceof PeriodAnalysis2DChartPane) {
			XYPlot plot = ((PeriodAnalysis2DChartPane)interfaceComponent).getChart().getXYPlot();
			startX = plot.getDomainCrosshairValue();
			startY = plot.getRangeCrosshairValue();
		} else if (interfaceComponent instanceof PeriodAnalysisDataTablePane) {
			JTable table = ((PeriodAnalysisDataTablePane)interfaceComponent).getTable();
			 int[] indices = table.getSelectedRows();
			 startIndices = new ArrayList<Integer>();
			 for (int row : indices) {
				 startIndices.add(row);
			 }
		}
		
		this.harmonicMap = new TreeMap<String, Harmonic>();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createListPane());
		topPane.add(createButtonPane());

		getContentPane().add(topPane);
		pack();

		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				dismiss();
			}
		});
		
		setLocationRelativeTo(Mediator.getUI().getContentPane());
		setVisible(true);
	}

	private JPanel createListPane() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		harmonicListModel = new DefaultListModel();

		for (Harmonic harmonic : msg.getHarmonics()) {
			String label = "Frequency: " + harmonic.toString() + " ("
					+ harmonic.getHarmonicNumber() + "f), Period: "
					+ NumericPrecisionPrefs.formatOther(harmonic.getPeriod());
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

		JButton dismissButton = new JButton("Dismiss");
		dismissButton.addActionListener(createDismissButtonListener());
		dismissButton.setEnabled(true);
		panel.add(dismissButton);

		JButton copyButton = new JButton("Copy");
		copyButton.addActionListener(createCopyButtonListener());
		copyButton.setEnabled(true);
		panel.add(copyButton);
		
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
				double x;
				
				if (interfaceComponent instanceof PeriodAnalysis2DChartPane) {
					PeriodAnalysis2DChartPane plotPane = (PeriodAnalysis2DChartPane)interfaceComponent;
					if (plotPane.getModel().getDomainType() == PeriodAnalysisCoordinateType.FREQUENCY) { 
						x = harmonic.getFrequency();
					} else if (plotPane.getModel().getDomainType() == PeriodAnalysisCoordinateType.PERIOD) { 
						x = harmonic.getPeriod();
					} else {
						return;
					}
					plotPane.setCrossHair(x, 0);
				} else if (interfaceComponent instanceof PeriodAnalysisDataTablePane) {
					selectHarmonic(((PeriodAnalysisDataTablePane)interfaceComponent).getTable(), harmonic, msg.getTolerance());
				}
			}
		}
	}
	
	private void selectHarmonic(JTable table, Harmonic harmonic, double tolerance) {
		if (table.getModel() instanceof PeriodAnalysisDataTableModel) {
			PeriodAnalysisDataTableModel model = (PeriodAnalysisDataTableModel)(table.getModel());
			Integer closestRow = null;
			Double minDiff = null;
			int n = harmonic.getHarmonicNumber();
			//System.out.println("\nselectHarmonic");
			for (int row = 0; row < model.getRowCount(); row++) {
				PeriodAnalysisDataPoint dataPoint = model.getDataPointFromRow(row);
				double f = dataPoint.getFrequency();
				// use tolerance but also look for the closest point inside the tolerance!
				if (Tolerance.areClose(f / n, harmonic.getFrequency() / n, tolerance, false)) {
					double dif = Math.abs(f - harmonic.getFrequency());
					if (minDiff == null || dif < minDiff) {
						minDiff = dif;
						closestRow = row;
						//System.out.println("minDif = " + minDiff);
					}
				}
			}
			if (closestRow != null) {
				closestRow = table.convertRowIndexToView(closestRow);				
				ensureVisible(table, closestRow);
				boolean state = ((PeriodAnalysisDataTablePane)interfaceComponent).disableValueChangeEvent();
				try {
					table.setRowSelectionInterval(closestRow, closestRow);
				} finally {
					((PeriodAnalysisDataTablePane)interfaceComponent).setValueChangedDisabledState(state);
				}
			} else {
				throw new IllegalArgumentException("Harmonic not found");
			}
		}
	}

	private void dismiss() {
		setVisible(false);
		dispose();
		// Restore the plot's cross hair or table's selection(s).
		if (interfaceComponent instanceof PeriodAnalysis2DChartPane) {
			((PeriodAnalysis2DChartPane)interfaceComponent).setCrossHair(startX, startY);
		} else if (interfaceComponent instanceof PeriodAnalysisDataTablePane) {
			JTable table = ((PeriodAnalysisDataTablePane)interfaceComponent).getTable();
			if (startIndices != null) {
				if (startIndices.size() > 0) {
					ensureVisible(table, startIndices.get(0));
				}
				boolean state = ((PeriodAnalysisDataTablePane)interfaceComponent).disableValueChangeEvent();
				try {
					table.clearSelection();
					for (int row : startIndices) {
						table.addRowSelectionInterval(row, row);
					}
				} finally {
					((PeriodAnalysisDataTablePane)interfaceComponent).setValueChangedDisabledState(state);
				}
			}
		}
	}

	private void ensureVisible(JTable table, int row) {
		int colWidth = (int) table.getCellRect(row, 0, true).getWidth();
		int rowHeight = table.getRowHeight(row);
		table.scrollRectToVisible(new Rectangle(colWidth, rowHeight * row, colWidth, rowHeight));
	}
	
	// Return a listener for the "Dismiss" button.
	private ActionListener createDismissButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dismiss();
			}
		};
	}
	
	// Return a listener for the "Dismiss" button.
	private ActionListener createCopyButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = "";
				for (int i = 0; i < harmonicListModel.size(); i++) {
					s += harmonicListModel.get(i).toString() + "\n";
				}
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(new StringSelection(s) , null);
			}
		};
	}

}
