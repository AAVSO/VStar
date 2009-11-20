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
package org.aavso.tools.vstar.ui;

import java.io.File;
import java.util.List;

import javax.swing.filechooser.FileFilter;

/**
 * File extension based file filter for file dialog.
 */
public class FileExtensionFilter extends FileFilter {

	private List<String> extensions;

	/**
	 * Constructor
	 * 
	 * @param extensions
	 *            A list of file extensions on which to filter.
	 */
	public FileExtensionFilter(List<String> extensions) {
		super();
		this.extensions = extensions;
	}

	/**
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File f) {
		boolean acceptable = false;

		String name = f.getName();

		if (f.isDirectory()) {
			acceptable = true;
		} else {
			for (String extension : extensions) {
				if (name.endsWith(extension)) {
					acceptable = true;
					break;
				}
			}
		}

		return acceptable;
	}

	/**
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	public String getDescription() {
		StringBuffer strBuf = new StringBuffer();

		for (String extension : extensions) {
			strBuf.append("'");
			strBuf.append(extension);
			strBuf.append("' ");
		}

		return strBuf.toString().trim().replace(" ", ", ");
	}
}
