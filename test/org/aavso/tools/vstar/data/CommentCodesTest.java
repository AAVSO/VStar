package org.aavso.tools.vstar.data;

import junit.framework.TestCase;

public class CommentCodesTest extends TestCase {

	public CommentCodesTest(String name) {
		super(name);
	}

	public void testSingleCodeB() {
		CommentCodes cc = new CommentCodes("B");
		assertTrue(cc.getCommentcodes().contains(CommentType.SKY_BRIGHT));
	}

	public void testSingleCodeZ() {
		CommentCodes cc = new CommentCodes("Z");
		assertTrue(cc.getCommentcodes().contains(CommentType.MAG_UNCERTAIN));
	}

	public void testMultipleCodes() {
		CommentCodes cc = new CommentCodes("BZ");
		assertTrue(cc.getCommentcodes().contains(CommentType.SKY_BRIGHT));
		assertTrue(cc.getCommentcodes().contains(CommentType.MAG_UNCERTAIN));
	}

	public void testMultipleCodesWithSpaces() {
		CommentCodes spaced = new CommentCodes("B Z");
		CommentCodes compact = new CommentCodes("BZ");
		assertTrue(spaced.getCommentcodes().contains(CommentType.SKY_BRIGHT));
		assertTrue(spaced.getCommentcodes().contains(CommentType.MAG_UNCERTAIN));
		assertTrue(compact.getCommentcodes().equals(spaced.getCommentcodes()));
	}

	public void testUnknownCodeFallsToOther() {
		CommentCodes cc = new CommentCodes("1");
		assertTrue(cc.getCommentcodes().contains(CommentType.OTHER));
		assertEquals(1, cc.getCommentcodes().size());
	}

	public void testNullInput() {
		CommentCodes cc = new CommentCodes(null);
		assertTrue(cc.getCommentcodes().isEmpty());
		assertEquals("", cc.getOrigString());
	}

	public void testGetOrigString() {
		CommentCodes cc = new CommentCodes("BZ");
		assertEquals("BZ", cc.getOrigString());
	}

	public void testEquals() {
		CommentCodes a = new CommentCodes("BZ");
		CommentCodes b = new CommentCodes("BZ");
		assertTrue(a.equals(b));
	}

	public void testNotEquals() {
		CommentCodes a = new CommentCodes("B");
		CommentCodes b = new CommentCodes("Z");
		assertFalse(a.equals(b));
	}

	public void testHashCodeConsistency() {
		CommentCodes a = new CommentCodes("BZ");
		CommentCodes b = new CommentCodes("BZ");
		assertEquals(a.hashCode(), b.hashCode());
	}
}
