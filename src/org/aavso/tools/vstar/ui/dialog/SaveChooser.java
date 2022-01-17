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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.aavso.tools.vstar.util.locale.LocaleProps;

public class SaveChooser {
	
	private JFileChooser saveFileChooser = null;
	private String defaultExt = null;
	
	
	/**
	 * Constructor: save file dialogs
	 * 
	 * @param extensionFilterSave
	 *          file filters for 'Save' dialog
	 * 
	 * @param defaultExt
	 *          default file extension for 'Save' dialog
	 *          
	 * @param titleSave
	 * 
	 */
	public SaveChooser(FileNameExtensionFilter[] extensionFilterSave, String defaultExt, String titleSave) {
		saveFileChooser = new JFileChooser();
		
		if (titleSave != null) {
			saveFileChooser.setDialogTitle(titleSave);
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
	 * Show the file save dialog.
	 * 
	 * @param parent
	 *            The parent component to which the dialog should be positioned
	 *            relative.
	 * @return Whether the dialog was "approved".
	 */
	public boolean showDialog(Component parent) {
		if (saveFileChooser != null) {
			return saveFileChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION;
		} else {
			MessageBox.showErrorDialog(parent, "Error", "File Chooser was not created");
			return false;
		}
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
	 * Invoke Save Dialog (chooser) and write @content to the selected file.
	 * 
	 * @param parent
	 *            The parent component to which the dialog should be positioned
	 *            relative.
	 *
	 * @param content
	 *          String to be written         
	 *            
	 * @param charset
	 *          Charset, if null, the default charset is used. 
	 *            
	 * @return true if dialog was approved, false otherwise.
	 * 
	 * @throws IOException 
	 *             
	 */
	public boolean writeStringToFile(Component parent, String content, Charset charset) throws IOException {
		if (showDialog(parent)) {
			File file = getSaveDialogSelectedFile();
			if (file != null) {
				if (file.exists() && file.isFile() && !MessageBox.showConfirmDialog(LocaleProps.get("FILE_MENU_SAVE"),
						file.getName() + ": " + LocaleProps.get("SAVE_OVERWRITE"))) {
					return false;
				}
				String path = file.getAbsolutePath();
				writeStringToFile(path, content, charset);
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Write a String to file
	 * 
	 * @param path
	 *          Path to a file
	 *          
	 * @param content
	 *          String to be written         
	 *          
	 * @param charset
	 *          Charset, if null, the default charset is used. 
	 *          
	 * @throws IOException 
	 * 
	 */
	public void writeStringToFile(String path, String content, Charset charset) throws IOException {
		if (charset == null) {
			charset = Charset.defaultCharset();
		}
		try (FileOutputStream fos = new FileOutputStream(path);
		     OutputStreamWriter osw = new OutputStreamWriter(fos, charset);
		     BufferedWriter writer = new BufferedWriter(osw)
		) {
			writer.write(content);
			writer.flush();
			writer.close();
		}
	}
	
}
