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
package org.aavso.tools.vstar.ui.dialog.period;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;

import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisRefinementMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.util.locale.NumberParser;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisAlgorithm;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPoint;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPointComparator;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * Top hits table pane.
 */
public class PeriodAnalysisTopHitsTablePane extends PeriodAnalysisDataTablePane {

	private Set<PeriodAnalysisDataPoint> refinedDataPoints;
	private Set<PeriodAnalysisDataPoint> resultantDataPoints;

	private JButton refineButton;

	/**
	 * Constructor.
	 * 
	 * @param topHitsModel
	 *            The top hits data model.
	 * @param fullDataModel
	 *            The full data data model.
	 * @param algorithm
	 *            The period analysis algorithm.
	 */
	public PeriodAnalysisTopHitsTablePane(
			PeriodAnalysisDataTableModel topHitsModel,
			PeriodAnalysisDataTableModel fullDataModel,
			IPeriodAnalysisAlgorithm algorithm) {
		super(topHitsModel, algorithm);

		refinedDataPoints = new TreeSet<PeriodAnalysisDataPoint>(
				PeriodAnalysisDataPointComparator.instance);

		resultantDataPoints = new TreeSet<PeriodAnalysisDataPoint>(
				PeriodAnalysisDataPointComparator.instance);

		Mediator.getInstance().getPeriodAnalysisRefinementNotifier()
				.addListener(createRefinementListener());
	}

	protected JPanel createButtonPanel() {
		JPanel buttonPane = super.createButtonPanel();

		refineButton = new JButton(algorithm.getRefineByFrequencyName());
		refineButton.setEnabled(false);
		refineButton.addActionListener(createRefineButtonHandler());
		buttonPane.add(refineButton, BorderLayout.LINE_START);

		return buttonPane;
	}

	// Refine button listener.
	private ActionListener createRefineButtonHandler() {
		final JPanel parent = this;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Collect frequencies to be used in refinement, ensuring that
				// we don't try to use a frequency that has already been used
				// for refinement. We also do not want to use the result of a
				// previous refinement.
				List<Double> freqs = new ArrayList<Double>();
				int[] selectedTableRowIndices = table.getSelectedRows();
				for (int row : selectedTableRowIndices) {
					int modelRow = table.convertRowIndexToModel(row);
					PeriodAnalysisDataPoint dataPoint = model
							.getDataPointFromRow(modelRow);
					if (!refinedDataPoints.contains(dataPoint)) {
						refinedDataPoints.add(dataPoint);
						freqs.add(model.getFrequencyValueInRow(modelRow));
					} else {
						String fmt = NumericPrecisionPrefs
								.getOtherOutputFormat();
						String msg = String.format("Top Hit with frequency "
								+ fmt + " and power " + fmt
								+ " has previously been used.", dataPoint
								.getFrequency(), dataPoint.getPower());
						MessageBox.showErrorDialog(parent, algorithm
								.getRefineByFrequencyName(), msg);
						freqs.clear();
						break;
					}

					if (resultantDataPoints.contains(dataPoint)) {
						String fmt = NumericPrecisionPrefs
								.getOtherOutputFormat();
						String msg = String.format("Top Hit with frequency "
								+ fmt + " and power " + fmt
								+ " was generated by %s so cannot be used.",
								dataPoint.getFrequency(), dataPoint.getPower(),
								algorithm.getRefineByFrequencyName());
						MessageBox.showErrorDialog(parent, algorithm
								.getRefineByFrequencyName(), msg);
						freqs.clear();
						break;
					}
				}

				if (!freqs.isEmpty()) {
					try {
						// Before going ahead with a refinement operation, ask
						// for variable and locked periods.
						List<Double> variablePeriods = requestPeriods(parent,
								"variable");
						List<Double> lockedPeriods = requestPeriods(parent,
								"locked");

						// Perform a refinement operation and get the new
						// top-hits resulting from the refinement.
						List<PeriodAnalysisDataPoint> newTopHits = algorithm
								.refineByFrequency(freqs, variablePeriods,
										lockedPeriods);

						// Update the model and tell anyone else who might be
						// interested.
						Map<PeriodAnalysisCoordinateType, List<Double>> data = algorithm
								.getResultSeries();
						Map<PeriodAnalysisCoordinateType, List<Double>> topHits = algorithm
								.getTopHits();

						model.setData(topHits);

						PeriodAnalysisRefinementMessage msg = new PeriodAnalysisRefinementMessage(
								this, data, topHits, newTopHits);

						Mediator.getInstance()
								.getPeriodAnalysisRefinementNotifier()
								.notifyListeners(msg);
					} catch (AlgorithmError ex) {
						MessageBox.showErrorDialog(parent, algorithm
								.getRefineByFrequencyName(), ex
								.getLocalizedMessage());
					}
				}
			}
		};
	}

	// Collect (and return) period values of the specified kind until the user
	// adds no more.
	private List<Double> requestPeriods(Component parent, String kind) {
		List<Double> periods = new ArrayList<Double>();

		String str = null;

		do {
			JOptionPane pane = new JOptionPane("Add a " + kind + " period?",
					JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
			pane.setWantsInput(true);
			pane.setInputValue("");
			JDialog dialog = pane.createDialog(parent, "Enter Period");
			dialog.setAlwaysOnTop(true);
			dialog.setVisible(true);
			str = (String) pane.getInputValue();

			str = str.trim();

			if (str.length() != 0) {
				try {
					double period = NumberParser.parseDouble(str);
					periods.add(period);
				} catch (NumberFormatException e) {
					MessageBox.showErrorDialog(parent, "Period Value Error",
							String.format("'%s' is not a valid period value.",
									str));
				}
			}
		} while (str.length() != 0);

		return periods;
	}

	/**
	 * Select the row in the table corresponding to the period analysis
	 * selection. We also enable the "refine" button.
	 */
	protected Listener<PeriodAnalysisSelectionMessage> createPeriodAnalysisListener() {
		final Component parent = this;

		return new Listener<PeriodAnalysisSelectionMessage>() {
			@Override
			public void update(PeriodAnalysisSelectionMessage info) {
				if (info.getSource() != parent) {
					// Find data point in top hits table.
					int row = -1;
					for (int i = 0; i < model.getRowCount(); i++) {
						if (model.getDataPointFromRow(i).equals(
								info.getDataPoint())) {
							row = i;
							break;
						}
					}

					// Note that the row may not correspond to anything in the
					// top hits table since there's more data in the full
					// dataset than there is here!
					if (row != -1) {
						// Convert to view index!
						row = table.convertRowIndexToView(row);

						// Scroll to an arbitrary column (zeroth) within
						// the selected row, then select that row.
						// Assumption: we are specifying the zeroth cell
						// within row i as an x,y coordinate relative to
						// the top of the table pane.
						// Note that we could call this on the scroll
						// pane, which would then forward the request to
						// the table pane anyway.
						int colWidth = (int) table.getCellRect(row, 0, true)
								.getWidth();
						int rowHeight = table.getRowHeight(row);
						table.scrollRectToVisible(new Rectangle(colWidth,
								rowHeight * row, colWidth, rowHeight));

						table.setRowSelectionInterval(row, row);
						enableButtons();
					}
				} else {
					enableButtons();
				}
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.period.PeriodAnalysisDataTablePane#enableButtons()
	 */
	@Override
	protected void enableButtons() {
		super.enableButtons();
		refineButton.setEnabled(true);
	}

	/**
	 * We send a period analysis selection message when the table selection
	 * value has "settled". This event could be consumed by other views such as
	 * plots.
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == table.getSelectionModel()
				&& table.getRowSelectionAllowed() && !e.getValueIsAdjusting()) {
			// Which row in the top hits table was selected?
			int row = table.getSelectedRow();

			if (row >= 0) {
				row = table.convertRowIndexToModel(row);
				PeriodAnalysisSelectionMessage message = new PeriodAnalysisSelectionMessage(
						this, model.getDataPointFromRow(row));
				Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
						.notifyListeners(message);
			}
		}
	}

	// Create a period analysis refinement listener which adds refinement
	// results to a collection that is checked to ensure that the user does not
	// select them (or the originating data row) again.
	private Listener<PeriodAnalysisRefinementMessage> createRefinementListener() {
		return new Listener<PeriodAnalysisRefinementMessage>() {
			@Override
			public void update(PeriodAnalysisRefinementMessage info) {
				resultantDataPoints.addAll(info.getNewTopHits());
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}
}
