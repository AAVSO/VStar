package template;

import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;

/**
 * Observation source
 */
public class ObservationSource extends ObservationSourcePluginBase {

	/**
	 * Get the display name for this plugin, e.g. for a menu item.
	 */
	@Override
	public String getDisplayName() {
		return "Skeleton observation source";
	}

	/**
	 * Get a description of this plugin, e.g. for display in a plugin manager.
	 */
	@Override
	public String getDescription() {
		return "Skeleton observation source";
	}

	/**
	 * What is the input type for this plug-in?
	 * 
	 * Possible return values:
	 * 
	 * InputType.FILE
	 * InputType.URL
	 * InputType.FILE_OR_URL
	 * InputType.NONE
	 *
	 * @return The input type.
	 */
	@Override
	public InputType getInputType() {
		return InputType.NONE;
	}

	/**
	 * <p>
	 * Get an observation retriever for this plug-in.
	 * </p>
	 * 
	 * <p>
	 * A new observation retriever instance should be created for each call to
	 * this method to avoid side-effects relating to clearing of collections by
	 * Mediator upon each new-star-artefact creation operation.
	 * </p>
	 * 
	 * @return An observation retriever.
	 */
	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return null;
	}
}
