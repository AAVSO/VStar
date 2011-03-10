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
package org.aavso.tools.vstar.scripting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JFileChooser;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.dialog.MessageBox;

/**
 * This class runs a VStar script.
 */
public class ScriptRunner {

	private static ScriptRunner instance = new ScriptRunner();

	private ScriptEngineManager manager;
	private ScriptEngine jsEngine;
	private ScriptContext context;

	private JFileChooser scriptFileChooser;

	/**
	 * Constructor.
	 */
	protected ScriptRunner() {
		manager = new ScriptEngineManager();
		jsEngine = manager.getEngineByName("JavaScript");
		context = jsEngine.getContext();
		context.setAttribute("vstar", new VStarScriptingAPI(), context
				.getScopes().get(0));
		scriptFileChooser = new JFileChooser();
	}

	/**
	 * Return a Singleton.
	 */
	public static ScriptRunner getInstance() {
		return instance;
	}

	/**
	 * Run script from a chosen file.
	 */
	public void runScript() {
		File scriptFile = null;

		try {
			int returnVal = scriptFileChooser.showOpenDialog(MainFrame
					.getInstance());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				scriptFile = scriptFileChooser.getSelectedFile();
				FileReader reader = new FileReader(scriptFile);
				jsEngine.eval(reader, context);
			}
		} catch (FileNotFoundException ex) {
			MessageBox.showErrorDialog("Script Error", "Cannot load script '"
					+ scriptFile.getAbsolutePath() + "'");
		} catch (ScriptException ex) {
			MessageBox.showErrorDialog("Script Error", String.format(
					"Error in '%s' on line %d.", ex.getFileName(), ex
							.getLineNumber()));
		}
	}
}
