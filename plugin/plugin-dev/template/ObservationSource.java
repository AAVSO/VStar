/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
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
