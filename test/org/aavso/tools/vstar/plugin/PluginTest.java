package org.aavso.tools.vstar.plugin;

import java.util.LinkedHashSet;
import java.util.Set;

import org.aavso.tools.vstar.ui.resources.PluginLoader;

import junit.framework.TestCase;

// Invoke the test() method on each plug-in that is built in and currently
// in the user's environment. It may be better to iterate over each class
// in the org.aavso.tools.vstar.external.plugin package and include intrinsic
// plug-ins.

public class PluginTest extends TestCase {

	private Set<IPlugin> plugins;
	
	public PluginTest(String name) {
		super(name);

		// Load all plug-ins in the current environment
//		PluginLoader.loadPlugins();

		plugins = new LinkedHashSet<IPlugin>();
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
		
		System.out.printf("Running %d Plugin Tests...\n\n", plugins.size());
		
		for (IPlugin plugin : plugins) {
			Boolean success = plugin.test();
			if (success != null) {
				overallResult &= success;
				System.out.printf("%s (%s') test: %b\n",
						plugin.getClass().getSimpleName(),
						plugin.getDescription(), success);
			} else {
				System.out.printf("No test for %s (%s)\n",
						plugin.getClass().getSimpleName(),
						plugin.getDescription());
			}
		}
		
		System.out.printf(String.format("\n** PASS=%d, FAIL=%d, No Test=%d\n",
				passCount, failCount, plugins.size()-passCount-failCount));
		
		assertTrue(overallResult);
	}
}
