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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.ui.mediator.NewStarType;
import org.aavso.tools.vstar.ui.resources.LoginInfo;

/**
 * <p>
 * This is the base class for all observation source plug-in classes.
 * </p>
 * <p>
 * An observation source plugin will appear in VStar's File menu or file load
 * dialog (if the source loads from a file +/- a URL) when its jar file is
 * placed into the vstar_plugins directory.
 * </p>
 */
public abstract class ObservationSourcePluginBase implements IPlugin {

	protected boolean testMode = false;

	protected String userName;
	protected String password;

	protected List<InputStream> inputStreams;
	protected String inputName;
	
	protected Map<InputStream, String> streamNameMap;

	protected boolean isAdditive;

	protected String velaFilterStr;

	/**
	 * Constructor
	 * 
	 * @param userName The user name to pass to the authenticator.
	 * @param password The password to pass to the authenticator.
	 */
	protected ObservationSourcePluginBase(String username, String password) {
		this.userName = username;
		this.password = password;
		streamNameMap = new HashMap<InputStream, String>();
		isAdditive = false;
		velaFilterStr = "";
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
	 * A new observation retriever instance should be created for each call to this
	 * method to avoid side-effects relating to clearing of collections by Mediator
	 * upon each new-star-artefact creation operation.
	 * </p>
	 * 
	 * @return An observation retriever.
	 */
	public abstract AbstractObservationRetriever getObservationRetriever() throws IOException, ObservationReadError;

	/**
	 * Get the name of the star associated with the current observation dataset.
	 * 
	 * @return The current star name.
	 * Use getInputName() or getObservationRetriever() for more specific
	 * information about source name, type, and observations possibly
	 * containing object name.
	 */
	@Deprecated
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
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return null;
	}
	
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
	 * TODO: wondering whether this needs to be deprecated
	 * 
	 * @param inputStreams The input streams. Multiple streams may be required, e.g.
	 *                     one per filter or one to access data and another to
	 *                     access catalog information or multiple selected files.
	 * @param inputName    A name associated with the input (e.g. file, URL).
	 */
	public void setInputInfo(List<InputStream> inputStreams, String inputName) {
		this.inputStreams = inputStreams;
		this.inputName = inputName;
	}

	/**
	 * Empty the stream-name map.
	 */
	public void clearStreamNameMap() {
	    streamNameMap.clear();
	}
	
	/**
	 * Add an input-name, stream pair to the stream-name map,
	 * e.g. for error reporting.
	 * 
     * @param stream The input stream associated with the input name.
	 * @param name A name associated with the input (e.g. file, URL).
	 */
	public void addStreamNamePair(InputStream stream, String name) {
	    streamNameMap.put(stream, name);
	}
	
	/**
	 * Given an input stream, return the corresponding name.
	 * 
	 * @param stream The input stream.
	 * @return The corresponding name.
	 */
	public String nameFromStream(InputStream stream) {
	    return streamNameMap.get(stream);
	}
	
	/**
	 * Return a list of file extension strings to be added to the default file
	 * extensions in the file chooser, or null if none are to be added (the
	 * default).
	 * 
	 * @return The list of file extension strings to be added to the file chooser.
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
	 * NewStarFromObsSourcePlugin invokes this when the input type is FILE. If null
	 * is returned, a dialog is opened requesting a single File be selected. The
	 * purpose of this is so that when executed from a WebStart context, a plugin
	 * cannot create input streams. It can however create File objects, so if it
	 * wants to construct Files on the fly, this will be OK from a security policy
	 * perspective.
	 * 
	 * @return a list of URLs or null.
	 * @throws Exception if a problem occurs during URL creation.
	 */
	@Deprecated
	public List<File> getFiles() throws Exception {
		return null;
	}

	/**
	 * <p>
	 * Return a list of URL objects; defaults to null.
	 * </p>
	 * 
	 * NewStarFromObsSourcePlugin invokes this when the input type is URL. If null
	 * is returned, a dialog is opened requesting a single URL be entered. The
	 * purpose of this is so that when executed from a WebStart context, a plugin
	 * cannot create input streams. It can however create URL objects, so if it
	 * wants to construct URLs on the fly, this will be OK from a security policy
	 * perspective.
	 * 
	 * @return a list of URLs or null.
	 * @throws Exception if a problem occurs during URL creation.
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
	 * loaded, a series visibility change message will be sent to ensure that all
	 * observations are visible.
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
	 * @return are loads currently additive?
	 */
	public boolean isAdditive() {
		return isAdditive;
	}

	/**
	 * @param isAdditive should loads be additive?
	 */
	public void setAdditive(boolean isAdditive) {
		this.isAdditive = isAdditive;
	}

	/**
	 * @return whether multiple file selection is permitted
	 */
	public boolean isMultipleFileSelectionAllowed() {
        return false;
    }

	/** 
	 * @return whether this plug-in is a source of text based observations
	 */
	public boolean isTextSource() {
	    // this is the common case
	    return true;
	}
	
    /**
	 * @return the velaFilterStr
	 */
	public String getVelaFilterStr() {
		return velaFilterStr;
	}

	/**
	 * @param velaFilterStr the velaFilterStr to set
	 */
	public void setVelaFilterStr(String velaFilterStr) {
		this.velaFilterStr = velaFilterStr;
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

	// Test methods

	@Override
	public Boolean test() {
		return null;
	}

	@Override
	public boolean inTestMode() {
		return testMode;
	}

	@Override
	public void setTestMode(boolean mode) {
		testMode = mode;
	}

	/**
	 * Helper method that takes an array of lines (each string ending in "\n", sets
	 * the plugin's inputs and, retrieves the observations and returns the
	 * observation retriever.
	 * 
	 * @param lines An array of linefeed-delimited strings
	 * @param inputName An input name (arbitrary string)
	 * @return The observation retriever
	 * @throws InterruptedException
	 * @throws ObservationReadError
	 * @throws IOException
	 */
	protected AbstractObservationRetriever getTestRetriever(String[] lines, String inputName)
			throws InterruptedException, ObservationReadError, IOException {
		StringBuffer content = new StringBuffer();
		for (String line : lines) {
			content.append(line);
		}

		InputStream in = new ByteArrayInputStream(content.toString().getBytes());
		List<InputStream> streams = new ArrayList<InputStream>();
		streams.add(in);
		setInputInfo(streams, inputName);

		AbstractObservationRetriever retriever = getObservationRetriever();
		retriever.retrieveObservations();

		return retriever;
	}
}
