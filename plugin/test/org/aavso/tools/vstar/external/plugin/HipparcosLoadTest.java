package org.aavso.tools.vstar.external.plugin;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.util.Tolerance;

import junit.framework.TestCase;

/**
 * Loads Hipparcos sample data via {@link ObservationSourcePluginBase#getTestRetriever}.
 */
public class HipparcosLoadTest extends TestCase {

    private static class HipparcosTestAccess extends HipparcosObservationSource {
        AbstractObservationRetriever load(String[] lines) throws Exception {
            return getTestRetriever(lines, "HIP");
        }
    }

    public void testLoadSampleLine() throws Exception {
        HipparcosTestAccess plugin = new HipparcosTestAccess();
        AbstractObservationRetriever retriever = plugin
                .load(new String[] { "JD\n", "10000|10.0|0|0\n" });
        assertEquals(0, retriever.getInvalidObservations().size());
        assertEquals(1, retriever.getValidObservations().size());
        ValidObservation ob = retriever.getValidObservations().get(0);
        assertTrue(Tolerance.areClose(2450000.0, ob.getJD(), 1e-6, true));
        assertTrue(Tolerance.areClose(10.0, ob.getMag(), 1e-6, true));
    }
}
