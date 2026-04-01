/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2016  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.plugin.ob.src.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class UTF8FilteringInputStreamTest extends TestCase {

	public UTF8FilteringInputStreamTest(String name) {
		super(name);
	}

	public void testNormalAscii() throws IOException {
		UTF8FilteringInputStream in = new UTF8FilteringInputStream(
				new ByteArrayInputStream("hello".getBytes("US-ASCII")));
		assertEquals('h', in.read());
		assertEquals('e', in.read());
		assertEquals('l', in.read());
		assertEquals('l', in.read());
		assertEquals('o', in.read());
	}

	public void testTabPassesThrough() throws IOException {
		UTF8FilteringInputStream in = new UTF8FilteringInputStream(
				new ByteArrayInputStream(new byte[] { 0x9 }));
		assertEquals(0x9, in.read());
	}

	public void testLFPassesThrough() throws IOException {
		UTF8FilteringInputStream in = new UTF8FilteringInputStream(
				new ByteArrayInputStream(new byte[] { 0xa }));
		assertEquals(0xa, in.read());
	}

	public void testCRPassesThrough() throws IOException {
		UTF8FilteringInputStream in = new UTF8FilteringInputStream(
				new ByteArrayInputStream(new byte[] { 0xd }));
		assertEquals(0xd, in.read());
	}

	public void testNullByteBecomesSpace() throws IOException {
		UTF8FilteringInputStream in = new UTF8FilteringInputStream(
				new ByteArrayInputStream(new byte[] { 0x0 }));
		assertEquals(0x20, in.read());
	}

	public void testControlCharBecomesSpace() throws IOException {
		UTF8FilteringInputStream in = new UTF8FilteringInputStream(
				new ByteArrayInputStream(new byte[] { 0x1 }));
		assertEquals(0x20, in.read());
	}

	public void testBellBecomesSpace() throws IOException {
		UTF8FilteringInputStream in = new UTF8FilteringInputStream(
				new ByteArrayInputStream(new byte[] { 0x7 }));
		assertEquals(0x20, in.read());
	}

	public void testFormFeedBecomesSpace() throws IOException {
		UTF8FilteringInputStream in = new UTF8FilteringInputStream(
				new ByteArrayInputStream(new byte[] { 0xc }));
		assertEquals(0x20, in.read());
	}

	public void testSpacePassesThrough() throws IOException {
		UTF8FilteringInputStream in = new UTF8FilteringInputStream(
				new ByteArrayInputStream(new byte[] { 0x20 }));
		assertEquals(0x20, in.read());
	}

	public void testEOFPassesThrough() throws IOException {
		UTF8FilteringInputStream in = new UTF8FilteringInputStream(
				new ByteArrayInputStream(new byte[0]));
		assertEquals(-1, in.read());
	}

	public void testMixedContent() throws IOException {
		byte[] raw = new byte[] { 'a', 0x1, 0x9, 'b', 0xa, 'c' };
		UTF8FilteringInputStream in = new UTF8FilteringInputStream(
				new ByteArrayInputStream(raw));
		assertEquals('a', in.read());
		assertEquals(0x20, in.read());
		assertEquals(0x9, in.read());
		assertEquals('b', in.read());
		assertEquals(0xa, in.read());
		assertEquals('c', in.read());
		assertEquals(-1, in.read());
	}
}
