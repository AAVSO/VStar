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
package org.aavso.tools.vstar.ui.dialog;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.SeriesCreationMessage;

/**
 * This dialog permits a new series type to be defined and created and
 * associated with a list of observations.
 */
@SuppressWarnings("serial")
public class SeriesTypeCreationDialog extends AbstractOkCancelDialog {

	private List<ValidObservation> obs;

	private JTextField nameField;
	private JColorChooser colorChooser;

	/**
	 * Constructor.
	 * 
	 * @param obs
	 *            The list of observations from which to create the series.
	 */
	public SeriesTypeCreationDialog(List<ValidObservation> obs) {
		super("Create Series");

		this.obs = obs;

		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Series name field and colour panes.
		topPane.add(createSeriesNamePane());
		topPane.add(Box.createRigidArea(new Dimension(75, 10)));

		topPane.add(createSeriesColorPane());
		topPane.add(Box.createRigidArea(new Dimension(75, 10)));

		// OK, Cancel
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();
	}

	private JPanel createSeriesNamePane() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		nameField = new JTextField("New Series");
		nameField.setToolTipText("Enter Series Name");
		nameField.setBorder(BorderFactory.createTitledBorder("Name"));

		panel.add(nameField);

		return panel;
	}

	private JPanel createSeriesColorPane() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		colorChooser = new JColorChooser(Color.BLACK);
		colorChooser.setToolTipText("Select Series Color");
		colorChooser.setBorder(BorderFactory.createTitledBorder("Color"));
		panel.add(colorChooser);

		return panel;
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
	 */
	@Override
	protected void cancelAction() {
		// Nothing to do.
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
	 */
	@Override
	protected void okAction() {
		// If a suitably named series has been specified, create its
		// type, associating it with the provided observations.
		String description = nameField.getText().trim();

		if (description.length() != 0) {
			if (!SeriesType.exists(description)) {
				SeriesType type = null;
				Color color = colorChooser.getColor();

				type = SeriesType.create(description, description, color,
						false, true);

				SeriesCreationMessage msg = new SeriesCreationMessage(this,
						type, obs);

				Mediator.getInstance().getSeriesCreationNotifier()
						.notifyListeners(msg);

				// Dismiss the dialog.
				cancelled = false;
				setVisible(false);
				dispose();
			} else {
				MessageBox.showErrorDialog("Series Creation",
						"A series with that name already exists.");
			}
		}
	}
}
