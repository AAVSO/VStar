/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2026  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.util.prefs;

import java.util.Locale;

import org.quicktheories.WithQuickTheories;

import junit.framework.TestCase;

/**
 * Unit and property-based tests for {@link NumericPrecisionPrefs} formatting.
 */
public class NumericPrecisionPrefsTest extends TestCase implements WithQuickTheories {

	private Locale savedLocale;

	public NumericPrecisionPrefsTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		savedLocale = Locale.getDefault();
		Locale.setDefault(Locale.US);
		NumericPrecisionPrefs.setDefaultDecimalPlacePrefs();
		NumericPrecisionPrefs.clearHashMaps();
	}

	@Override
	protected void tearDown() throws Exception {
		NumericPrecisionPrefs.setDefaultDecimalPlacePrefs();
		NumericPrecisionPrefs.clearHashMaps();
		Locale.setDefault(savedLocale);
		super.tearDown();
	}

	public void testDefaultDecimalPlaces() {
		assertEquals(5, NumericPrecisionPrefs.getTimeDecimalPlaces());
		assertEquals(6, NumericPrecisionPrefs.getMagDecimalPlaces());
		assertEquals(12, NumericPrecisionPrefs.getOtherDecimalPlaces());
	}

	public void testSetMagDecimalPlacesAffectsFormat() {
		NumericPrecisionPrefs.setMagDecimalPlaces(2);
		NumericPrecisionPrefs.clearHashMaps();
		String formatted = NumericPrecisionPrefs.formatMag(1.23456);
		// With max 2 fraction digits, value rounds to 1.23
		assertEquals("1.23", formatted);
	}

	public void testLocaleIndependentUsesEnglishDecimalPoint() {
		Locale.setDefault(Locale.FRANCE);
		NumericPrecisionPrefs.clearHashMaps();
		NumericPrecisionPrefs.setTimeDecimalPlaces(3);
		String independent = NumericPrecisionPrefs.formatTimeLocaleIndependent(1234.5678);
		assertTrue("expected English decimal point: " + independent, independent.contains("."));
		assertFalse("must not use French decimal comma: " + independent, independent.contains(","));
	}

	public void testInputFormatStringLengthTracksPlaces() {
		NumericPrecisionPrefs.setOtherDecimalPlaces(4);
		String pattern = NumericPrecisionPrefs.getOtherInputFormat();
		assertEquals("#.####", pattern);
	}

	public void testFormatCoefUsesScientificWithoutPlusExponent() {
		NumericPrecisionPrefs.setOtherDecimalPlaces(3);
		String s = NumericPrecisionPrefs.formatCoefLocaleIndependent(1.5e15);
		assertTrue(s.toUpperCase(Locale.ENGLISH).contains("E"));
		assertFalse("VeLa workaround should strip E+: " + s, s.contains("E+"));
	}

	/**
	 * Locale-independent mag formatting never introduces a comma decimal
	 * separator for finite values.
	 */
	public void testLocaleIndependentMagNeverUsesCommaProperty() {
		qt().forAll(doubles().between(-100.0, 100.0), integers().between(1, 8)).check((value, places) -> {
			NumericPrecisionPrefs.setMagDecimalPlaces(places);
			NumericPrecisionPrefs.clearHashMaps();
			String s = NumericPrecisionPrefs.formatMagLocaleIndependent(value);
			return !s.contains(",");
		});
	}

	/**
	 * Changing time decimal places updates getTimeDecimalPlaces().
	 */
	public void testTimeDecimalPlacesRoundTripProperty() {
		qt().forAll(integers().between(1, 12)).check(places -> {
			NumericPrecisionPrefs.setTimeDecimalPlaces(places);
			return NumericPrecisionPrefs.getTimeDecimalPlaces() == places;
		});
	}
}
