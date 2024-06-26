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
package org.aavso.tools.vstar.ui.dialog.period.wwz;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.model.list.WWZDataTableModel;
import org.aavso.tools.vstar.util.IStartAndCleanup;
import org.aavso.tools.vstar.util.comparator.FormattedDoubleComparator;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.model.WWZMultiperiodicModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.period.wwz.WWZStatistic;

/**
 * This class represents a WWZ data table pane.
 */
@SuppressWarnings("serial")
public class WWZDataTablePane extends JPanel implements ListSelectionListener,
		IStartAndCleanup {

	private List<ValidObservation> obs;

	private JTable table;
	private WWZDataTableModel model;
	private TableRowSorter<WWZDataTableModel> rowSorter;

	private Listener<PeriodAnalysisSelectionMessage> periodAnalysisSelectionListener;

	protected JButton modelButton;

	/**
	 * Constructor
	 * 
	 * @param obs
	 *            The observations on which the WWZ was carried out.
	 * @param model
	 *            The WWZ table model.
	 */
	public WWZDataTablePane(WWZDataTableModel model) {
		super(new GridLayout(1, 1));

		this.obs = model.getWwt().getObs();
		this.model = model;

		table = new JTable(model);
		JScrollPane scrollPane = new JScrollPane(table);

		this.add(scrollPane);

		table.getSelectionModel().addListSelectionListener(this);

		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);

		table.setAutoCreateRowSorter(true);
		FormattedDoubleComparator comparator = FormattedDoubleComparator
				.getInstance();
		rowSorter = new TableRowSorter<WWZDataTableModel>(model);
		for (int i = 0; i < model.getColumnCount(); i++) {
			rowSorter.setComparator(i, comparator);
		}
		table.setRowSorter(rowSorter);

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		add(createButtonPanel());
	}

	protected JPanel createButtonPanel() {
		JPanel buttonPane = new JPanel();

		modelButton = new JButton(LocaleProps.get("CREATE_MODEL_BUTTON"));
		modelButton.setEnabled(false);
		modelButton.addActionListener(createModelButtonHandler());
		buttonPane.add(modelButton, BorderLayout.LINE_END);

		return buttonPane;
	}

	/**
	 * We send a period analysis selection message when the table selection
	 * value has "settled". This event could be consumed by other views such as
	 * plots.
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == table.getSelectionModel()
				&& table.getRowSelectionAllowed() && !e.getValueIsAdjusting()) {
			int row = table.getSelectedRow();

			if (row >= 0) {
				row = table.convertRowIndexToModel(row);

				PeriodAnalysisSelectionMessage message = new PeriodAnalysisSelectionMessage(
						this, model.getDataPointFromRow(row), row);
				message.setTag(Mediator.getParentDialogName(this));
				Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
						.notifyListeners(message);
			}
		}
	}

	// Model button listener.
	private ActionListener createModelButtonHandler() {
		final JPanel parent = this;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<Double> periods = new ArrayList<Double>();
				int[] selectedTableRowIndices = table.getSelectedRows();
				for (int row : selectedTableRowIndices) {
					int modelRow = table.convertRowIndexToModel(row);
					WWZStatistic dataPoint = model
							.getDataPointFromRow(modelRow);
					periods.add(dataPoint.getPeriod());
				}

				if (!periods.isEmpty()) {
					try {
						IModel periodModel = new WWZMultiperiodicModel(model
								.getWwt(), periods);
						Mediator.getInstance().performModellingOperation(
								periodModel);
					} catch (Exception ex) {
						MessageBox.showErrorDialog(parent, "Modelling", ex
								.getLocalizedMessage());
					}
				}
			}
		};
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
				if (!Mediator.isMsgForDialog(Mediator.getParentDialog(WWZDataTablePane.this), info))
					return;
				if (info.getSource() != parent) {
					// Find data point in table.
					int row = -1;
					for (int i = 0; i < model.getRowCount(); i++) {
						if (model.getDataPointFromRow(i).equals(
								info.getDataPoint())) {
							row = i;
							break;
						}
					}

					// Note that the row may not correspond to anything in the
					// data table, e.g. in the case of period analysis
					// refinement or if a datapoint was selected from the full
					// set of data and this update method is being called in
					// the context of a maximal WWZ data set.
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
				return true;
			}
		};
	}

	/**
	 * Enable the buttons on this pane.
	 */
	protected void enableButtons() {
		modelButton.setEnabled(true);
	}

	@Override
	public void startup() {
		periodAnalysisSelectionListener = createPeriodAnalysisListener();
		Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
				.addListener(periodAnalysisSelectionListener);
	}

	@Override
	public void cleanup() {
		Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
				.removeListenerIfWilling(periodAnalysisSelectionListener);
	}
}
