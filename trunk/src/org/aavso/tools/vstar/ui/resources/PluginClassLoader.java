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
package org.aavso.tools.vstar.ui.resources;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom class loader for VStar plugins.
 * @deprecated
 */
public class PluginClassLoader extends ClassLoader {

	private Map<String, Class> classes;
	private URL[] urls;
	private ClassLoader loader;

	/**
	 * Constructor. Sets the class loader to be used if we cannot load it.
	 * 
	 * @param urls
	 *            Additional class URLS to be used when resolving references
	 *            from class to be loaded.
	 */
	public PluginClassLoader(URL[] urls) {
		super(PluginClassLoader.class.getClassLoader());
		
		classes = new HashMap<String, Class>();
		
		this.urls = urls;
		
		loader = new URLClassLoader(urls, PluginClassLoader.class
				.getClassLoader());
	
		try {
			String mainFrameName = "org.aavso.tools.vstar.ui.MainFrame";
			classes.put(mainFrameName, loader.loadClass(mainFrameName));
		} catch (Exception e) {
			System.err.println("BOO!");
		}
	}

	/**
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return findClass(name);
	}

	/**
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class clazz = null;

		clazz = classes.get(name);
		if (clazz == null) {

			try {
				clazz = findSystemClass(name);
			} catch (Exception e) {
				// Do nothing; proceed.
			}

			if (clazz == null) {
				clazz = loader.loadClass(name);
				classes.put(name, clazz);
			}
		}

		return clazz;
	}
}
