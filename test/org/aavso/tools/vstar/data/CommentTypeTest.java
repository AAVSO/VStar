package org.aavso.tools.vstar.data;

import junit.framework.TestCase;

public class CommentTypeTest extends TestCase {

	public CommentTypeTest(String name) {
		super(name);
	}

	public void testGetTypeFromFlagB() {
		assertSame(CommentType.SKY_BRIGHT, CommentType.getTypeFromFlag("B"));
	}

	public void testGetTypeFromFlagU() {
		assertSame(CommentType.CLOUDS, CommentType.getTypeFromFlag("U"));
	}

	public void testGetTypeFromFlagW() {
		assertSame(CommentType.POOR_SEEING, CommentType.getTypeFromFlag("W"));
	}

	public void testGetTypeFromFlagL() {
		assertSame(CommentType.LOW_IN_SKY, CommentType.getTypeFromFlag("L"));
	}

	public void testGetTypeFromFlagD() {
		assertSame(CommentType.UFOS, CommentType.getTypeFromFlag("D"));
	}

	public void testGetTypeFromFlagY() {
		assertSame(CommentType.OUTBURST, CommentType.getTypeFromFlag("Y"));
	}

	public void testGetTypeFromFlagK() {
		assertSame(CommentType.NON_AAVSO_CHART, CommentType.getTypeFromFlag("K"));
	}

	public void testGetTypeFromFlagS() {
		assertSame(CommentType.COMP_SEQ_PROBLEM, CommentType.getTypeFromFlag("S"));
	}

	public void testGetTypeFromFlagZ() {
		assertSame(CommentType.MAG_UNCERTAIN, CommentType.getTypeFromFlag("Z"));
	}

	public void testGetTypeFromFlagI() {
		assertSame(CommentType.IDENT_UNCERTAIN, CommentType.getTypeFromFlag("I"));
	}

	public void testGetTypeFromFlagV() {
		assertSame(CommentType.FAINT_STAR, CommentType.getTypeFromFlag("V"));
	}

	public void testGetTypeFromFlagA() {
		assertSame(CommentType.AAVSO_ATLAS, CommentType.getTypeFromFlag("A"));
	}

	public void testGetTypeFromFlagF() {
		assertSame(CommentType.UNCONVENTIONAL, CommentType.getTypeFromFlag("F"));
	}

	public void testGetTypeFromFlagG() {
		assertSame(CommentType.NON_AAVSO_GUIDE_STAR_CAT, CommentType.getTypeFromFlag("G"));
	}

	public void testGetTypeFromFlagH() {
		assertSame(CommentType.HAZE, CommentType.getTypeFromFlag("H"));
	}

	public void testGetTypeFromFlagJ() {
		assertSame(CommentType.NON_AAVSO_HIPPARCOS, CommentType.getTypeFromFlag("J"));
	}

	public void testGetTypeFromFlagM() {
		assertSame(CommentType.MOON, CommentType.getTypeFromFlag("M"));
	}

	public void testGetTypeFromFlagN() {
		assertSame(CommentType.ANGLE, CommentType.getTypeFromFlag("N"));
	}

	public void testGetTypeFromFlagO() {
		assertSame(CommentType.OTHER, CommentType.getTypeFromFlag("O"));
	}

	public void testGetTypeFromFlagP() {
		assertSame(CommentType.MAG_FROM_STEP_MAG, CommentType.getTypeFromFlag("P"));
	}

	public void testGetTypeFromFlagQ() {
		assertSame(CommentType.QUESTIONED_BY_HQ, CommentType.getTypeFromFlag("Q"));
	}

	public void testGetTypeFromFlagR() {
		assertSame(CommentType.COLOR_COMMENT, CommentType.getTypeFromFlag("R"));
	}

	public void testGetTypeFromFlagT() {
		assertSame(CommentType.NON_AAVSO_TYCHO, CommentType.getTypeFromFlag("T"));
	}

	public void testGetTypeFromFlagX() {
		assertSame(CommentType.REJECTED, CommentType.getTypeFromFlag("X"));
	}

	public void testGetTypeFromUnknown() {
		assertSame(CommentType.OTHER, CommentType.getTypeFromFlag("9"));
	}

	public void testGetCommentFlagRoundTrip() {
		for (CommentType t : CommentType.values()) {
			assertSame(t, CommentType.getTypeFromFlag(t.getCommentFlag()));
		}
	}

	public void testGetRegex() {
		assertEquals(".", CommentType.getRegex());
	}
}
