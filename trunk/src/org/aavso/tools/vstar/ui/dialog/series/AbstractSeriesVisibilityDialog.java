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
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.model.plot.ObservationPlotModel;

/**
 * This dialog permits the visibility of plot series to be changed, and allows
 * subclasses to extend its functionality by adding series-related panes and
 * behavior. Note that this class must be specialised at least by specifying
 * the generic parameter.
 *
 * TODO: change the generic type to permit Void (set of types?)
 */
abstract public class AbstractSeriesVisibilityDialog<T extends JPanel> extends AbstractOkCancelDialog {

	protected JPanel topPane;
	protected JPanel seriesPane;

	protected ObservationPlotModel obsPlotModel;
	protected SeriesVisibilityPane seriesVisibilityPane;
	protected T nextPane;

	/**
	 * Constructor.
	 * 
	 * @param obsPlotModel
	 *            An observation plot model.
	 */
	public AbstractSeriesVisibilityDialog(ObservationPlotModel obsPlotModel) {
		this(obsPlotModel, "Change Series", null);
	}

	/**
	 * Constructor.
	 * 
	 * @param obsPlotModel
	 *            An observation plot model.
	 * @param title
	 *            The dialog title.
	 * @param nextPane
	 *            An optional (may be null) pane to be added next to the primary
	 *            pane. This allows us to specialise dialog panes by chaining.
	 */
	public AbstractSeriesVisibilityDialog(ObservationPlotModel obsPlotModel,
			String title, T nextPane) {
		super(title);

		this.obsPlotModel = obsPlotModel;
		this.nextPane = nextPane;

		Container contentPane = this.getContentPane();
		
		// This pane contains a series pane and buttons.

		topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		seriesPane = new JPanel();
		seriesPane.setLayout(new BoxLayout(seriesPane, BoxLayout.LINE_AXIS));
		seriesVisibilityPane = new SeriesVisibilityPane(obsPlotModel);
		seriesPane.add(seriesVisibilityPane);
		
		if (nextPane != null) {
			seriesPane.add(nextPane);
		}
		
		topPane.add(new JScrollPane(seriesPane));

		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
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
	 * @return the nextPane
	 */
	public T getNextPane() {
		return nextPane;
	}

	/**
	 * @return the visibilityDeltaMap
	 */
	public Map<Integer, Boolean> getVisibilityDeltaMap() {
		return seriesVisibilityPane.getVisibilityDeltaMap();
	}
}
