package org.aavso.tools.vstar.external.plugin;

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.plugin.IPlugin;
import org.aavso.tools.vstar.ui.resources.PluginLoader;

import junit.framework.TestCase;

// Invoke the test() method on each plug-in that is built in and currently
// in the user's environment. It may be better to iterate over each class
// in the org.aavso.tools.vstar.external.plugin package and include intrinsic
// plug-ins.

public class PluginTest extends TestCase {

	private List<IPlugin> plugins;
	
	public PluginTest(String name) {
		super(name);

		PluginLoader.loadPlugins();

		plugins = new ArrayList<IPlugin>();
		plugins.addAll(PluginLoader.getCustomFilterPlugins());
		plugins.addAll(PluginLoader.getGeneralToolPlugins());
		plugins.addAll(PluginLoader.getModelCreatorPlugins());
		plugins.addAll(PluginLoader.getObservationSinkPlugins());
		plugins.addAll(PluginLoader.getObservationSourcePlugins());
		plugins.addAll(PluginLoader.getObservationToolPlugins());
		plugins.addAll(PluginLoader.getObservationTransformerPlugins());
		plugins.addAll(PluginLoader.getPeriodAnalysisPlugins());
	}
	
	public void testPlugins() {
		int failCount = 0;
		int passCount = 0;
		boolean overallResult = true;
		
		System.out.printf("Running %d Plugin Tests...\n", plugins.size());
		
		for (IPlugin plugin : plugins) {
			Boolean success = plugin.test();
			if (success != null) {
				overallResult &= success;
				
				System.out.printf("[%s]  %s ('%s') test\n",
						success ? "PASSED" : "FAILED",
						plugin.getClass().getSimpleName(),
						plugin.getDescription());
				
				if (success) passCount++; else failCount++;
			} else {
				System.out.printf("[NO TEST] %s ('%s')\n",
						plugin.getClass().getSimpleName(),
						plugin.getDescription());
			}
		}

		System.out.printf(String.format("** PASSED: %d, FAILED: %d, NO TEST: %d\n",
				passCount, failCount, plugins.size()-passCount-failCount));
		
		assertTrue(overallResult);
	}
}
