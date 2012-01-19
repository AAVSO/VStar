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
package org.aavso.tools.vstar.input.ws.client;

import java.util.List;

/**
 * Example of using AAVSO observation web service.
 */
public class AAVSOObsWebServiceClientApp {

	public static void main(String[] args) {
		AAVSOObsWebServiceService service = new AAVSOObsWebServiceService();

		AAVSOObsWebService proxy = service.getAAVSOObsWebServicePort();

		try {
			// eps Aur info.
			StarInfo info = proxy.getStarInfoByName("eps Aur");
			System.out.println(info.getDesignation());
			System.out.println(info.getAuid());
			System.out.println(info.getPeriod());
			System.out.println(info.getVarType());

			// eps Aur observations in range 2454000..2454010.

			// System.out.println("getObservationsWithJDRange()");
			// List<ValidObservation> obs1 = proxy.getObservationsWithJDRange(
			// info, 2454000.0, 2454010.0);
			//
			// for (ValidObservation ob : obs1) {
			// System.out.println(ob.getDateInfo());
			// System.out.println(ob.getMagnitude());
			// }

			System.out.println("getObservationsInRange()");
			List<Ob> obs2 = proxy.getObservationsInRange(info,
					2454000.0, 2454010.0);

			for (Ob ob : obs2) {
				System.out.print(ob.getJd() + ", ");
				System.out.print(ob.getMagnitude() + ", ");
				System.out.println(ob.getUncertainty());
			}
		} catch (ConnectionException_Exception e) {
			System.err.println("Connection error.");
		} catch (UnknownStarError_Exception e) {
			System.err.println("Unknown star.");
		} catch (ObservationReadError_Exception e) {
			System.err.println("Unknown star.");
		}
	}
}
