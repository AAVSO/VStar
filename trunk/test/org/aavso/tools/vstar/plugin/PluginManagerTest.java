/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.plugin;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * PluginManager test cases.
 */
public class PluginManagerTest extends TestCase {

	private PluginManager pluginManager;
	
	public PluginManagerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		pluginManager = new PluginManager();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link org.aavso.tools.vstar.plugin.PluginManager#retrievePluginInfo(java.lang.String)}.
	 */
	public void testRetrievePluginInfo() {
		pluginManager.retrievePluginInfo(PluginManager.DEFAULT_PLUGIN_BASE_URL_STR);
		Map<String, URL> plugins = pluginManager.getPlugins();
		assertEquals(9, plugins.size());
		Map<String, URL> libs = pluginManager.getLibs();
		assertEquals(2, libs.size());
	}

	/**
	 * Test method for {@link org.aavso.tools.vstar.plugin.PluginManager#installPlugins(java.util.Set)}.
	 */
	public void testInstallPlugins() throws IOException {
		pluginManager.retrievePluginInfo(PluginManager.DEFAULT_PLUGIN_BASE_URL_STR);
		Set<String> descs = new HashSet<String>();
		descs.addAll(pluginManager.getDescriptions().keySet());
		assertTrue(pluginManager.installPlugins(descs));
	}
}
