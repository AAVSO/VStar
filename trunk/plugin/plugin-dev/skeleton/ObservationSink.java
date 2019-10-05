package template;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ObservationSinkPluginBase;

/**
 * Observation sink
 */
public class ObservationSink extends ObservationSinkPluginBase {

    /**
     * Get the display name for this plugin, e.g. for a menu item.
     */
	@Override
	public String getDisplayName() {
		return null;
	}

    /**
     * Get a description of this plugin, e.g. for display in a plugin manager.
     */
	@Override
	public String getDescription() {
		return null;
	}

	/**
	 * Save the specified observations.
	 * 
	 * @param writer
	 *            A text writer.
	 * @param obs
	 *            A list of observations.
	 * @param delimiter
	 *            The field delimiter to use; may be null.
	 */
	@Override
	public void save(PrintWriter writer, List<ValidObservation> obs,
			String delimiter) throws IOException {
		// TODO Auto-generated method stub
	}
}
