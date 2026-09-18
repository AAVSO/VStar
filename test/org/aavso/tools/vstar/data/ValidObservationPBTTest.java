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
package org.aavso.tools.vstar.data;

import org.quicktheories.WithQuickTheories;
import org.quicktheories.core.Gen;

import junit.framework.TestCase;

/**
 * Property-based tests for {@link ValidObservation} copy / equals / hashCode
 * contracts on the fields exercised by typical observation construction.
 */
public class ValidObservationPBTTest extends TestCase implements WithQuickTheories {

	public ValidObservationPBTTest(String name) {
		super(name);
	}

	private Gen<ValidObservation> simpleObservations() {
		return doubles().between(2400000.0, 2500000.0)
				.zip(doubles().between(-5.0, 25.0), doubles().between(0.0, 1.0),
						(jd, mag, unc) -> {
							ValidObservation ob = new ValidObservation();
							ob.setJD(jd);
							ob.setMagnitude(new Magnitude(mag, unc));
							ob.setBand(SeriesType.Johnson_V);
							ob.setName("PBT");
							return ob;
						});
	}

	/**
	 * copy() yields an equal observation with distinct magnitude / details
	 * identity (defensive copy).
	 */
	public void testCopyEqualsButDistinctMagnitudeProperty() {
		qt().forAll(simpleObservations()).check(ob -> {
			ValidObservation copy = ob.copy();
			return ob.equals(copy) && ob.hashCode() == copy.hashCode()
					&& ob.getMagnitude() != copy.getMagnitude()
					&& ob.getDetails() != copy.getDetails();
		});
	}

	/**
	 * equals is reflexive for constructed observations.
	 */
	public void testEqualsReflexiveProperty() {
		qt().forAll(simpleObservations()).check(ob -> ob.equals(ob));
	}

	/**
	 * Mutating the copy's magnitude must not change the original, and the two
	 * must then be unequal.
	 */
	public void testMagnitudeMutationIsolatedProperty() {
		qt().forAll(simpleObservations(), doubles().between(-5.0, 25.0), doubles().between(0.0, 1.0))
				.assuming((ob, mag, unc) -> Math.abs(ob.getMag() - mag) > 1e-9
						|| Math.abs(ob.getMagnitude().getUncertainty() - unc) > 1e-9)
				.check((ob, mag, unc) -> {
					ValidObservation copy = ob.copy();
					Magnitude originalMag = ob.getMagnitude();
					copy.setMagnitude(new Magnitude(mag, unc));
					return originalMag.equals(ob.getMagnitude()) && !ob.equals(copy);
				});
	}

	/**
	 * Observations that differ only in JD are not equal.
	 */
	public void testDifferentJdNotEqualProperty() {
		qt().forAll(simpleObservations(), doubles().between(2400000.0, 2500000.0))
				.assuming((ob, otherJd) -> Double.compare(ob.getJD(), otherJd) != 0).check((ob, otherJd) -> {
					ValidObservation other = ob.copy();
					other.setJD(otherJd);
					return !ob.equals(other);
				});
	}
}
