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
package org.aavso.tools.vstar.ui.dialog.series;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.util.locale.LocaleProps;

/**
 * This class represents a dialog that permits a single series to be selected.
 */
@SuppressWarnings("serial")
public class SingleSeriesSelectionDialog extends AbstractOkCancelDialog {

	protected JPanel seriesPane;

	protected ObservationAndMeanPlotModel obsPlotModel;

	/**
	 * Constructor.
	 * 
	 * @param obsPlotModel
	 *            An observation plot model including means.
	 */
	public SingleSeriesSelectionDialog(
			ObservationAndMeanPlotModel obsPlotModel) {
		super(LocaleProps.get("SELECT_SINGLE_SERIES_DLG_TITLE"));

		this.obsPlotModel = obsPlotModel;

		Container contentPane = this.getContentPane();

		// This pane contains a series pane and buttons.

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		seriesPane = new JPanel();
		seriesPane.setLayout(new BoxLayout(seriesPane, BoxLayout.LINE_AXIS));
		seriesPane.add(new SingleSeriesSelectionPane(obsPlotModel));

		topPane.add(new JScrollPane(seriesPane));

		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();
		this.setLocationRelativeTo(Mediator.getUI().getContentPane());
		this.setVisible(true);
	}

	/**
	 * @return has the dialog been cancelled?
	 */
	public boolean isCancelled() {
		return cancelled;
	}

	protected void cancelAction() {
		// Nothing to do
	}

	protected void okAction() {
		cancelled = false;
		setVisible(false);
		dispose();
	}

	/**
	 * @return The selected series.
	 */
	public SeriesType getSeries() {
		return obsPlotModel.getLastSinglySelectedSeries();
	}
}
