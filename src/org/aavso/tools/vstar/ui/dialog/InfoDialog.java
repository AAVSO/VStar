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
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;

/**
 * Information dialog for the currently loaded document.
 */
public class InfoDialog extends JDialog implements ActionListener {

	/**
	 * Constructor.
	 * 
	 * @param newStarMessage
	 *            A new (loaded) star message.
	 */
	public InfoDialog(NewStarMessage newStarMessage) {
		super(DocumentManager.findActiveWindow());
		this.setTitle("Information");

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JScrollPane scrollPane = new JScrollPane(
				createInfoPanel(newStarMessage));
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
		this.setVisible(true);
	}

	/**
	 * Create the info pane given a new star message.
	 */
	private JPanel createInfoPanel(NewStarMessage msg) {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

		StarInfo starInfo = msg.getStarInfo();
		List<ValidObservation> obs = msg.getObservations();
		Map<SeriesType, List<ValidObservation>> obsCategoryMap = msg
				.getObsCategoryMap();

		JTextArea summaryTextArea = new JTextArea();
		summaryTextArea.setEditable(false);
		summaryTextArea.setBorder(BorderFactory.createTitledBorder("Summary"));
		JTextArea seriesTextArea = new JTextArea();
		seriesTextArea.setEditable(false);
		seriesTextArea.setBorder(BorderFactory.createTitledBorder("Series"));
		JTextArea statsTextArea = new JTextArea();
		statsTextArea.setEditable(false);
		statsTextArea.setBorder(BorderFactory.createTitledBorder("Statistics"));

		StringBuffer summaryBuf = new StringBuffer();

		summaryBuf.append("Object: ");
		summaryBuf.append(starInfo.getDesignation());
		summaryBuf.append("\n");

		summaryBuf.append("Source Type: ");
		summaryBuf.append(msg.getStarInfo().getRetriever().getSourceType());
		summaryBuf.append("\n");

		summaryBuf.append("Source Name: ");
		summaryBuf.append(msg.getStarInfo().getRetriever().getSourceName());
		summaryBuf.append("; loaded: ");
		summaryBuf.append(msg.getLoadDate().toString());
		summaryBuf.append("\n");

		if (starInfo.getAuid() != null) {
			summaryBuf.append("AUID: ");
			summaryBuf.append(starInfo.getAuid());
			summaryBuf.append("\n");
		}

		if (starInfo.getPeriod() != null) {
			summaryBuf.append("Period: ");
			summaryBuf.append(starInfo.getPeriod());
			summaryBuf.append(" days\n");
		}

		if (starInfo.getVarType() != null) {
			summaryBuf.append("Variable Type: ");
			summaryBuf.append(starInfo.getVarType());
			summaryBuf.append("\n");
		}

		if (starInfo.getSpectralType() != null) {
			summaryBuf.append("Spectral Type: ");
			summaryBuf.append(starInfo.getSpectralType());
			summaryBuf.append("\n");
		}

		if (starInfo.getDiscoverer() != null) {
			summaryBuf.append("Discoverer: ");
			summaryBuf.append(starInfo.getDiscoverer());
			summaryBuf.append("\n");
		}

		summaryBuf.append("Loaded Observations: ");
		summaryBuf.append(obs.size());
		summaryBuf.append("\n");

		summaryTextArea.setText(summaryBuf.toString());

		StringBuffer seriesBuf = new StringBuffer();

		for (SeriesType type : obsCategoryMap.keySet()) {
			List<ValidObservation> obsOfType = obsCategoryMap.get(type);
			if (!obsOfType.isEmpty()) {
				seriesBuf.append(type.getDescription());
				seriesBuf.append(": ");
				seriesBuf.append(obsOfType.size());
				seriesBuf.append("\n");
			}
		}

		seriesTextArea.setText(seriesBuf.toString());

		StringBuffer statsBuf = new StringBuffer();

		Map<String, String> statsInfo = Mediator.getInstance()
				.getDocumentManager().getStatsInfo();
		for (String key : statsInfo.keySet()) {
			statsBuf.append(key);
			statsBuf.append(": ");
			statsBuf.append(statsInfo.get(key));
			statsBuf.append("\n");
		}

		statsTextArea.setText(statsBuf.toString());

		pane.add(summaryTextArea);
		pane.add(Box.createRigidArea(new Dimension(10, 10)));
		pane.add(seriesTextArea);
		pane.add(Box.createRigidArea(new Dimension(10, 10)));
		pane.add(statsTextArea);

		return pane;
	}

	/**
	 * OK button handler.
	 */
	public void actionPerformed(ActionEvent e) {
		this.setVisible(false);
		dispose();
	}
}
