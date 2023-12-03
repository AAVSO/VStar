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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.util.help.Help;

/**
 * This class encapsulates the behaviour of an authentication code-based modal
 * login dialog box.
 * 
 * It requests and validates an authentication code field, only being dismissed
 * when a valid code is entered.
 */
@SuppressWarnings("serial")
public class AuthCodeLoginDialog extends AbstractOkCancelDialog {

	private Pattern whitespacePattern = Pattern.compile("^\\s*$");

	private static final String AUTH_URL_PREFIX = "https://apps.aavso.org/auth/external?app=vstar&identifier=";

	private Container contentPane;

	private JTextField authCodeField;

	private String uuid;

	/**
	 * Constructor
	 * 
	 * @param intro The introductory text to be displayed in this dialog.
	 */
	public AuthCodeLoginDialog(String intro) {
		super("Login");

		contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		uuid = UUID.randomUUID().toString();
		String loginURL = AUTH_URL_PREFIX + uuid;

		topPane.add(createIntroPane(intro));
		topPane.add(createOpenURLPane(loginURL));
		topPane.add(createURLPane(loginURL));
		topPane.add(createAuthCodePane());
		topPane.add(createButtonPane());
		contentPane.add(topPane);

		this.pack();
		authCodeField.requestFocusInWindow();
		this.setLocationRelativeTo(Mediator.getUI().getContentPane());
		this.setVisible(true);
	}

	private JPanel createIntroPane(String intro) {
		JPanel panel = new JPanel(new BorderLayout());

		panel.add(new JLabel(intro));

		return panel;
	}

	private JPanel createOpenURLPane(String loginURL) {
		JPanel panel = new JPanel(new BorderLayout());
//		panel.setBorder(BorderFactory.createTitledBorder("AAVSO web login"));
		JButton loginButton = new JButton("Authenticate");
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Help.openURLInWebBrowser(loginURL, "Authentication");

			}
		});
		panel.add(loginButton);

		return panel;
	}

	private JPanel createURLPane(String loginURL) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder("AAVSO login web page link"));
		String loginUrlHTML = loginURL;
		JTextArea urlTextArea = new JTextArea("Or, enter\n\n" + loginUrlHTML + "\n\ninto a web browser");
		urlTextArea.setEditable(false);
		panel.add(urlTextArea);

		return panel;
	}

	private JPanel createAuthCodePane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Enter Authentication Code"));

		authCodeField = new JTextField();
		authCodeField.addActionListener(createFieldActionListener());
		panel.add(authCodeField);

		return panel;
	}

	// Return a listener for the token fields.
	private ActionListener createFieldActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkInput();
			}
		};
	}

	// Validate token field. If it doesn't validate, we pop-up
	// a message box, clear both fields, and start again,
	// otherwise we tell the dialog to go away.
	private void checkInput() {
		if (whitespacePattern.matcher(authCodeField.getText()).matches()) {
			authCodeField.setText("");
			setVisible(true);
		} else {
			// The field validated, so dismiss the dialog box, indicating
			// success.
			setCancelled(false);
			setVisible(false);
			dispose();
		}
	}

	public String getUUID() {
		return uuid;
	}

	public String getCode() {
		return authCodeField.getText();
	}

	protected void cancelAction() {
		// Nothing to do
	}

	protected void okAction() {
		checkInput();
	}
}
