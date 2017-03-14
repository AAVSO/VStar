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

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * The purpose of this class is to provide locale-specific strings and
 * preference handling.
 */
public class LocaleProps {

	private final static String LOCALE_PREFS_KEY = "locale";

	// Default locale when JVM (and this class) first loads.
	private static final Locale defaultLocale = Locale.getDefault();

	private static ResourceBundle localeResourceBundle = null;

	private static Preferences prefs;
	private static Locale locale;

	static {
		// Create preferences node for locale and get the current locale or the
		// current JVM instance's locale if none is set.
		try {
			prefs = Preferences.userNodeForPackage(LocaleProps.class);
			
			String languageTag = prefs.get(LOCALE_PREFS_KEY,
					defaultLocale.toLanguageTag());
			
			locale = Locale.forLanguageTag(languageTag);
			Locale.setDefault(locale);
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}

	public static void setLocalePref(Locale newLocale) {
		locale = newLocale;
		Locale.setDefault(locale);
		
		try {
			String languageTag = newLocale.toLanguageTag();
			prefs.put(LOCALE_PREFS_KEY, languageTag);
			prefs.flush();
		} catch (Throwable t) {
			// We need VStar to function in the absence of prefs.
		}
	}

	public static void setDefaultLocalePref() {
		setLocalePref(defaultLocale);
	}

	/**
	 * @return the locale
	 */
	public static Locale getLocale() {
		return locale;
	}

	/**
	 * Return the localised string given the specified ID.
	 * 
	 * @param id
	 *            The ID of the localised string.
	 * @return The localised string.
	 */
	public static String get(String id) {
		try {
			if (localeResourceBundle == null) {
				// For normal running from distribution jar.
				localeResourceBundle = ResourceBundle.getBundle(
						"locale.strings");
			}
		} catch (Exception e) {
			try {
				// For Eclipse run or other scenario in which bundle is to be
				// taken
				// from normal classpath. Note: Probably should unify these two
				// cases by
				// constructing the distribution jar to use full package below.
				localeResourceBundle = ResourceBundle.getBundle(
						"org.aavso.tools.vstar.ui.resources.locale.strings");
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return localeResourceBundle.getString(id);
	}
}
