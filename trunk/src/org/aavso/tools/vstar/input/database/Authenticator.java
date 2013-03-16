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
package org.aavso.tools.vstar.input.database;

import java.awt.Cursor;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.aavso.tools.vstar.exception.AuthenticationError;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.ui.dialog.LoginDialog;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;

/**
 * This class is responsible for authenticating against one or more
 * authentication sources, caching results such as whether authentication has
 * happened, user name, observer code.
 * 
 * 
 * generateHexDigest() was adapted from Zapper's UserInfo.encryptPassword()
 */
public class Authenticator {

	private final static Authenticator instance = new Authenticator();

	private boolean authenticated;
	private List<IAuthenticationSource> authenticators;

	/**
	 * Singleton getter.
	 */
	public static Authenticator getInstance() {
		return instance;
	}

	/**
	 * Singleton Constructor.
	 */
	private Authenticator() {
		this.authenticated = false;

		this.authenticators = new ArrayList<IAuthenticationSource>();
		this.authenticators.add(new AAVSOAuthenticationSource());
	}

	/**
	 * Authenticate by prompting the user to enter credentials in a dialog,
	 * throwing an exception upon failure after retries. A manifest reason for
	 * this authentication is to obtain the observer code and validate user name
	 * for discrepant reporting, both of which are assumed to be stored by
	 * concrete authentication sources.
	 */
	public void authenticate() throws CancellationException,
			AuthenticationError, ConnectionException {

		int retries = 3;
		boolean cancelled = false;

		while (!cancelled && !authenticated && retries > 0) {
			Mediator.getUI().getStatusPane().setMessage("Authenticating...");

			LoginDialog loginDialog = new LoginDialog("AAVSO Web Login");

			cancelled = loginDialog.isCancelled();

			if (!cancelled) {
				String username = loginDialog.getUsername();
				String password = new String(loginDialog.getPassword());

				try {
					Mediator.getUI().setCursor(
							Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					AuthenticationTask task = new AuthenticationTask(
							authenticators, username, password);

					task.execute();
					authenticated = task.get();
				} catch (ExecutionException e) {
					throw new AuthenticationError(e.getLocalizedMessage());
				} catch (InterruptedException e) {
					// Nothing to do.
				} finally {
					Mediator.getUI().setCursor(null);
				}

				if (!authenticated) {
					retries--;
				}
			}
		}

		Mediator.getUI().getStatusPane().setMessage("");

		if (cancelled) {
			throw new CancellationException();
		}

		if (!authenticated) {
			throw new AuthenticationError("Unable to authenticate.");
		}
	}

	/**
	 * Generate a string consisting of 2 hex digits per byte of a MD5 message
	 * digest.
	 * 
	 * @param str
	 *            the string to generate a digest from
	 * @return the message digest as hex digits
	 */
	protected static String generateHexDigest(String str) {
		String digest = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset();
			md.update(str.getBytes());
			byte messageDigest[] = md.digest();
			StringBuffer hexString = new StringBuffer();
			for (int byteVal : messageDigest) {
				hexString.append(String.format("%02x", 0xFF & byteVal));
			}
			digest = hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			MessageBox.showErrorDialog(Mediator.getUI().getComponent(),
					"Error generating digest", e);
		}

		return digest;
	}
}
