package template;

import java.util.List;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.ModelCreatorPluginBase;
import org.aavso.tools.vstar.util.model.IModel;

/**
 * Model creator
 */
public class ModelCreator extends ModelCreatorPluginBase {

	/**
	 * Get the display name for this plugin, e.g. for a menu item.
	 */
	@Override
	public String getDisplayName() {
		return "Skeleton model creator";
	}

	/**
	 * Get a description of this plugin, e.g. for display in a plugin manager.
	 */
	@Override
	public String getDescription() {
		return "Skeleton model creator";
	}
	
	/**
	 * Returns the model object for this plugin whose execute() method can be
	 * invoked to create the model artifacts.
	 * 
	 * @param obs
	 *            The list of observations to create a model of.
	 * @return The model object or null if no model was created.
	 */	
	@Override
	public IModel getModel(List<ValidObservation> obs) {
		return null;
	}
}
