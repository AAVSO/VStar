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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.Mediator;

/**
 * This dialog is used to display information about a star that has been marked
 * as discrepant in order to optionally add comments and submit to AAVSO.
 */
@SuppressWarnings("serial")
public class DiscrepantReportDialog extends AbstractOkCancelDialog {

	private final static int MAX_COMMENT_LENGTH = 100;

	private String auid;
	private String name;
	private int uniqueId;
	private double jd;
	private double magnitude;

	private JTextField commentsField;
	private JTextField userIdField;

	/**
	 * Constructor
	 * 
	 * @param auid
	 *            The AAVSO unique ID for the object.
	 * @param ob
	 *            The observation to be reported as discrepant.
	 */
	public DiscrepantReportDialog(String auid, ValidObservation ob) {
		super("AAVSO Discrepant Report");
		this.auid = auid;
		this.name = ob.getName();
		this.uniqueId = ob.getRecordNumber();
		this.jd = ob.getJD();
		this.magnitude = ob.getMag();

		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createQuestionPane());
		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(createDicrepantInfoPane());
		topPane.add(Box.createRigidArea(new Dimension(10, 10)));
		topPane.add(createCommentsPane());
		topPane.add(Box.createRigidArea(new Dimension(10, 10)));

		// OK, Cancel
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();
		setLocationRelativeTo(Mediator.getUI().getContentPane());
		this.setVisible(true);
	}

	public String getComments() {
		return commentsField.getText();
	}

	public String getUserId() {
		return userIdField.getText();
	}

	private JPanel createQuestionPane() {
		JPanel panel = new JPanel();

		panel.add(new JLabel("Submit Report to AAVSO?"),
				BorderLayout.LINE_START);

		return panel;
	}

	private JPanel createDicrepantInfoPane() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory
				.createTitledBorder("Discrepant Observation Details"));

		JTextArea details = new JTextArea();
		details.setEditable(false);

		String text = "";
		text += "AUID: " + auid;
		text += "\nName: " + name;
		text += "\nJD: " + jd;
		text += "\nMag: " + magnitude;

		details.setText(text);

		panel.add(details);

		return panel;
	}

	private JPanel createCommentsPane() {
		JPanel panel = new JPanel();
		panel
				.setBorder(BorderFactory
						.createTitledBorder("Comments (optional)"));

		commentsField = new JTextField(20);
		commentsField
				.setToolTipText("Enter comments to be included in discrepant report.");
		panel.add(commentsField);

		return panel;
	}

	@Override
	protected void cancelAction() {
		// Nothing to do.
	}

	@Override
	protected void okAction() {
		if (getComments().length() > MAX_COMMENT_LENGTH) {
			MessageBox.showErrorDialog("Comment",
					"Discrepant observation comment length exceeds "
							+ MAX_COMMENT_LENGTH + " characters ("
							+ getComments().length() + ").");
		} else if (!getUserId().isEmpty() && !getUserId().matches("^\\s+$")) {
			// TODO: this should be the base class implementation of okAction().
			cancelled = false;
			setVisible(false);
			dispose();
		}
	}
}
