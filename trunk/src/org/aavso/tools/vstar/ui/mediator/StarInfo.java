/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.ui.mediator;

import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.RAInfo;

/**
 * This class contains information about a star.
 * 
 * Some or all of its members may be absent, so each is null by default.
 */
public class StarInfo {

	private AbstractObservationRetriever retriever;
	private String designation = null;
	private String auid = null;
	private Double period = null;
	private Double epoch = null;
	private String varType = null;
	private String spectralType = null;
	private String discoverer = null;
	private RAInfo ra = null;
	private DecInfo dec = null;

	/**
	 * Parameterless constructor for web service.
	 */
	public StarInfo() {
	}

	/**
	 * Constructor.
	 * 
	 * @param retriever
	 *            The observation retriever that knows about the source of
	 *            observations. May be null! setRetriever() can be called after
	 *            construction.
	 * @param designation
	 *            A name or designation for the star; may be null.
	 * @param auid
	 *            The star's unique identifier (AAVSO unique ID).
	 * @param period
	 *            The star's period, if known.
	 * @param epoch
	 *            The star's (corresponding to period for phase plot purposes)
	 *            epoch, if known, as a Heliocentric Julian Date.
	 * @param varType
	 *            The variable's type, if known.
	 * @param spectralType
	 *            The spectral type, if known.
	 * @param discoverer
	 *            The discoverer of the star, if known.
	 * @param ra
	 *            The object's Right Acscension.
	 * @param dec
	 *            The object's Declination.
	 */
	public StarInfo(AbstractObservationRetriever retriever, String designation,
			String auid, Double period, Double epoch, String varType,
			String spectralType, String discoverer, RAInfo ra,
			DecInfo dec) {
		this.retriever = retriever;
		this.designation = designation != null
				&& designation.trim().length() != 0 ? designation
				: "Unknown Object";
		this.auid = auid;
		this.period = period;
		this.epoch = epoch;
		this.varType = varType;
		this.spectralType = spectralType;
		this.discoverer = discoverer;
		this.ra = ra;
		this.dec = dec;
	}

	/**
	 * Constructor.
	 * 
	 * All but designation and AUID are null.
	 * 
	 * @param retriever
	 *            The observation retriever that knows about the source of
	 *            observations. May be null! setRetriever() can be called after
	 *            construction.
	 * @param designation
	 *            A name or designation for the star.
	 * @param auid
	 *            The star's unique identifier (AAVSO unique ID).
	 */
	public StarInfo(AbstractObservationRetriever retriever, String designation,
			String auid) {
		this(retriever, designation, auid, null, null, null, null, null, null, null);
	}

	/**
	 * Constructor.
	 * 
	 * All but designation is null.
	 * 
	 * @param retriever
	 *            The observation retriever that knows about the source of
	 *            observations. May be null! setRetriever() can be called after
	 *            construction.
	 * @param designation
	 *            A name or designation for the star.
	 */
	public StarInfo(AbstractObservationRetriever retriever, String designation) {
		this(retriever, designation, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param designation
	 *            A name or designation for the star.
	 * @param auid
	 *            The star's unique identifier (AAVSO unique ID).
	 * @param period
	 *            The star's period, if known.
	 * @param epoch
	 *            The star's (corresponding to period for phase plot purposes)
	 *            epoch, if known, as a Heliocentric Julian Date.
	 * @param varType
	 *            The variable's type, if known.
	 * @param spectralType
	 *            The spectral type, if known.
	 * @param discoverer
	 *            The discoverer of the star, if known.
	 * @param ra
	 *            The object's Right Acscension.
	 * @param dec
	 *            The object's Declination.
	 */
	public StarInfo(String designation, String auid, Double period,
			Double epoch, String varType, String spectralType, String discoverer, RAInfo ra,
			DecInfo dec) {
		this(null, designation, auid, period, epoch, varType, spectralType,
				discoverer, ra, dec);
	}

	/**
	 * Constructor.
	 * 
	 * All but designation and AUID are null.
	 * 
	 * @param designation
	 *            A name or designation for the star.
	 * @param auid
	 *            The star's unique identifier (AAVSO unique ID).
	 */
	public StarInfo(String designation, String auid) {
		this(null, designation, auid);
	}

	/**
	 * @param retriever
	 *            the retriever to set
	 */
	public void setRetriever(AbstractObservationRetriever retriever) {
		this.retriever = retriever;
	}

	/**
	 * @return the retriever
	 */
	public AbstractObservationRetriever getRetriever() {
		return retriever;
	}

	/**
	 * @return the designation
	 */
	public String getDesignation() {
		return designation;
	}

	/**
	 * @return the auid
	 */
	public String getAuid() {
		return auid;
	}

	/**
	 * @return the period
	 */
	public Double getPeriod() {
		return period;
	}

	/**
	 * @return the epoch
	 */
	public Double getEpoch() {
		return epoch;
	}

	/**
	 * @return the varType
	 */
	public String getVarType() {
		return varType;
	}

	/**
	 * @return the spectralType
	 */
	public String getSpectralType() {
		return spectralType;
	}

	/**
	 * @return the discoverer
	 */
	public String getDiscoverer() {
		return discoverer;
	}


	/**
	 * @return the ra
	 */
	public RAInfo getRA() {
		return ra;
	}

	/**
	 * @return the dec
	 */
	public DecInfo getDec() {
		return dec;
	}
	
	/**
	 * @param designation
	 *            the designation to set
	 */
	public void setDesignation(String designation) {
		this.designation = designation;
	}

	/**
	 * @param auid
	 *            the auid to set
	 */
	public void setAuid(String auid) {
		this.auid = auid;
	}

	/**
	 * @param period
	 *            the period to set
	 */
	public void setPeriod(Double period) {
		this.period = period;
	}

	/**
	 * @param epoch
	 *            the epoch to set
	 */
	public void setEpoch(Double epoch) {
		this.epoch = epoch;
	}

	/**
	 * @param varType
	 *            the varType to set
	 */
	public void setVarType(String varType) {
		this.varType = varType;
	}

	/**
	 * @param spectralType
	 *            the spectralType to set
	 */
	public void setSpectralType(String spectralType) {
		this.spectralType = spectralType;
	}

	/**
	 * @param discoverer
	 *            the discoverer to set
	 */
	public void setDiscoverer(String discoverer) {
		this.discoverer = discoverer;
	}

	/**
	 * @param ra the ra to set
	 */
	public void setRa(RAInfo ra) {
		this.ra = ra;
	}

	/**
	 * @param dec the dec to set
	 */
	public void setDec(DecInfo dec) {
		this.dec = dec;
	}
}
