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
package org.aavso.tools.vstar.ui.mediator.message;

import java.util.List;

import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisDatum;

/**
 * Messages of this type are sent when a harmonic search result needs to be
 * propagated.
 */
public class HarmonicSearchResultMessage extends MessageBase {

	private List<Harmonic> harmonics;
	private IPeriodAnalysisDatum dataPoint;
	private double tolerance;
	private String iDstring = null;
	
	/**
	 * Constructor
	 * 
	 * @param source
	 *            The source (sender) of the message.
	 * @param harmonics The harmonics found in the search.
	 * @param dataPoint The datapoint associated with the first harmonic.
	 */
	public HarmonicSearchResultMessage(Object source, List<Harmonic> harmonics,
			IPeriodAnalysisDatum dataPoint, double tolerance) {
		super(source);

		this.harmonics = harmonics;
		this.dataPoint = dataPoint;
		this.tolerance = tolerance;
	}

	/**
	 * @return the harmonics
	 */
	public List<Harmonic> getHarmonics() {
		return harmonics;
	}

	/**
	 * @return the dataPoint
	 */
	public IPeriodAnalysisDatum getDataPoint() {
		return dataPoint;
	}
	
	public void setIDstring(String s) {
		iDstring = s;
	}
	
	public String getIDstring() {
		return iDstring; 
	}

	public double getTolerance() {
		return tolerance;
	}

}
