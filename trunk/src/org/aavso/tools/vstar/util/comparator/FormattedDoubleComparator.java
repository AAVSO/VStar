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
package org.aavso.tools.vstar.util.comparator;

import java.util.Comparator;

import org.aavso.tools.vstar.util.locale.NumberParser;

/**
 * This comparator takes strings, parsing them as doubles, and compares them.
 */
public class FormattedDoubleComparator implements Comparator<String> {

	private final static FormattedDoubleComparator instance = new FormattedDoubleComparator();

	/**
	 * Singleton constructor.
	 */
	private FormattedDoubleComparator() {
	}

	/**
	 * @return the instance
	 */
	public static FormattedDoubleComparator getInstance() {
		return instance;
	}

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(String s1, String s2) {
		return Double.compare(NumberParser.parseDouble(s1), NumberParser
				.parseDouble(s2));
	}
}
