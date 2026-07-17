package org.aavso.tools.vstar.external.plugin;

import junit.framework.TestCase;

public class OCAnalysisToolTest extends TestCase {

    public OCAnalysisToolTest(String name) {
        super(name);
    }

    public void testPluginSmokeFosterClock2() {
        Boolean ok = new OCAnalysisTool().test();
        assertNotNull(ok);
        assertTrue(ok);
    }
}
