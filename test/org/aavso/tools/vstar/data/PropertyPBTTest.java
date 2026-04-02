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
package org.aavso.tools.vstar.data;

import junit.framework.TestCase;

import org.quicktheories.WithQuickTheories;
import org.quicktheories.core.Gen;

/**
 * Property-based tests for the Property class, verifying algebraic contracts
 * of equals, hashCode, and compareTo.
 *
 * These properties are the Java equivalents of the axioms that a formal
 * verification (e.g. in KeY or Lean) would prove unconditionally.
 */
public class PropertyPBTTest extends TestCase implements WithQuickTheories {

	public PropertyPBTTest(String name) {
		super(name);
	}

	// -- Generators for Property instances --

	private Gen<Property> intProperties() {
		return integers().all().map(Property::new);
	}

	private Gen<Property> realProperties() {
		return doubles().between(-1e15, 1e15).map(Property::new);
	}

	private Gen<Property> boolProperties() {
		return booleans().all().map(Property::new);
	}

	private Gen<Property> stringProperties() {
		return strings().basicLatinAlphabet().ofLengthBetween(0, 20)
				.map(Property::new);
	}

	// -- equals: reflexivity --

	public void testEqualsReflexiveIntProperty() {
		qt().forAll(intProperties()).check(p -> p.equals(p));
	}

	public void testEqualsReflexiveRealProperty() {
		qt().forAll(realProperties()).check(p -> p.equals(p));
	}

	public void testEqualsReflexiveBoolProperty() {
		qt().forAll(boolProperties()).check(p -> p.equals(p));
	}

	public void testEqualsReflexiveStringProperty() {
		qt().forAll(stringProperties()).check(p -> p.equals(p));
	}

	// -- equals: symmetry (via two identical constructions) --

	public void testEqualsSymmetricIntProperty() {
		qt().forAll(integers().all()).check(v -> {
			Property a = new Property(v);
			Property b = new Property(v);
			return a.equals(b) == b.equals(a);
		});
	}

	public void testEqualsSymmetricRealProperty() {
		qt().forAll(doubles().between(-1e15, 1e15)).check(v -> {
			Property a = new Property(v);
			Property b = new Property(v);
			return a.equals(b) == b.equals(a);
		});
	}

	public void testEqualsSymmetricStringProperty() {
		qt().forAll(strings().basicLatinAlphabet().ofLengthBetween(0, 20))
				.check(v -> {
					Property a = new Property(v);
					Property b = new Property(v);
					return a.equals(b) == b.equals(a);
				});
	}

	// -- hashCode: consistency with equals --

	public void testHashCodeConsistentWithEqualsIntProperty() {
		qt().forAll(integers().all()).check(v -> {
			Property a = new Property(v);
			Property b = new Property(v);
			return !a.equals(b) || a.hashCode() == b.hashCode();
		});
	}

	public void testHashCodeConsistentWithEqualsRealProperty() {
		qt().forAll(doubles().between(-1e15, 1e15)).check(v -> {
			Property a = new Property(v);
			Property b = new Property(v);
			return !a.equals(b) || a.hashCode() == b.hashCode();
		});
	}

	public void testHashCodeConsistentWithEqualsStringProperty() {
		qt().forAll(strings().basicLatinAlphabet().ofLengthBetween(0, 20))
				.check(v -> {
					Property a = new Property(v);
					Property b = new Property(v);
					return !a.equals(b) || a.hashCode() == b.hashCode();
				});
	}

	// -- compareTo: antisymmetry (sgn(a.compareTo(b)) == -sgn(b.compareTo(a))) --

	public void testCompareToAntisymmetricIntProperty() {
		qt().forAll(integers().all(), integers().all()).check((v1, v2) -> {
			Property a = new Property(v1);
			Property b = new Property(v2);
			return Integer.signum(a.compareTo(b)) == -Integer
					.signum(b.compareTo(a));
		});
	}

	public void testCompareToAntisymmetricRealProperty() {
		qt().forAll(doubles().between(-1e15, 1e15),
				doubles().between(-1e15, 1e15)).check((v1, v2) -> {
			Property a = new Property(v1);
			Property b = new Property(v2);
			return Integer.signum(a.compareTo(b)) == -Integer
					.signum(b.compareTo(a));
		});
	}

	public void testCompareToAntisymmetricStringProperty() {
		qt().forAll(strings().basicLatinAlphabet().ofLengthBetween(0, 20),
				strings().basicLatinAlphabet().ofLengthBetween(0, 20))
				.check((v1, v2) -> {
					Property a = new Property(v1);
					Property b = new Property(v2);
					return Integer.signum(a.compareTo(b)) == -Integer
							.signum(b.compareTo(a));
				});
	}

	// -- compareTo: consistency with equals --

	public void testCompareToConsistentWithEqualsIntProperty() {
		qt().forAll(integers().all()).check(v -> {
			Property a = new Property(v);
			Property b = new Property(v);
			return a.equals(b) && a.compareTo(b) == 0;
		});
	}

	public void testCompareToConsistentWithEqualsRealProperty() {
		qt().forAll(doubles().between(-1e15, 1e15)).check(v -> {
			Property a = new Property(v);
			Property b = new Property(v);
			return a.equals(b) && a.compareTo(b) == 0;
		});
	}

	public void testCompareToConsistentWithEqualsStringProperty() {
		qt().forAll(strings().basicLatinAlphabet().ofLengthBetween(0, 20))
				.check(v -> {
					Property a = new Property(v);
					Property b = new Property(v);
					return a.equals(b) && a.compareTo(b) == 0;
				});
	}

	// -- compareTo: transitivity --

	public void testCompareToTransitiveIntProperty() {
		qt().forAll(integers().all(), integers().all(), integers().all())
				.check((v1, v2, v3) -> {
					Property a = new Property(v1);
					Property b = new Property(v2);
					Property c = new Property(v3);
					if (a.compareTo(b) <= 0 && b.compareTo(c) <= 0) {
						return a.compareTo(c) <= 0;
					}
					return true;
				});
	}

	// -- equals: null safety --

	public void testEqualsNullReturnsFalseIntProperty() {
		qt().forAll(intProperties()).check(p -> !p.equals(null));
	}

	// -- NO_VALUE sentinel --

	public void testNoValueEquality() {
		assertTrue(Property.NO_VALUE.equals(new Property()));
		assertEquals(Property.propType.NONE, Property.NO_VALUE.getType());
	}
}
