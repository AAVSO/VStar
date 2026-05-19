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
package org.aavso.tools.vstar.external.lib;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.EpochType;
import org.aavso.tools.vstar.util.coords.RAInfo;

import com.sun.net.httpserver.HttpServer;

import junit.framework.TestCase;

/**
 * Unit tests for {@link ConvertHelper}.
 */
public class ConvertHelperTest extends TestCase {

	private String savedLocalServiceUrl;

	public ConvertHelperTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() {
		savedLocalServiceUrl = ConvertHelper.localServiceURLstring;
	}

	@Override
	protected void tearDown() {
		ConvertHelper.localServiceURLstring = savedLocalServiceUrl;
	}

	public void testGetCoordinatesWhenPresent() {
		RAInfo ra = new RAInfo(EpochType.J2000, 45.0);
		DecInfo dec = new DecInfo(EpochType.J2000, 30.0);
		StarInfo info = new StarInfo(null, "Test Star", null, null, null, null, null, null, ra, dec, null);

		Pair<RAInfo, DecInfo> coords = ConvertHelper.getCoordinates(info);

		assertNotNull(coords);
		assertSame(ra, coords.first);
		assertSame(dec, coords.second);
	}

	public void testAstroutilsUrlTemplate() {
		assertTrue(ConvertHelper.URL_TEMPLATE.startsWith(ConvertHelper.ASTROUTILS_URL));
		assertTrue(ConvertHelper.URL_TEMPLATE.contains("FUNCTION=%s"));
	}

	public void testGetConvertedListOfTimesLocalService() throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		int port = server.getAddress().getPort();
		server.createContext("/convert", exchange -> {
			byte[] response = "{\"bjd_tdb\":[2458001.5,2458002.5]}".getBytes("UTF-8");
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});
		server.start();
		try {
			ConvertHelper.localServiceURLstring = "http://127.0.0.1:" + port + "/convert";
			List<Double> times = Arrays.asList(2458000.0, 2458001.0);
			List<Double> out = ConvertHelper.getConvertedListOfTimes(times, 45.0, 30.0, "utc2bjd");
			assertEquals(2, out.size());
			assertEquals(2458001.5, out.get(0), 1e-9);
			assertEquals(2458002.5, out.get(1), 1e-9);
		} finally {
			server.stop(0);
		}
	}
}
