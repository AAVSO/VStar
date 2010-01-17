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
package org.aavso.tools.vstar.ui.model.plot;

import java.util.List;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.ValidObservation;

/**
 * A Julian Date element source/sink.
 */
public class JDTimeElementEntity implements ITimeElementEntity {

	private static final int DEFAULT_BIN_DAYS = 20;

	public static final JDTimeElementEntity instance = new JDTimeElementEntity();

	public double getTimeElement(List<ValidObservation> obs, int index) {
		return obs.get(index).getJD();
	}

	public void setTimeElement(ValidObservation ob, double meanJD) {
		ob.setDateInfo(new DateInfo(meanJD));
	}

	public double getDefaultTimeElementsInBin() {
		return DEFAULT_BIN_DAYS;
	}

	public double getDefaultTimeIncrements() {
		return 1;
	}
}
