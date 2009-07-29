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
package org.aavso.tools.vstar.input.database;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.ObservationRetrieverBase;
import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.MenuBar;
import org.aavso.tools.vstar.ui.MessageBox;

/**
 * This class reads variable star observations from an AAVSO database and yields
 * a collection of observations for one star.
 * 
 * REQ_VSTAR_AAVSO_DATABASE_READ REQ_VSTAR_DATABASE_READ_ONLY
 */
public class AAVSODatabaseObservationReader extends ObservationRetrieverBase {

	private ResultSet source;

	/**
	 * Constructor.
	 * 
	 * @param source
	 *            A SQL result set that is the source of observations.
	 */
	public AAVSODatabaseObservationReader(ResultSet source) {
		this.source = source;
	}

	/**
	 * @see org.aavso.tools.vstar.input.ObservationRetrieverBase#retrieveObservations()
	 */
	public void retrieveObservations() throws ObservationReadError {
		try {
			while (source.next()) {
				// TODO: create obs list
			}
		} catch (SQLException e) {
			MessageBox.showErrorDialog(MainFrame.getInstance(),
					MenuBar.NEW_STAR_FROM_DATABASE, e);
		}
	}
}
