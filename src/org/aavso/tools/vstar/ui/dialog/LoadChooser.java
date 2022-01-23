/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed inputStream the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.aavso.tools.vstar.ui.dialog;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.aavso.tools.vstar.util.Pair;

public class LoadChooser {
	
	private JFileChooser loadFileChooser = null;
	
	
	/**
	 * Constructor: open file dialog
	 * 
	 * @param extensionFilterOpen
	 *          file filters for 'Open' dialog
	 *          
	 * @param titleOpen
	 * 
	 */
	public LoadChooser(FileNameExtensionFilter[] extensionFilterOpen, String titleOpen) {
		loadFileChooser = new JFileChooser();
		
		if (titleOpen != null) {
			loadFileChooser.setDialogTitle(titleOpen);
		}

		if (extensionFilterOpen != null) {
			FileNameExtensionFilter flt0 = null;
			for (FileNameExtensionFilter flt : extensionFilterOpen) {
				if (flt != null) {
					if (flt0 == null) {
						flt0 = flt;
					}
					loadFileChooser.addChoosableFileFilter(flt);
				}
			}
			if (flt0 != null) {
				loadFileChooser.setFileFilter(flt0);
			}
		}
	}
	
	/**
	 * Show the file dialog.
	 * 
	 * @param parent
	 *            The parent component to which this dialog should be positioned
	 *            relative.
	 * @return Whether the dialog was "approved".
	 * @throws Exception 
	 */
	public boolean showDialog(Component parent) {
		return loadFileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION;
	}
	
	/**
	 * Returns the selected file
	 * 
	 * @return The selected file.
	 */
	public File getOpenDialogSelectedFile() {
		return loadFileChooser.getSelectedFile();
	}
	
	/**
	 * Read the content of a file to a String
	 * 
	 * @param path
	 *          Path to a file
	 *          
	 * @param charset
	 *          Charset, if null, the default charset is used. 
	 * 
	 * @return Content as String
	 * 
	 * @throws IOException 
	 */
	public String readFileAsString(String path, Charset charset) throws IOException {
		if (charset == null) {
			charset = Charset.defaultCharset();
		}
		StringBuffer content = new StringBuffer();
		String line;
		try (FileInputStream fis = new FileInputStream(path);
			 InputStreamReader isr = new InputStreamReader(fis, charset);
			 BufferedReader reader = new BufferedReader(isr)
			) {
			while ((line = reader.readLine()) != null) {
				content.append(line);
				content.append("\n");
			}
		}
		return content.toString();
	}

	/**
	 * Invoke Open Dialog (chooser) and read the content of the selected file to a String
	 * 
	 * @param parent
	 *            The parent component to which the dialog should be positioned
	 *            relative.
	 *            
	 * @param charset
	 *          Charset, if null, the default charset is used. 
	 * 
	 * @return a Pair object: file content as the first member, file extension as the second one
	 *            or null if no files selected (dialog cancelled)
	 * 
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")	
	public Pair<String, String> readFileAsString(Component parent, Charset charset) throws IOException {
		return (Pair<String, String>)readFile(parent, charset, false);
	}
	
	/**
	 * Invoke Open Dialog (chooser) and read the content of the selected file to an array of bytes
	 * 
	 * @param parent
	 *            The parent component to which the dialog should be positioned
	 *            relative.
	 * 
	 * @return a Pair object: file content as the first member, file extension as the second one
	 *            or null if no files selected (dialog cancelled)
	 * 
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public Pair<byte[], String> readFileAsBytes(Component parent) throws IOException {
		return (Pair<byte[], String>)readFile(parent, null, true);
	}

	/**
	 * Read the content of a file to a byte array
	 * 
	 * @param path
	 *          Path to a file
	 * 
	 * @return Content as array of bytes
	 * 
	 * @throws IOException 
	 */
	public byte[] readFileAsBytes(String path) throws IOException {
		Path p = Paths.get(path);
		return Files.readAllBytes(p);
	}
	
	private Pair<?, String> readFile(Component parent, Charset charset, boolean raw) throws IOException {
		if (showDialog(parent)) {
			File file = getOpenDialogSelectedFile();
			if (file != null) {
				String filename = file.getName();
				String extension = "";
				int index = filename.lastIndexOf('.');
				if (index > 0) {
					extension = filename.substring(index + 1);
				}
				if (raw) {
					return new Pair<byte[], String>(readFileAsBytes(file.getAbsolutePath()), extension);
				} else {
					return new Pair<String, String>(readFileAsString(file.getAbsolutePath(), charset), extension);
				}
			} else {
				return null;
			}
		}
		return null;
	}
	
}
