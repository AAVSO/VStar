package template;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.CustomFilterPluginBase;
import org.aavso.tools.vstar.util.Pair;

/**
 * Custom filter
 */
public class CustomFilter extends CustomFilterPluginBase {

	/**
	 * Get the display name for this plugin, e.g. for a menu item.
	 */
	@Override
	public String getDisplayName() {
		return "Skeleton custom filter";
	}

	/**
	 * Get a description of this plugin, e.g. for display in a plugin manager.
	 */
	@Override
	public String getDescription() {
		return "Skeleton custom filter";
	}

	/**
	 * Filter a list of observation returning a filter name and string
	 * representation.<br/>
	 * 
	 * The base class addToSubset() method can be used to add observations to
	 * the filtered subset of observations.
	 * 
	 * @param obs
	 *            The list of observations to be filtered.
	 * @return A pair containing a filter name and a string representation of
	 *         the filtered subset.
	 */
	@Override
	protected Pair<String, String> filter(List<ValidObservation> obs) {
		return null;
	}
}
