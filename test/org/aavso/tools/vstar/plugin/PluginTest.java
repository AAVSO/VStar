package org.aavso.tools.vstar.plugin;

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.ui.resources.PluginLoader;

import junit.framework.TestCase;

// Invoke the test() method on each plug-in

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
