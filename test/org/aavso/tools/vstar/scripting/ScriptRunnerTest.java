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

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import junit.framework.TestCase;

/**
 * Tests for JavaScript engine availability and {@link ScriptRunner}.
 *
 * Issue #601: Tool -> Run Script... failed on Java 15+ after Nashorn was
 * removed from the JDK.
 */
public class ScriptRunnerTest extends TestCase {

	public ScriptRunnerTest(String name) {
		super(name);
	}

	public void testJavaScriptEngineIsAvailable() {
		ScriptEngine engine = new ScriptEngineManager(
				ScriptRunnerTest.class.getClassLoader())
				.getEngineByName("javascript");
		assertNotNull(
				"JavaScript engine should be available (standalone Nashorn)",
				engine);
	}

	public void testCompiledScriptSeesGlobalBindings() throws Exception {
		ScriptEngine engine = new ScriptEngineManager(
				ScriptRunnerTest.class.getClassLoader())
				.getEngineByName("javascript");
		assertNotNull(engine);

		Bindings bindings = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
		assertNotNull("GLOBAL_SCOPE bindings should be present", bindings);
		bindings.put("answer", 42);

		Object result = ((Compilable) engine).compile("answer + 1").eval();
		assertEquals(43, ((Number) result).intValue());
	}

	public void testScriptRunnerEvalAndVStarBinding() throws Exception {
		ScriptRunner runner = new ScriptRunner(false);
		Object result = runner.eval("1 + 2");
		assertEquals(3, ((Number) result).intValue());
		assertEquals("object", runner.eval("typeof vstar"));
		assertEquals("function", runner.eval("typeof println"));
	}
}
