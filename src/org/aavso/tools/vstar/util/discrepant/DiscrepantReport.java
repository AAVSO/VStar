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
package org.aavso.tools.vstar.util.discrepant;

import java.text.DecimalFormat;
import java.util.Calendar;

import org.aavso.tools.vstar.util.date.AbstractDateUtil;
import org.aavso.tools.vstar.util.locale.NumberParser;

/**
 * An instance of this class represents a discrepant observation report.
 */
public class DiscrepantReport {

	private static final DecimalFormat dateFormat = new DecimalFormat("0.0###");

	private String auid;
	private String name;
	private int uniqueId;
	private String editor;
	private String comments;
	private double jd;

	/**
	 * Constructor.
	 * 
	 * @param auid
	 *            The AAVSO unique ID.
	 * @param name
	 *            The name of the object.
	 * @param uniqueId
	 *            The unique ID associated with the original observation.
	 * @param editor
	 *            The editor (AAVSO or Citizen Sky user name).
	 * @param comments
	 *            Optional comments supplied by the editor (reporter).
	 */
	public DiscrepantReport(String auid, String name, int uniqueId,
			String editor, String comments) {
		super();
		this.auid = auid;
		this.name = name;
		this.uniqueId = uniqueId;
		this.editor = editor;
		this.comments = comments;
		this.jd = getTodaysJD();
	}

	// Getters

	/**
	 * @return the auid
	 */
	public String getAuid() {
		return auid;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the uniqueId
	 */
	public int getUniqueId() {
		return uniqueId;
	}

	/**
	 * @return the editor
	 */
	public String getEditor() {
		return editor;
	}

	/**
	 * @return the comments
	 */
	public String getComments() {
		return comments;
	}

	/**
	 * @return the report JD
	 */
	public double getJd() {
		return jd;
	}
	
	public String toString() {
		String report = "";

		report += "AUID: " + auid;
		report += "\nName: " + name;
		report += "\nUnique ID: " + uniqueId;
		report += "\nEditor: " + editor;
		report += "\nComments: " + comments;
		report += "\nReport Date: " + jd;

		return report;
	}

	// Helpers

	private static double getTodaysJD() {
		Calendar today = Calendar.getInstance();
		double day = NumberParser.parseDouble(dateFormat.format(today
				.get(Calendar.DAY_OF_MONTH)
				+ (double) today.get(Calendar.HOUR_OF_DAY)
				/ 24
				+ (double) today.get(Calendar.MINUTE) / 1440));

		return AbstractDateUtil.getInstance().calendarToJD(
				today.get(Calendar.YEAR), today.get(Calendar.MONTH) + 1, day);
	}
}
