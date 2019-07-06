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

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.ui.mediator.NewStarType;
import org.aavso.tools.vstar.ui.resources.LoginInfo;

/**
 * <p>
 * This is the base class for all observation source plug-in classes.
 * </p>
 * <p>
 * An observation source plugin will appear in VStar's File menu when its jar
 * file is placed into the vstar_plugins directory.
 * </p>
 */
public abstract class ObservationSourcePluginBase implements IPlugin {

	protected String userName;
	protected String password;

	protected List<InputStream> inputStreams;
	protected String inputName;

	protected boolean isAdditive;

	/**
	 * Constructor
	 * 
	 * @param userName
	 *            The user name to pass to the authenticator.
	 * @param password
	 *            The password to pass to the authenticator.
	 */
	protected ObservationSourcePluginBase(String username, String password) {
		this.userName = username;
		this.password = password;
		isAdditive = false;
	}

	/**
	 * Constructor
	 */
	protected ObservationSourcePluginBase() {
		this(null, null);
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
	public abstract AbstractObservationRetriever getObservationRetriever();

	/**
	 * Get the name of the star associated with the current observation dataset.
	 * 
	 * @return The current star name.
	 * @deprecated Use getInputName() or getObservationRetriever() for more
	 *             specific information about source name, type, and
	 *             observations possibly containing object name.
	 */
	public String getCurrentStarName() {
		return null;
	}

	/**
	 * What is the input type for this plug-in?
	 * 
	 * @return The input type.
	 */
	public abstract InputType getInputType();

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getGroup()
	 */
	@Override
	public String getGroup() {
		return null;
	}

	/**
	 * Sets the input streams and a representative name for these.
	 * 
	 * @param inputStreams
	 *            The input streams. Multiple streams may be required, e.g. one
	 *            per filter or one to access data and another to access catalog
	 *            information.
	 * @param inputName
	 *            A name associated with the input (e.g. file, URL).
	 */
	public void setInputInfo(List<InputStream> inputStreams, String inputName) {
		this.inputStreams = inputStreams;
		this.inputName = inputName;
	}

	/**
	 * Return a list of file extension strings to be added to the default file
	 * extensions in the file chooser, or null if none are to be added (the
	 * default).
	 * 
	 * @return The list of file extension strings to be added to the file
	 *         chooser.
	 */
	public List<String> getAdditionalFileExtensions() {
		return null;
	}

	/**
	 * @return the input streams
	 */
	public List<InputStream> getInputStreams() {
		return inputStreams;
	}

	/**
	 * <p>
	 * Return a list of File objects; defaults to null.
	 * </p>
	 * 
	 * NewStarFromObsSourcePlugin invokes this when the input type is FILE. If
	 * null is returned, a dialog is opened requesting a single File be
	 * selected. The purpose of this is so that when executed from a WebStart
	 * context, a plugin cannot create input streams. It can however create File
	 * objects, so if it wants to construct Files on the fly, this will be OK
	 * from a security policy perspective.
	 * 
	 * @return a list of URLs or null.
	 * @throws Exception
	 *             if a problem occurs during URL creation.
	 * @deprecated
	 */
	public List<File> getFiles() throws Exception {
		return null;
	}

	/**
	 * <p>
	 * Return a list of URL objects; defaults to null.
	 * </p>
	 * 
	 * NewStarFromObsSourcePlugin invokes this when the input type is URL. If
	 * null is returned, a dialog is opened requesting a single URL be entered.
	 * The purpose of this is so that when executed from a WebStart context, a
	 * plugin cannot create input streams. It can however create URL objects, so
	 * if it wants to construct URLs on the fly, this will be OK from a security
	 * policy perspective.
	 * 
	 * @return a list of URLs or null.
	 * @throws Exception
	 *             if a problem occurs during URL creation.
	 */
	public List<URL> getURLs() throws Exception {
		return null;
	}

	/**
	 * @return the name associated with the input (e.g. file, URL)
	 */
	public String getInputName() {
		return inputName;
	}

	/**
	 * @return the userName, e.g. for a URL based source; may be null
	 */
	public String getUsername() {
		return userName;
	}

	/**
	 * @return the password, e.g. for a URL based source; may be null
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * If a non-null value is return by this method, once all observations are
	 * loaded, a series visibility change message will be sent to ensure that
	 * all observations are visible.
	 * 
	 * @return The set of visible series or null.
	 */
	public Set<SeriesType> getVisibleSeriesTypes() {
		return null;
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#requiresAuthentication()
	 */
	@Override
	public boolean requiresAuthentication() {
		return false;
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#additionalAuthenticationSatisfied(org.aavso.tools.vstar.ui.resources.LoginInfo)
	 */
	@Override
	public boolean additionalAuthenticationSatisfied(LoginInfo loginInfo) {
		return true;
	}

	/**
	 * @return the isAdditive
	 */
	public boolean isAdditive() {
		return isAdditive;
	}

	/**
	 * @param isAdditive
	 *            the isAdditive to set
	 */
	public void setAdditive(boolean isAdditive) {
		this.isAdditive = isAdditive;
	}

	/**
	 * This method returns the NewStarType enumerated value. Most observation
	 * sources will use the default implementation, a few may override it.
	 * 
	 * @return The NewStarType enumerated value
	 */
	public NewStarType getNewStarType() {
		return NewStarType.NEW_STAR_FROM_ARBITRARY_SOURCE;
	}
}
