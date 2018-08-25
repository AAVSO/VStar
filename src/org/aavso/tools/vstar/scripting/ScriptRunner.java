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
import java.io.FileReader;
import java.io.IOException;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.JFileChooser;

import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;

/**
 * This class runs a VStar script.
 */
public class ScriptRunner {

	private static ScriptRunner instance = new ScriptRunner(true);

	private ScriptEngineManager manager;
	private ScriptEngine jsEngine;
	private Compilable compilable;
	private Bindings bindings;
	private String error;
	private String warning;

	private JFileChooser scriptFileChooser;

	/**
	 * Constructor
	 */
	public ScriptRunner(boolean fromFileChooser) {
		manager = new ScriptEngineManager();
		jsEngine = manager.getEngineByName("javascript");
		compilable = (Compilable) jsEngine;
		bindings = jsEngine.getBindings(ScriptContext.GLOBAL_SCOPE);
		bindings.put("vstar", VStarScriptingAPI.getInstance());
		if (fromFileChooser) {
			scriptFileChooser = new JFileChooser();
		}
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

		int returnVal = scriptFileChooser.showOpenDialog(DocumentManager
				.findActiveWindow());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			scriptFile = scriptFileChooser.getSelectedFile();
			runScript(scriptFile);
		}
	}

	/**
	 * Run script from the specified file.
	 */
	public void runScript(File scriptFile) {
		FileReader reader = null;

		try {
			setError(null);
			setWarning(null);

			Mediator.getUI().setScriptingStatus(true);

			reader = new FileReader(scriptFile);
			jsEngine.put(ScriptEngine.FILENAME, scriptFile.toString());
			CompiledScript script = compilable.compile(reader);
			script.eval();
		} catch (ScriptException ex) {
			Mediator.getUI().setScriptingStatus(false);
			MessageBox
					.showErrorDialog("Script Error", ex.getLocalizedMessage());
		} catch (Throwable ex) {
			Mediator.getUI().setScriptingStatus(false);
			MessageBox.showErrorDialog("Script Error",
					String.format("Error: " + ex.getLocalizedMessage()));

		} finally {
			Mediator.getUI().setScriptingStatus(false);
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// Nothing to be done
				}
			}
		}
	}

	/**
	 * @return the error
	 */
	public String getError() {
		return error;
	}

	/**
	 * @param error
	 *            the error to set
	 */
	public void setError(String error) {
		this.error = error;
	}

	/**
	 * @return the warning
	 */
	public String getWarning() {
		return warning;
	}

	/**
	 * @param warning
	 *            the warning to set
	 */
	public void setWarning(String warning) {
		this.warning = warning;
	}

	/**
	 * Bind a name to a value in the scripting context.
	 * 
	 * @param name
	 *            The name of the value to bind to.
	 * @param value
	 *            The value to which to bind.
	 */
	public void bind(String name, Object value) {
		bindings.put(name, value);
	}
}
