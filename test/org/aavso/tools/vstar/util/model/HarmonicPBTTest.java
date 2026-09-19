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
package org.aavso.tools.vstar.util.model;

import org.quicktheories.WithQuickTheories;

import junit.framework.TestCase;

/**
 * Property-based tests for {@link Harmonic} frequency / period / harmonic-number
 * relationships.
 */
public class HarmonicPBTTest extends TestCase implements WithQuickTheories {

	public HarmonicPBTTest(String name) {
		super(name);
	}

	/**
	 * period * frequency == 1 for any positive frequency.
	 */
	public void testPeriodFrequencyReciprocalProperty() {
		qt().forAll(doubles().between(1e-6, 1e3)).check(freq -> {
			Harmonic h = new Harmonic(freq);
			return Math.abs(h.getPeriod() * h.getFrequency() - 1.0) < 1e-9;
		});
	}

	/**
	 * getFundamentalFrequency() * harmonicNumber == frequency.
	 */
	public void testFundamentalTimesHarmonicNumberProperty() {
		qt().forAll(doubles().between(1e-4, 10.0), integers().between(1, 20)).check((fund, n) -> {
			double freq = fund * n;
			Harmonic h = new Harmonic(freq, n);
			return Math.abs(h.getFundamentalFrequency() - fund) < 1e-9
					&& Math.abs(h.getFrequency() - fund * n) < 1e-9;
		});
	}

	/**
	 * Harmonic number 1 is always reported as fundamental.
	 */
	public void testHarmonicOneIsFundamentalProperty() {
		qt().forAll(doubles().between(1e-6, 1e3)).check(freq -> new Harmonic(freq, 1).isFundamental());
	}
}
