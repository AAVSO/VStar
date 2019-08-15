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
package org.aavso.tools.vstar.ui.dialog.vela;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import org.aavso.tools.vstar.ui.dialog.FileExtensionFilter;

/**
 * This class implements a VeLa file save chooser.
 */
public class VeLaFileSaveChooser {

	private JFileChooser fileChooser;

	public VeLaFileSaveChooser() {
		fileChooser = new JFileChooser();
		
		List<String> extensions = new ArrayList<String>();
		extensions.add("txt");
		extensions.add("vl");
		extensions.add("vela");

		fileChooser.setFileFilter(new FileExtensionFilter(extensions));
	}
	
	/**
	 * Show the file dialog.
	 * 
	 * @param parent
	 *            The parent component to which this dialog should be positioned
	 *            relative.
	 * @return Whether the dialog was "approved".
	 */
	public boolean showDialog(Component parent) {
		return fileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION;
	}

	/**
	 * Returns the selected file and ensures it has a ".PNG" suffix, e.g. so
	 * that Windows (vs Mac) knows it's an image file.
	 * 
	 * @return The selected file.
	 */
	public File getSelectedFile() {
		return fileChooser.getSelectedFile();
	}
}
