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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;

/**
 * Information dialog for the currently loaded document.
 */
public class InfoDialog extends JDialog implements ActionListener {

	// TODO: Use a table instead of a text area?
	// Column numbers could vary depending upon
	// how much info is present in StarInfo etc.
	// Add more data of interest, e.g. spectral type

	private JTextArea textArea;

	/**
	 * Constructor.
	 * 
	 * @param newStarMessage A new (loaded) star message.
	 */
	public InfoDialog(NewStarMessage newStarMessage) {
		super();
		this.setTitle("Information");
		this.setModal(false);

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		textArea = new JTextArea();
		textArea.setEditable(false);
		setText(newStarMessage);
		JScrollPane scrollPane = new JScrollPane(textArea);
		topPane.add(scrollPane);
		
		topPane.add(Box.createRigidArea(new Dimension(10, 10)));

		JPanel buttonPane = new JPanel();
		JButton okButton = new JButton("OK");
		okButton.addActionListener(this);
		buttonPane.add(okButton, BorderLayout.CENTER);
		topPane.add(buttonPane);
		
		this.getContentPane().add(topPane);

		this.getRootPane().setDefaultButton(okButton);

		this.pack();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setAlwaysOnTop(true);
		this.setVisible(true);
	}

	/**
	 * Set the dialog text given a new star message.
	 */
	private void setText(NewStarMessage msg) {
		StarInfo starInfo = msg.getStarInfo();
		List<ValidObservation> obs = msg.getObservations();
		Map<SeriesType, List<ValidObservation>> obsCategoryMap = msg
				.getObsCategoryMap();

		StringBuffer buf = new StringBuffer();

		buf.append("Object: ");
		buf.append(starInfo.getDesignation());
		buf.append("\n");

		if (starInfo.getAuid() != null) {
			buf.append("AUID: ");
			buf.append(starInfo.getAuid());
			buf.append("\n");
		}

		if (starInfo.getPeriod() != null) {
			buf.append("Period: ");
			buf.append(starInfo.getPeriod());
			buf.append(" days\n");
		}

		buf.append("Loaded Observations: ");
		buf.append(obs.size());
		buf.append("\n");

		for (SeriesType type : obsCategoryMap.keySet()) {
			List<ValidObservation> obsOfType = obsCategoryMap.get(type);
			buf.append(type.getDescription());
			buf.append(": ");
			buf.append(obsOfType.size());
			buf.append("\n");
		}

		textArea.setText(buf.toString());
	}

	/**
	 * OK button handler.
	 */
	public void actionPerformed(ActionEvent e) {
		this.setVisible(false);
		dispose();
	}
}
