package org.aavso.tools.vstar.util.notification;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class NotifierTest extends TestCase {

	public NotifierTest(String name) {
		super(name);
	}

	private static class TestListener implements Listener<String> {
		final List<String> received = new ArrayList<String>();
		boolean canRemove = true;

		public void update(String info) {
			received.add(info);
		}

		public boolean canBeRemoved() {
			return canRemove;
		}
	}

	public void testAddListenerAndNotify() {
		Notifier<String> n = new Notifier<String>();
		TestListener L = new TestListener();
		n.addListener(L);
		n.notifyListeners("hello");
		assertEquals(1, L.received.size());
		assertEquals("hello", L.received.get(0));
	}

	public void testMultipleListeners() {
		Notifier<String> n = new Notifier<String>();
		TestListener a = new TestListener();
		TestListener b = new TestListener();
		n.addListener(a);
		n.addListener(b);
		n.notifyListeners("x");
		assertEquals(1, a.received.size());
		assertEquals(1, b.received.size());
		assertEquals("x", a.received.get(0));
		assertEquals("x", b.received.get(0));
	}

	public void testRemoveWillingListener() {
		Notifier<String> n = new Notifier<String>();
		TestListener L = new TestListener();
		L.canRemove = true;
		n.addListener(L);
		n.removeListenerIfWilling(L);
		n.notifyListeners("after");
		assertTrue(L.received.isEmpty());
	}

	public void testRemoveUnwillingListener() {
		Notifier<String> n = new Notifier<String>();
		TestListener L = new TestListener();
		L.canRemove = false;
		n.addListener(L);
		n.removeListenerIfWilling(L);
		n.notifyListeners("still");
		assertEquals(1, L.received.size());
		assertEquals("still", L.received.get(0));
	}

	public void testMessageReplay() {
		Notifier<String> n = new Notifier<String>();
		n.notifyListeners("A");
		n.notifyListeners("B");
		TestListener L = new TestListener();
		n.addListener(L, true);
		assertEquals(2, L.received.size());
		assertEquals("A", L.received.get(0));
		assertEquals("B", L.received.get(1));
	}

	public void testNoReplayByDefault() {
		Notifier<String> n = new Notifier<String>();
		n.notifyListeners("A");
		TestListener L = new TestListener();
		n.addListener(L, false);
		assertTrue(L.received.isEmpty());
	}

	public void testCleanup() {
		Notifier<String> n = new Notifier<String>();
		TestListener L = new TestListener();
		L.canRemove = true;
		n.addListener(L);
		n.notifyListeners("gone");
		n.cleanup();
		TestListener replay = new TestListener();
		n.addListener(replay, true);
		assertTrue(replay.received.isEmpty());
	}

	public void testAddSameListenerTwice() {
		Notifier<String> n = new Notifier<String>();
		TestListener L = new TestListener();
		n.addListener(L);
		n.addListener(L);
		n.notifyListeners("once");
		assertEquals(1, L.received.size());
		assertEquals("once", L.received.get(0));
	}
}
