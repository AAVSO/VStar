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

import java.awt.Rectangle;

import javax.swing.event.ListSelectionEvent;

import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.ui.model.list.PeriodAnalysisDataTableModel;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;

/**
 * Top hits table pane.
 * @deprecated
 */
public class PeriodAnalysisTopHitsTablePane extends PeriodAnalysisDataTablePane {

	private PeriodAnalysisDataTableModel topHitsModel;
	private PeriodAnalysisDataTableModel fullDataModel;

	/**
	 * Constructor.
	 * 
	 * @param topHitsModel
	 *            The top hits data model.
	 * @param fullDataModel
	 *            The full data data model.
	 */
	public PeriodAnalysisTopHitsTablePane(
			PeriodAnalysisDataTableModel topHitsModel,
			PeriodAnalysisDataTableModel fullDataModel) {
		super(topHitsModel);
		this.topHitsModel = topHitsModel;
		this.fullDataModel = fullDataModel;
	}

	/**
	 * We send a row selection event when the table selection value has
	 * "settled". This event could be consumed by other views such as plots. We
	 * find a row in the data table given the selected top hits table row and
	 * send a message with that row.
	 */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == table.getSelectionModel()
				&& table.getRowSelectionAllowed() && !e.getValueIsAdjusting()) {
			// Which row in the top hits table was selected?
			int row = table.getSelectedRow();

			if (row >= 0) {
				row = table.convertRowIndexToModel(row);

				// Now, what index (row) does this correspond to in the full
				// data table model? We arbitrarily compare period values.
				int fullDataRow = -1;
				double selectedPeriod = topHitsModel.getData().get(
						PeriodAnalysisCoordinateType.PERIOD).get(row);

				for (int i = 0; i < fullDataModel.getRowCount(); i++) {
					if (fullDataModel.getData().get(
							PeriodAnalysisCoordinateType.PERIOD).get(i) == selectedPeriod) {
						fullDataRow = i;
						break;
					}
				}

				// If the row was found (no reason it should not have been),
				// send a period analysis selection message.
				if (fullDataRow != -1) {
					PeriodAnalysisSelectionMessage message = new PeriodAnalysisSelectionMessage(
							this, fullDataRow);
					Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
							.notifyListeners(message);
				}
			}
		}
	}

	/**
	 * We convert from a full data index to the corresponding row in the top
	 * hits table before selecting it.
	 */
	@Override
	public void update(PeriodAnalysisSelectionMessage info) {
		if (info.getSource() != this) {
			// Scroll to an arbitrary column (zeroth) within
			// the selected row, then select that row.
			// Assumption: we are specifying the zeroth cell
			// within row i as an x,y coordinate relative to
			// the top of the table pane.
			// Note that we could call this on the scroll
			// pane, which would then forward the request to
			// the table pane anyway.
			try {
				// Convert from full data index to top hits table row.
				double selectedPeriod = fullDataModel.getData().get(
						PeriodAnalysisCoordinateType.PERIOD)
						.get(info.getItem());
				int row = -1;
				for (int i = 0; i < topHitsModel.getRowCount(); i++) {
					if (topHitsModel.getData().get(
							PeriodAnalysisCoordinateType.PERIOD).get(i) == selectedPeriod) {
						row = i;
						break;
					}
				}

				// Note that it may not be in the top hits table since there's
				// more data in the full dataset than is here!
				if (row != -1) {
					// Convert to view index!
					row = table.convertRowIndexToView(row);

					int colWidth = (int) table.getCellRect(row, 0, true)
							.getWidth();
					int rowHeight = table.getRowHeight(row);
					table.scrollRectToVisible(new Rectangle(colWidth, rowHeight
							* row, colWidth, rowHeight));

					table.setRowSelectionInterval(row, row);
				}
			} catch (Throwable t) {
				// TODO: investigate! (e.g. Johnson V band, then click top-most
				// top hits table row).
				// t.printStackTrace();
			}
		}
	}
}
