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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.aavso.tools.vstar.util.locale.LocaleProps;

import org.aavso.tools.vstar.util.Pair;

public class FileIOchoosers {
	
	private JFileChooser loadFileChooser;
	private JFileChooser saveFileChooser;
	private String defaultExt;
	
	
	/**
	 * Constructor
	 * 
	 * @param extensionFilterOpen
	 *          file filters for 'Open' dialog
	 *          
	 * @param extensionFilterSave
	 *          file filters for 'Save' dialog
	 * 
	 * @param defaultExt
	 *          default file extension for 'Save' dialog
	 *          
	 * @param titleOpen
	 * 
	 * @param titleSave
	 * 
	 */
	public FileIOchoosers(FileNameExtensionFilter[] extensionFilterOpen, FileNameExtensionFilter[] extensionFilterSave, 
			String defaultExt, String titleOpen, String titleSave) {
		loadFileChooser = new JFileChooser();
		saveFileChooser = new JFileChooser();
		
		if (titleOpen != null) {
			loadFileChooser.setDialogTitle(titleOpen);
		}

		if (titleSave != null) {
			saveFileChooser.setDialogTitle(titleSave);
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

		if (extensionFilterSave != null) {
			FileNameExtensionFilter flt0 = null;
			for (FileNameExtensionFilter flt : extensionFilterSave) {
				if (flt != null) {
					if (flt0 == null) {
						flt0 = flt;
					}
					saveFileChooser.addChoosableFileFilter(flt);
				}
			}
			if (flt0 != null) {
				saveFileChooser.setFileFilter(flt0);
			}
		}
		
		this.defaultExt = defaultExt;
		if (this.defaultExt != null && !"".equals(this.defaultExt) && this.defaultExt.charAt(0) != '.') {
			this.defaultExt = "." + this.defaultExt;
		}
	}
	
	/**
	 * Show the file dialog.
	 * 
	 * @param parent
	 *            The parent component to which this dialog should be positioned
	 *            relative.
	 * @return Whether the dialog was "approved".
	 */
	public boolean showOpenDialog(Component parent) {
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
	 * @return Content as String
	 * 
	 * @throws IOException 
	 */
	public String readFileAsString(String path) throws IOException {
		StringBuffer content = new StringBuffer();
		try (Stream<String> stream = Files.lines(Paths.get(path))) {
			stream.forEachOrdered(line -> {
				content.append(line);
				content.append("\n");
			});
			return content.toString();
		}
	}
	
	/**
	 * Invoke Open Dialog (chooser) and read the content of the selected file to a String
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
	public Pair<String, String> readFileAsString(Component parent) throws IOException {
		if (showOpenDialog(parent)) {
			File file = getOpenDialogSelectedFile();
			if (file != null) {
				String filename = file.getName();
				String extension = "";
				int index = filename.lastIndexOf('.');
				if (index > 0) {
					extension = filename.substring(index + 1);
				}			
				return new Pair<String, String>(readFileAsString(file.getAbsolutePath()), extension);
			} else {
				return null;
			}
		}
		return null;
	}
	
	/**
	 * Show the file save dialog.
	 * 
	 * @param parent
	 *            The parent component to which the dialog should be positioned
	 *            relative.
	 * @return Whether the dialog was "approved".
	 */
	public boolean showSaveDialog(Component parent) {
		return saveFileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION;
	}

	/**
	 * Returns the selected file and ensures it has a @defaultExt suffix (specified in the constructor)
	 * 
	 * @return The selected file.
	 */
	public File getSaveDialogSelectedFile() {
		File file = saveFileChooser.getSelectedFile();
		String path = file != null ? file.getAbsolutePath() : null;
		if (path != null && defaultExt != null && !"".equals(defaultExt) && !path.toLowerCase().endsWith(defaultExt)) {
			path = path + defaultExt;
			file = new File(path);
		}
		return file;
	}
	
	/**
	 * Write a String to file
	 * 
	 * @param path
	 *          Path to a file
	 *          
	 * @throws IOException 
	 * 
	 */
	public void writeStringToFile(String path, String content) throws IOException {
		try (FileWriter writer = new FileWriter(path)) {
			writer.write(content);
			writer.flush();
			writer.close();
		}
	}
	

	/**
	 * Invoke Save Dialog (chooser) and write @content to the selected file.
	 * 
	 * @param parent
	 *            The parent component to which the dialog should be positioned
	 *            relative.
	 * @return true if dialog was approved, false otherwise.
	 * @throws IOException 
	 *             
	 */
	public boolean writeStringToFile(Component parent, String content) throws IOException {
		if (showSaveDialog(parent)) {
			File file = getSaveDialogSelectedFile();
			if (file != null) {
				if (file.exists() && file.isFile() && !MessageBox.showConfirmDialog(LocaleProps.get("FILE_MENU_SAVE"),
						file.getName() + ": " + LocaleProps.get("SAVE_OVERWRITE"))) {
					return false;
				}
				String path = file.getAbsolutePath();
				writeStringToFile(path, content);
			}
			return true;
		} else {
			return false;
		}
	}
	
}
