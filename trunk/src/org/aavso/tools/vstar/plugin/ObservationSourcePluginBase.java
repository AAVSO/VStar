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
package org.aavso.tools.vstar.plugin;

import java.io.InputStream;

import org.aavso.tools.vstar.input.AbstractObservationRetriever;

/**
 * <p>
 * This is the base class for all observation source plug-in classes.
 * </p>
 */
public abstract class ObservationSourcePluginBase implements PluginBase {
	
	protected InputStream inputStream;
	protected String inputName;

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
	public abstract AbstractObservationRetriever getObservationRetriever();

	/**
	 * Get the name of the star associated with the current observation dataset.
	 * 
	 * @return The current star name.
	 */
	public abstract String getCurrentStarName();
	
	/**
	 * What is the input type for this plug-in?
	 * @return The input type.
	 */
	public abstract InputType getInputType();
	
	/**
	 * Sets the input stream and name.
	 *  
	 * @param inputStream The input stream.
	 * @param inputName A name associated with the input (e.g. file, URL).
	 */
	public void setInputInfo(InputStream inputStream, String inputName) {
		this.inputStream = inputStream;
		this.inputName = inputName;
	}

	/**
	 * @return the input stream
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * @return the name associated with the input (e.g. file, URL)
	 */
	public String getInputName() {
		return inputName;
	}
}
