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

import java.util.List;

import javax.swing.SwingWorker;

/**
 * This concurrent task attempts to authenticate against one or more
 * authentication sources, completing when one is successful or all are
 * exhausted.
 */
public class AuthenticationTask extends SwingWorker<Boolean, Void> {

	private String usernmame;
	private String password;
	private List<IAuthenticationSource> authenticators;

	/**
	 * Constructor.
	 * 
	 * @param authenticators
	 *            A list of authentication sources to try.
	 * @param usernmame
	 *            The username to be used in authentication.
	 * @param password
	 *            The password to be used in authentication.
	 */
	public AuthenticationTask(List<IAuthenticationSource> authenticators,
			String usernmame, String password) {
		this.authenticators = authenticators;
		this.usernmame = usernmame;
		this.password = password;
	}

	/**
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected Boolean doInBackground() throws Exception {

		boolean authenticated = false;

		// Try each authentication source in turn.
		for (IAuthenticationSource authSource : authenticators) {
			// MainFrame.getInstance().getStatusPane()
			// .setMessage(
			// "Checking " + authSource.getLoginType()
			// + " credentials...");

			authenticated = authSource.authenticate(usernmame, password);

			if (authenticated) {
				break;
			}
		}
		
		return authenticated;
	}

	/**
	 * Executed in event dispatching thread.
	 */
	public void done() {
		// TODO: how to detect task cancellation?
	}
}
