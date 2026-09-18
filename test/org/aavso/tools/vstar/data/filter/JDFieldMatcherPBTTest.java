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
package org.aavso.tools.vstar.data.filter;

import org.aavso.tools.vstar.data.ValidObservation;
import org.quicktheories.WithQuickTheories;
import org.quicktheories.core.Gen;

import junit.framework.TestCase;

/**
 * Property-based tests for JD field matching against comparison operators.
 */
public class JDFieldMatcherPBTTest extends TestCase implements WithQuickTheories {

	public JDFieldMatcherPBTTest(String name) {
		super(name);
	}

	private static ValidObservation obWithJd(double jd) {
		ValidObservation ob = new ValidObservation();
		ob.setJD(jd);
		return ob;
	}

	private static boolean expected(int comparison, ObservationMatcherOp op) {
		switch (op) {
		case EQUALS:
			return comparison == 0;
		case NOT_EQUALS:
			return comparison != 0;
		case LESS_THAN:
			return comparison < 0;
		case GREATER_THAN:
			return comparison > 0;
		case LESS_THAN_OR_EQUAL:
			return comparison <= 0;
		case GREATER_THAN_OR_EQUAL:
			return comparison >= 0;
		default:
			throw new IllegalArgumentException("unsupported op: " + op);
		}
	}

	private Gen<ObservationMatcherOp> numericOps() {
		return arbitrary().pick(ObservationMatcherOp.EQUALS, ObservationMatcherOp.NOT_EQUALS,
				ObservationMatcherOp.LESS_THAN, ObservationMatcherOp.GREATER_THAN,
				ObservationMatcherOp.LESS_THAN_OR_EQUAL, ObservationMatcherOp.GREATER_THAN_OR_EQUAL);
	}

	/**
	 * For any JD, test value, and numeric operator, matches() agrees with
	 * Double.compare semantics used by {@link DoubleFieldMatcher}.
	 */
	public void testMatchesAgreesWithDoubleCompareProperty() {
		qt().forAll(doubles().between(2400000.0, 2500000.0), doubles().between(2400000.0, 2500000.0),
				numericOps()).check((jd, testValue, op) -> {
					JDFieldMatcher matcher = new JDFieldMatcher(testValue, op);
					int comparison = Double.valueOf(jd).compareTo(testValue);
					return matcher.matches(obWithJd(jd)) == expected(comparison, op);
				});
	}

	/**
	 * EQUALS and NOT_EQUALS are complementary for any JD / test-value pair.
	 */
	public void testEqualsAndNotEqualsComplementaryProperty() {
		qt().forAll(doubles().between(2400000.0, 2500000.0), doubles().between(2400000.0, 2500000.0))
				.check((jd, testValue) -> {
					ValidObservation ob = obWithJd(jd);
					boolean eq = new JDFieldMatcher(testValue, ObservationMatcherOp.EQUALS).matches(ob);
					boolean ne = new JDFieldMatcher(testValue, ObservationMatcherOp.NOT_EQUALS).matches(ob);
					return eq != ne;
				});
	}

	/**
	 * LESS_THAN_OR_EQUAL is EQUALS or LESS_THAN.
	 */
	public void testLessThanOrEqualDecompositionProperty() {
		qt().forAll(doubles().between(2400000.0, 2500000.0), doubles().between(2400000.0, 2500000.0))
				.check((jd, testValue) -> {
					ValidObservation ob = obWithJd(jd);
					boolean lte = new JDFieldMatcher(testValue, ObservationMatcherOp.LESS_THAN_OR_EQUAL)
							.matches(ob);
					boolean lt = new JDFieldMatcher(testValue, ObservationMatcherOp.LESS_THAN).matches(ob);
					boolean eq = new JDFieldMatcher(testValue, ObservationMatcherOp.EQUALS).matches(ob);
					return lte == (lt || eq);
				});
	}
}
