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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.aavso.tools.vstar.ui.MainFrame;

/**
 * This class encapsulates the behaviour of a generic modal login dialog box.
 * 
 * It requests and validates username and password fields, only being dismissed
 * when a valid username/password combination is entered or the dialog is
 * dismissed.
 */
public class LoginDialog extends JDialog {

	private Container contentPane;

	private JTextField usernameField;
	private JPasswordField passwordField;

	private Pattern usernamePattern;
	private Pattern passwordPattern;

	private boolean cancelled;

	/**
	 * Constructor
	 * 
	 * @param intro
	 *            The introductory text to be displayed in this dialog's.
	 */
	public LoginDialog(String intro) {
		super();
		this.setTitle("Login");
		this.setModal(true);

		Pattern commonPattern = Pattern
				.compile("^[A-Za-z0-9\\!@#\\$%&\\*\\(\\)_\\-\\+=\\?<>]{2,15}$");

		this.usernamePattern = commonPattern;
		this.passwordPattern = commonPattern;

		this.cancelled = false;

		contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createIntroPane(intro));
		topPane.add(createUsernamePane());
		topPane.add(createPasswordPane());
		topPane.add(createButtonPane());
		contentPane.add(topPane);

		this.pack();
		usernameField.requestFocusInWindow();
		this.setLocationRelativeTo(MainFrame.getInstance().getContentPane());
		this.setVisible(true);
	}

	private JPanel createIntroPane(String intro) {
		JPanel panel = new JPanel(new BorderLayout());

		panel.add(new JLabel(intro));

		return panel;
	}

	private JPanel createUsernamePane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Username"));

		usernameField = new JTextField();
		usernameField.addActionListener(createFieldActionListener());
		panel.add(usernameField);

		return panel;
	}

	private JPanel createPasswordPane() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Password"));

		passwordField = new JPasswordField();
		passwordField.addActionListener(createFieldActionListener());
		panel.add(passwordField);

		return panel;
	}

	private JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(createCancelButtonListener());
		panel.add(cancelButton, BorderLayout.LINE_START);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(createOKButtonListener());
		// okButton.setEnabled(false);
		panel.add(okButton, BorderLayout.LINE_END);

		return panel;
	}

	// Return a listener for the username and password fields.
	private ActionListener createFieldActionListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkInput();
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

	// Return a listener for the "OK" button.
	private ActionListener createOKButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkInput();
			}
		};
	}

	// Validate username and password fields. If either field
	// doesn't validate, we pop-up a message box, clear both
	// fields, and start again, otherwise we tell the dialog
	// to go away.
	private void checkInput() {
		setVisible(false);

		if (!usernamePattern.matcher(usernameField.getText()).matches()
				|| !passwordPattern.matcher(
						new String(passwordField.getPassword())).matches()) {
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					"Login Details Error", "Invalid username or password.");
			usernameField.setText("");
			passwordField.setText("");
			setVisible(true);
		} else {
			// The fields validated, so dismiss the dialog box.
			dispose();
		}
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return usernameField.getText();
	}

	/**
	 * @return the password
	 */
	public char[] getPassword() {
		return passwordField.getPassword();
	}

	/**
	 * @return was this dialog box cancelled?
	 */
	public boolean isCancelled() {
		return cancelled;
	}
}
