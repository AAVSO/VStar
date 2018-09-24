package template;

import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;

/**
 * Observation tool
 */
public class ObservationTool extends ObservationToolPluginBase {

	/**
	 * Get the display name for this plugin, e.g. for a menu item.
	 */
	@Override
	public String getDisplayName() {
		return "Skeleton observation tool";
	}

	/**
	 * Get a description of this plugin, e.g. for display in a plugin manager.
	 */
	@Override
	public String getDescription() {
		return "Skeleton observation tool";
	}

	/**
	 * Given information about observations per series, perform some arbitrary
	 * processing on a subset of the observations.
	 * 
	 * @param seriesInfo
	 *            A mapping from series type to lists of currently loaded
	 *            observations.
	 */
	@Override
	public void invoke(ISeriesInfoProvider seriesInfo) {
		// TODO
	}
}
