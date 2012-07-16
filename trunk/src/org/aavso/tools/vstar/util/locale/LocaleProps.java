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
package org.aavso.tools.vstar.util.locale;

import java.util.ResourceBundle;

/**
 * The purpose of this class is to provide locale-specific strings.
 */
public class LocaleProps {

	private static ResourceBundle localeResourceBundle = null;

	/**
	 * Return the localised string given the specified ID.
	 * 
	 * @param id
	 *            The ID of the localised string.
	 * @return The localised string.
	 */
	public static String getString(String id) {
		if (localeResourceBundle == null) {
			localeResourceBundle = ResourceBundle
					.getBundle("org.aavso.tools.vstar.ui.resources.locale.strings");
		}
		return localeResourceBundle.getString(id);
	}
}
