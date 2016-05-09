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

import java.io.IOException;
import java.io.InputStream;

/**
 * This class filters another input stream, returning any bytes whose ordinal
 * value is less than 0x20 (space) except for tab (0x9), CR (0xd), LF (0xa) as
 * 0x20. This ensures that the stream is UTF-8 compliant for XML 1.0. Note that
 * -1 denotes EOF so this must also be passed through untouched.
 */
public class UTF8FilteringInputStream extends InputStream {

	private InputStream stream;

	public UTF8FilteringInputStream(InputStream stream) {
		this.stream = stream;
	}

	@Override
	public int read() throws IOException {
		int b = stream.read();

		if (b < 0x20) {
			switch (b) {
			case 0x9:
			case 0xa:
			case 0xd:
			case -1:
				break;

			default:
				b = 0x20;
				break;
			}
		}

		return b;
	}
}
