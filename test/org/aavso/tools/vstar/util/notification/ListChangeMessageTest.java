package org.aavso.tools.vstar.util.notification;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class ListChangeMessageTest extends TestCase {

	public ListChangeMessageTest(String name) {
		super(name);
	}

	public void testConstructorWithAllArgs() {
		List<String> src = new ArrayList<String>();
		Object o = "elt";
		ListChangeMessage<String> m = new ListChangeMessage<String>(
				ListChangeType.ADDED_ONE, src, 3, o);
		assertSame(ListChangeType.ADDED_ONE, m.getType());
		assertSame(src, m.getSource());
		assertEquals(3, m.getIndex());
		assertSame(o, m.getObject());
	}

	public void testConstructorWithTypeAndSource() {
		List<String> src = new ArrayList<String>();
		ListChangeMessage<String> m = new ListChangeMessage<String>(
				ListChangeType.CLEARED, src);
		assertSame(ListChangeType.CLEARED, m.getType());
		assertSame(src, m.getSource());
		assertEquals(ListChangeMessage.NONE, m.getIndex());
		assertNull(m.getObject());
	}

	public void testConstructorWithTypeSourceIndex() {
		List<String> src = new ArrayList<String>();
		ListChangeMessage<String> m = new ListChangeMessage<String>(
				ListChangeType.REMOVED, src, 7);
		assertSame(ListChangeType.REMOVED, m.getType());
		assertSame(src, m.getSource());
		assertEquals(7, m.getIndex());
		assertNull(m.getObject());
	}

	public void testConstructorWithTypeSourceObject() {
		List<String> src = new ArrayList<String>();
		Object o = Integer.valueOf(42);
		ListChangeMessage<String> m = new ListChangeMessage<String>(
				ListChangeType.SET, src, o);
		assertSame(ListChangeType.SET, m.getType());
		assertSame(src, m.getSource());
		assertEquals(ListChangeMessage.NONE, m.getIndex());
		assertSame(o, m.getObject());
	}
}
