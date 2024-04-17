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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.aavso.tools.vstar.auth.Auth0JSONAutheticationSource;
import org.aavso.tools.vstar.exception.AuthenticationError;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.ui.dialog.AuthCodeLoginDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;

/**
 * This class is responsible for authenticating against one or more
 * authentication sources, caching results such as whether authentication has
 * happened, user name, observer code.
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
		this.authenticators.add(new Auth0JSONAutheticationSource());
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

			AuthCodeLoginDialog loginDialog = new AuthCodeLoginDialog("AAVSO Authentication");

			cancelled = loginDialog.isCancelled();

			if (!cancelled) {
				String uuid = loginDialog.getUUID();
				String code = new String(loginDialog.getCode());

				try {
					Mediator.getUI().setCursor(
							Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					AuthenticationTask task = new AuthenticationTask(
							authenticators, uuid, code);

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
		    Mediator.getUI().setCursor(null);
			throw new CancellationException();
		}

		if (!authenticated) {
			throw new AuthenticationError("Unable to authenticate.");
		}
	}
}
