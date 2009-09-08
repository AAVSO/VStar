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
package org.aavso.tools.vstar.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.model.ObservationPlotModel;

/**
 * This dialog permits the visibility of plot series to be changed.
 */
public class SeriesVisibilityDialog<T extends JPanel> extends JDialog {

	protected JPanel topPane;
	protected JPanel seriesPane;

	protected ObservationPlotModel obsPlotModel;
	protected SeriesVisibilityPane seriesVisibilityPane;
	protected T nextPane;

	private boolean cancelled;

	/**
	 * Constructor.
	 * 
	 * @param obsPlotModel
	 *            An observation plot model.
	 */
	public SeriesVisibilityDialog(ObservationPlotModel obsPlotModel) {
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
	public SeriesVisibilityDialog(ObservationPlotModel obsPlotModel,
			String title, T nextPane) {
		super();
		this.setTitle(title);
		this.setModal(true);

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
		
		topPane.add(seriesPane);

		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setVisible(true);
	}

	// TODO: need to refactor code for OkCancelDialog (including topPane)

	private JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(createCancelButtonListener());
		panel.add(cancelButton, BorderLayout.LINE_START);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(createOKButtonListener());
		panel.add(okButton, BorderLayout.LINE_END);

		return panel;
	}

	// Return a listener for the "OK" button.
	private ActionListener createOKButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		};
	}

	// Return a listener for the "cancel" button.
	private ActionListener createCancelButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelled = true;
				setVisible(false);
				dispose();
			}
		};
	}

	/**
	 * @return has the dialog been cancelled?
	 */
	public boolean isCancelled() {
		return cancelled;
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
