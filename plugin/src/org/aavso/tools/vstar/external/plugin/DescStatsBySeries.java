/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2014 AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.external.plugin;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.ui.model.plot.JDTimeElementEntity;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;
import org.aavso.tools.vstar.util.stats.DescStats;
import org.apache.commons.math.stat.descriptive.rank.Median;

// PMAK, 2020-02-22
// 1) Magnitude median
// PMAK, 2021-05-29
// 1) JTable instead of text fields
// 2) All visible series shown (except DISCREPANT, Excluded)

/**
 * This is an observation VStar tool plug-in which displays, descriptive
 * statistics about each series.
 * 
 * @author David Benn, modified by Maksym Pyatnytskyy
 * @version 1.1 - 29 May 2021
 * */
public class DescStatsBySeries extends ObservationToolPluginBase {

	@Override
	public void invoke(ISeriesInfoProvider seriesInfo) {
		Vector<Vector<Object>> rows = new Vector<Vector<Object>>();

		// "Normal" series
		for (SeriesType type : seriesInfo.getVisibleSeries()) {
			if (!type.isSynthetic() && !type.isUserDefined()
					&& (type != SeriesType.Excluded)
					&& (type != SeriesType.DISCREPANT)) {
				Vector<Object> row = getRow(seriesInfo, type); 
				if (row != null)
					rows.add(row);
			}
		}

		Collections.sort(rows, new SeriesComparator());		

		Vector<Vector<Object>> rows2 = new Vector<Vector<Object>>();		
		
		// Synthetic: Filtered, Mean, Model, etc.
		
		for (SeriesType type : seriesInfo.getVisibleSeries()) {
			if (type.isSynthetic()) {				
				Vector<Object> row = getRow(seriesInfo, type); 
				if (row != null)
					rows2.add(row);
			}
		}
		
		if (rows2.size() > 0) {
			Collections.sort(rows2, new SeriesComparator());
			rows.add(null);
			for (Vector<Object> v : rows2) {
				rows.add(v);
			}
		}
		
		rows2.clear();

		// User-defined series
		
		for (SeriesType type : seriesInfo.getVisibleSeries()) {
			if (type.isUserDefined()) {				
				Vector<Object> row = getRow(seriesInfo, type); 
				if (row != null)
					rows2.add(row);
			}
		}
		
		if (rows2.size() > 0) {
			Collections.sort(rows2, new SeriesComparator());				
			rows.add(null);
			for (Vector<Object> v : rows2) {
				rows.add(v);
			}
		}
		
        // Column Names
        Vector<String> columnNames = new Vector<String>();
        columnNames.add("Series");
        columnNames.add("Number of obs");
        columnNames.add("JD mean");
        columnNames.add("Mag mean");
        columnNames.add("StDev");
        columnNames.add("Mag median");
		
		new DescStatsDialog(rows, columnNames);
	}
	
	public class SeriesComparator implements Comparator<Vector<Object>> {

	    @Override
	    public int compare(Vector<Object> first, Vector<Object> second) {
	    	String name1;
	    	String name2;
	    	if (first != null && first.size() > 0 && first.elementAt(0) != null)
	    		name1 = first.elementAt(0).toString();
	    	else
	    		name1 = "";
	    	if (second != null && second.size() > 0 && second.elementAt(0) != null)
	    		name2 = second.elementAt(0).toString();
	    	else
	    		name2 = "";
	    	return name1.compareTo(name2);
	    }

	}	
	
	private Vector<Object> getRow(ISeriesInfoProvider seriesInfo, SeriesType type) {
		Vector<Object> row = null;
		List<ValidObservation> obs = seriesInfo.getObservations(type);
		int obsSize = obs.size();
		if (obsSize > 0) {
			int numberOfObservations = calcNumberInRange(obs, 0, obsSize - 1);
			double[] means = DescStats.calcMagMeanInRange(obs, JDTimeElementEntity.instance, 0, obsSize - 1);
			double stdev = DescStats.calcMagSampleStdDevInRange(obs, 0, obsSize - 1);
			double median = calcMagMedianInRange(obs, 0, obsSize - 1);
			row = new Vector<Object>();   
			row.add(type.getDescription());
			row.add(numberOfObservations);
			row.add(means[1]);
			row.add(means[0]);
			row.add(stdev);
			row.add(median);
		}
		return row;
	}

	@Override
	public String getDescription() {
		return "Observation tool plug-in to display descriptive statistics by series";
	}

	@Override
	public String getDisplayName() {
		return "Descriptive statistics by series";
	}

    // The calcMedian method can be moved to DescStats;
	// some checks are excessive here. 
	
	/**
	 * Calculates the means of a sequence of magnitudes and time elements for
	 * observations in a specified inclusive range.
	 * 
	 * @param observations
	 *            A list of valid observations.
	 * @param minIndex
	 *            The first observation index in the inclusive range.
	 * @param maxIndex
	 *            The last observation index in the inclusive range.
	 * @return The median of magnitudes.
	 */
	private static double calcMagMedianInRange(
			List<ValidObservation> observations,
			int minIndex, int maxIndex) {

		// Pre-conditions.
		assert (!observations.isEmpty());
		assert (maxIndex >= minIndex);
		assert (maxIndex < observations.size());
		
		ArrayList<Double> magValues = new ArrayList<Double>();
		for (int i = minIndex; i <= maxIndex; i++) {
			if (!observations.get(i).isDiscrepant())
				magValues.add(observations.get(i).getMag());
		}
		if (magValues.size() > 0) {
			double[] dvals = new double[magValues.size()];
			for (int i = 0; i < magValues.size(); i++)
				dvals[i] = magValues.get(i);
			return (new Median()).evaluate(dvals);
		} else {
			return Double.NaN;
		}
	}
	
	// For consistency, number of observations should be calculated the same way as
	// mean, stdev, etc.
	
	/**
	 * Calculates the number of observations in a specified inclusive range.
	 * 
	 * @param observations
	 *            A list of valid observations.
	 * @param minIndex
	 *            The first observation index in the inclusive range.
	 * @param maxIndex
	 *            The last observation index in the inclusive range.
	 * @return The number of observations.
	 */
	private static int calcNumberInRange(
			List<ValidObservation> observations,
			int minIndex, int maxIndex) {

		// Pre-conditions.
		assert (!observations.isEmpty());
		assert (maxIndex >= minIndex);
		assert (maxIndex < observations.size());
		
		int included = 0;
		
		for (int i = minIndex; i <= maxIndex; i++) {
			if (!observations.get(i).isDiscrepant())
				included++;
		}
		
		return included;
	}
	
	@SuppressWarnings("serial")
	private class DescStatsDialog extends JDialog {
		
		/**
		 * Constructor
		 */
		public DescStatsDialog(Vector<Vector<Object>> rows, Vector<String> columnNames) {
			super(DocumentManager.findActiveWindow(), getDisplayName(), ModalityType.APPLICATION_MODAL);
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			ActionListener cancelListener = createDismissButtonListener();
		    getRootPane().registerKeyboardAction(cancelListener, 
		    		KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 
		    		JComponent.WHEN_IN_FOCUSED_WINDOW);

			
			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
			topPane.add(createTablePane(rows, columnNames));

			topPane.add(createButtonPane(cancelListener));

			contentPane.add(topPane);

			this.pack();
			
			showDialog();
		}
		
		private JScrollPane createTablePane(Vector<Vector<Object>> rows, Vector<String> columnNames) {
			
			DescStatTableModel tableModel = new DescStatTableModel(rows, columnNames); 
			
	        JTable table = new JTable(tableModel);
	        table.setCellSelectionEnabled(true);
	        
	        JScrollPane pane = new JScrollPane(table);
	        pane.setPreferredSize(new Dimension(600, 200));
	        return pane;
		}
		
		private class DescStatTableModel extends AbstractTableModel {

			private Vector<Vector<Object>> rows;
			private Vector<String> columnNames;
			
			public DescStatTableModel(Vector<Vector<Object>> rows, Vector<String> columnNames) {
				this.rows = rows;
				this.columnNames = columnNames;
			}
			
			@Override
			public int getColumnCount() {
				return columnNames.size();
			}

			@Override
			public int getRowCount() {
				return rows.size();
			}

			@Override
			public String getColumnName(int col) {
		        return columnNames.elementAt(col);
		    }			
			
			@Override
			public Object getValueAt(int row, int col) {
				Vector<Object> rowObj = rows.elementAt(row);
				if (rowObj != null && col < rowObj.size()) {
					Object val = rowObj.elementAt(col);
					if (col > 1) {
						if (val != null && val instanceof Double) {
							if (col == 2)
								val = NumericPrecisionPrefs.formatTime((Double)val);
							else
								val = NumericPrecisionPrefs.formatMag((Double)val);
						}
					}
					return val;
				} else {
					return null;
				}
			}
			
			@Override
			public boolean isCellEditable(int row, int col) { 
				return false; 
			}			
			
		}
		
		private JPanel createButtonPane(ActionListener cancelListener) {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

			JButton dismissButton = new JButton(LocaleProps.get("DISMISS_BUTTON"));
			dismissButton.addActionListener(cancelListener);
			panel.add(dismissButton);

			return panel;
		}

		// Return a listener for the "OK" button.
		private ActionListener createDismissButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			};
		}
		
		private void showDialog() {
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			setVisible(true);
		}
		
	}
}