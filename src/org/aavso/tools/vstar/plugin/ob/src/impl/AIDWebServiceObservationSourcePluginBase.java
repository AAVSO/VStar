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
package org.aavso.tools.vstar.plugin.ob.src.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.data.MTypeType;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.MagnitudeModifier;
import org.aavso.tools.vstar.data.ValidationType;
import org.aavso.tools.vstar.data.validation.MagnitudeFieldValidator;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.input.database.VSXWebServiceStarInfoSource;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.dialog.StarSelectorDialog;
import org.aavso.tools.vstar.ui.mediator.NewStarType;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;
import org.aavso.tools.vstar.util.help.Help;
import org.aavso.tools.vstar.util.locale.LocaleProps;

public abstract class AIDWebServiceObservationSourcePluginBase extends
		ObservationSourcePluginBase {

	protected static final int MAX_OBS_AT_ONCE = 50000;
	protected static final String BASE_URL = ResourceAccessor.getVsxApiUrlBase();
	protected String view;
	protected String method;
	protected static final MagnitudeFieldValidator magnitudeFieldValidator = new MagnitudeFieldValidator();
	protected StarInfo info;
	protected StarSelectorDialog starSelector;
	protected List<String> urlStrs;

	/**
	 * Constructor
	 * 
	 * @param view
	 *            view type, e.g. api.object
	 * @param method
	 *            response method: &csv, &att
	 */
	public AIDWebServiceObservationSourcePluginBase(String view, String method) {
		super();
		assert ("&csv".equals(method) || "&att".equals(method));
		this.view = view;
		this.method = method;
		this.info = null;
	}

	/**
	 * Given an AUID, min and max JD, return a web service URL.
	 * 
	 * @param auid
	 *            The AUID of the target.
	 * @param minJD
	 *            The minimum JD of the range to be loaded.
	 * @param maxJD
	 *            The maximum JD of the range to be loaded.
	 * @return The URL string necessary to load data for the target and JD
	 *         range.
	 */
	public String createAIDUrlForAUID(String auid, double minJD, double maxJD) {

		StringBuffer urlStrBuf = new StringBuffer(BASE_URL);

		urlStrBuf.append(view);
		urlStrBuf.append("&ident=");
		urlStrBuf.append(auid);
		urlStrBuf.append("&data=");
		urlStrBuf.append(MAX_OBS_AT_ONCE);
		urlStrBuf.append("&fromjd=");
		urlStrBuf.append(minJD);
		urlStrBuf.append("&tojd=");
		urlStrBuf.append(maxJD);
		urlStrBuf.append(method);
		urlStrBuf.append("&mtype=std");

		return urlStrBuf.toString();
	}

	/**
	 * Given an AUID, min and max JD, and a series, return a web service URL.
	 * 
	 * @param auid
	 *            The AUID of the target.
	 * @param minJD
	 *            The minimum JD of the range to be loaded.
	 * @param maxJD
	 *            The maximum JD of the range to be loaded.
	 * @param series
	 *            The series to be loaded; may be a comma-delimited list or null
	 * @param obsCodes
	 *            The observer codes to be loaded; may be a comma-delimited list
	 *            or null
	 * @param minFields
	 *            Whether or not to request a minimum number of fields.
	 * @return The URL string necessary to load data for the target and JD
	 *         range.
	 */
	public String createAIDUrlForAUID(String auid, double minJD, double maxJD,
			String series, String obsCodes, boolean minFields) {

		StringBuffer urlStrBuf = new StringBuffer(BASE_URL);

		urlStrBuf.append(view);
		urlStrBuf.append("&ident=");
		urlStrBuf.append(auid);
		urlStrBuf.append("&data=");
		urlStrBuf.append(MAX_OBS_AT_ONCE);
		urlStrBuf.append("&fromjd=");
		urlStrBuf.append(minJD);
		urlStrBuf.append("&tojd=");
		urlStrBuf.append(maxJD);
		urlStrBuf.append(method);
		if (series != null) {
			urlStrBuf.append("&band=");
			urlStrBuf.append(series);
		}
		if (obsCodes != null) {
			urlStrBuf.append("&obscode=");
			urlStrBuf.append(obsCodes);
		}
		if (minFields) {
			urlStrBuf.append("&minfields");
		}
		urlStrBuf.append("&mtype=std");

		return urlStrBuf.toString();
	}

	/**
	 * Given an AUID and a series, return a web service URL to load data for all
	 * times with the specified constraints.
	 * 
	 * @param auid
	 *            The AUID of the target.
	 * @param series
	 *            The series to be loaded; may be a comma-delimited list or null
	 * @param obsCodes
	 *            The observer codes to be loaded; may be a comma-delimited list
	 *            or null
	 * @param minFields
	 *            Whether or not to request a minimum number of fields.
	 * @return The URL string necessary to load data for the target and JD
	 *         range.
	 */
	public String createAIDUrlForAUID(String auid, String series,
			String obsCodes, boolean minFields) {

		StringBuffer urlStrBuf = new StringBuffer(BASE_URL);

		urlStrBuf.append(view);
		urlStrBuf.append("&ident=");
		urlStrBuf.append(auid);
		urlStrBuf.append("&data=");
		urlStrBuf.append(MAX_OBS_AT_ONCE);
		urlStrBuf.append(method);
		if (series != null) {
			urlStrBuf.append("&band=");
			urlStrBuf.append(series);
		}
		if (obsCodes != null) {
			urlStrBuf.append("&obscode=");
			urlStrBuf.append(obsCodes);
		}
		if (minFields) {
			urlStrBuf.append("&minfields");
		}
		urlStrBuf.append("&mtype=std");

		return urlStrBuf.toString();
	}

	/**
	 * Given an AUID return a web service URL for all data for the target.
	 * 
	 * @param auid
	 *            The AUID of the target.
	 * @return The URL string necessary to load data for the target and JD
	 *         range.
	 */
	public String createAIDUrlForAUID(String auid) {

		StringBuffer urlStrBuf = new StringBuffer(BASE_URL);

		urlStrBuf.append(view);
		urlStrBuf.append("&ident=");
		urlStrBuf.append(auid);
		urlStrBuf.append("&data=");
		urlStrBuf.append(MAX_OBS_AT_ONCE);
		urlStrBuf.append(method);
		urlStrBuf.append("&mtype=std");

		return urlStrBuf.toString();
	}

	@Override
	public String getDisplayName() {
		return LocaleProps.get("FILE_MENU_NEW_STAR_FROM_DATABASE");
	}

	@Override
	public String getDescription() {
		return LocaleProps.get("DATABASE_OBS_SOURCE");
	}

	@Override
	public String getDocName() {
		return Help.getAIDWebServiceHelpPage();
	}

	@Override
	public InputType getInputType() {
		return InputType.URL;
	}

	@Override
	public List<URL> getURLs() throws Exception {
		urlStrs = new ArrayList<String>();
		List<URL> urls = new ArrayList<URL>();

		starSelector = StarSelectorDialog.getInstance();
		starSelector.setPluginDocName(getDocName());

		// Ask for object information.
		starSelector.showDialog();

		if (!starSelector.isCancelled()) {
			setAdditive(starSelector.isLoadAdditive());

			String auid = starSelector.getAuid();
			String starName = starSelector.getStarName();

			// Get the star name if we don't have it.
			VSXWebServiceStarInfoSource infoSrc = new VSXWebServiceStarInfoSource();

			if (starSelector.wantAllData()) {
				// Star info for all data
				if (starName == null) {
					info = infoSrc.getStarByAUID(auid);
					starName = info.getDesignation();
				} else {
					info = infoSrc.getStarByName(starName);
					auid = info.getAuid();
				}
			} else {
				// Star info by JD range
				if (starName == null) {
					info = infoSrc.getStarByAUID(auid, starSelector
							.getMinDate().getJulianDay(), starSelector
							.getMaxDate().getJulianDay());
					starName = info.getDesignation();
				} else {
					info = infoSrc.getStarByName(starName, starSelector
							.getMinDate().getJulianDay(), starSelector
							.getMaxDate().getJulianDay());
					auid = info.getAuid();
				}
			}

			setVelaFilterStr(starSelector.getVeLaFilter());

			String urlStr = addURLs(auid);

			// Return a list containing one URL to satisfy logic in new star
			// from obs source plug-in task. We are actually interested in just
			// the partial URL string we constructed, which will be used in
			// retrieveObservations().
			urls.add(new URL(urlStr));			
		} else {
			throw new CancellationException();
		}

		return urls;
	}

	@Override
	public NewStarType getNewStarType() {
		return NewStarType.NEW_STAR_FROM_DATABASE;
	}

	@Override
	public String getInputName() {
		return super.getInputName();
	}

	@Override
	public String getGroup() {
		return "Internal";
	}

	/**
	 * Set the star information object. This is primarily so we can test
	 * requestObservations() independent of the rest of the plug-in code.
	 * 
	 * @param info
	 *            the info to set
	 */
	public void setInfo(StarInfo info) {
		this.info = info;
	}

	/**
	 * Set the URL. This is primarily so we can test requestObservations()
	 * independent of the rest of the plug-in code.
	 * 
	 * @param url
	 */
	public void setUrl(String urlStr) {
		urlStrs = new ArrayList<String>();
		urlStrs.add(urlStr);
	}

	/**
	 * Create one or more URLs, adding them to the URL string list, for the
	 * specified target AUID.
	 * 
	 * @param auid
	 *            The target AUID.
	 * @return A representative URL.
	 */
	abstract protected String addURLs(String auid);

	protected Double getPossiblyNullDouble(String valStr) {
		Double num = null;

		try {
			if (valStr != null) {
				num = Double.parseDouble(valStr);
			}
		} catch (NumberFormatException e) {
			// The value will default to null.
		}

		return num;
	}

	protected Magnitude getMagnitude(double mag, double error,
			boolean fainterThan, boolean isUncertain) {

		MagnitudeModifier modifier = fainterThan ? MagnitudeModifier.FAINTER_THAN
				: MagnitudeModifier.NO_DELTA;

		return new Magnitude(mag, modifier, isUncertain, error);
	}

	protected ValidationType getValidationType(String valflag) {
		ValidationType type;

		// - V,Z,U => Good
		// - T,N => discrepant
		// - Y(,Q) filtered out server side

		switch (valflag) {
		case "V":
		case "Z":
		case "U":
			type = ValidationType.GOOD;
			break;

		case "T":
		case "N":
			type = ValidationType.DISCREPANT;
			break;

		default:
			// In case anything else slips through, e.g. Y,Q.
			type = ValidationType.BAD;
			break;
		}

		return type;
	}

	protected MTypeType getMType(String mtypeStr) {
		MTypeType result = MTypeType.STD;

		// If mtypeStr is null, we use the ValidObservation's
		// constructed default (standard magnitude type).
		//
		// Note that we should only ever see STD here anyway!

		if (mtypeStr != null || "".equals(mtypeStr)) {
			if (mtypeStr == "DIFF") {
				result = MTypeType.DIFF;
			} else if (mtypeStr == "STEP") {
				result = MTypeType.STEP;
			}
		}

		return result;
	}
}