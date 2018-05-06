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
package org.aavso.tools.vstar.util;

import java.awt.Desktop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import org.aavso.tools.vstar.ui.dialog.MessageBox;

/**
 * Email utility functions.
 */
public class EmailUtils {

	/**
	 * Create email in default email application, if available.
	 * 
	 * @param mailTo
	 *            The recipient's email address
	 * @param subject
	 *            The subject of the email
	 * @param message
	 *            The body of the email message.
	 */
	public static void createEmailMessage(String mailTo, String subject,
			String message) {
		try {
			String encodedSubject = encode(subject);
			String encodedMessage = encode(message);
			final String mailURIStr = String.format(
					"mailto:%s?subject=%s&body=%s", mailTo, encodedSubject,
					encodedMessage);
			final URI mailURI = new URI(mailURIStr);
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().mail(mailURI);
			}
		} catch (UnsupportedEncodingException e) {
			MessageBox.showErrorDialog("Mail",
					"Cannot create email message (encoding error)");
		} catch (URISyntaxException e) {
			MessageBox.showErrorDialog("Mail",
					"Cannot create email message (URI error)");
		} catch (IOException e) {
			MessageBox.showErrorDialog("Mail",
					"Cannot create email message (Mail application error)");
		}
	}

	// Helpers

	private static String encode(String text)
			throws UnsupportedEncodingException {
		return URLEncoder.encode(text, "UTF-8").replace("+", "%20");
	}
}
