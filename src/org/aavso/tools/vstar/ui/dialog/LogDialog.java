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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.aavso.tools.vstar.ui.VStar;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.ClipboardUtils;
import org.aavso.tools.vstar.util.EmailUtils;

/**
 * This dialog allows the log to be viewed.
 */
@SuppressWarnings("serial")
public class LogDialog extends JDialog implements ActionListener {

	private JTextArea logTextArea;

	/**
	 * Constructor
	 * 
	 * @param newStarMessages
	 *            The loaded star messages.
	 */
	public LogDialog() {
		super(DocumentManager.findActiveWindow());
		this.setTitle("Log");

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JScrollPane scrollPane = new JScrollPane(createLogPanel());
		topPane.add(scrollPane);

		topPane.add(Box.createRigidArea(new Dimension(10, 10)));

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));

		// TODO: localise strings...

		JButton dismissButton = new JButton("Dismiss");
		dismissButton.addActionListener(this);
		buttonPane.add(dismissButton);

		JButton copyButton = new JButton("Copy Log to Clipboard");
		copyButton.addActionListener(e -> {
			ClipboardUtils.copyToClipboard(logTextArea.getText());
			MessageBox.showMessageDialog("Log", "Log text copied to clipboard");
		});
		buttonPane.add(copyButton);

		JButton mailButton = new JButton("Send Log as Email");
		mailButton.addActionListener(e -> {
			EmailUtils.createEmailMessage("vstar@aavso.org", "VStar Log",
					logTextArea.getText());
		});
		buttonPane.add(mailButton);

		topPane.add(buttonPane);

		this.getContentPane().add(topPane);

		this.getRootPane().setDefaultButton(dismissButton);

		this.pack();
		this.setLocationRelativeTo(Mediator.getUI().getContentPane());
		this.setVisible(true);
	}

	/**
	 * Create the info pane showing the log content.
	 */
	private JPanel createLogPanel() {
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

		// Series information.
		logTextArea = new JTextArea();
		logTextArea.setEditable(false);
		logTextArea.setBorder(BorderFactory.createTitledBorder("Content"));

		String logText = "";

		try {
			FileReader fileReader = new FileReader(VStar.LOG_PATH);
			BufferedReader reader = new BufferedReader(fileReader);
			StringBuffer buf = new StringBuffer();
			String line = reader.readLine();
			while (line != null) {
				buf.append(line);
				buf.append("\n");
				line = reader.readLine();
			}
			logText = buf.toString();
			reader.close();
		} catch (FileNotFoundException e) {
			// Nothing to do
		} catch (IOException e) {
			// Nothing to do
		}

		logTextArea.setText(logText);

		pane.add(logTextArea);

		return pane;
	}

	/**
	 * Dismiss button handler.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		this.setVisible(false);
		dispose();
	}
}
