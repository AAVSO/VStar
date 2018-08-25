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

import org.aavso.tools.vstar.plugin.ob.src.impl.AIDWebServiceCSVObservationSourcePlugin;

/**
 * Unit (or integration) class for tests that read AID
 * observations via the VSX web service using the CSV response method.
 */
public class VSXWebServiceAIDCSVObservationReaderTest extends
		VSXWebServiceAIDObservationReaderTestBase {

	public VSXWebServiceAIDCSVObservationReaderTest() {
		super("AID CSV reader test",
				AIDWebServiceCSVObservationSourcePlugin.class);
	}
}
