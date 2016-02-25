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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.MTypeType;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.MagnitudeModifier;
import org.aavso.tools.vstar.data.ObsType;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidationType;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.input.database.VSXWebServiceXMLStarInfoSource;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.dialog.StarSelectorDialog;
import org.aavso.tools.vstar.ui.mediator.NewStarType;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This intrinsic observation source plug-in retrieves AID observations via the
 * VSX web service.
 * 
 * TODO:<br/>
 * - try on ~10,000 obs to see whether N in data=N (MAX_OBS_AT_ONCE) is OK. 
 */
public class VSXWebServiceAIDObservationSourcePlugin extends
		ObservationSourcePluginBase {

	private final static int MAX_OBS_AT_ONCE = 500;

	private String baseVsxUrlString;
	private String urlStr;
	private StarInfo info;

	public VSXWebServiceAIDObservationSourcePlugin() {
		baseVsxUrlString = "https://www.aavso.org/vsx/index.php?view=api.object";
		info = null;
	}

	@Override
	public String getDisplayName() {
		return LocaleProps.get("FILE_MENU_NEW_STAR_FROM_DATABASE") + " (VSX)";
	}

	@Override
	public String getDescription() {
		// TODO: localise
		return "AAVSO International Database observation source";
	}

	@Override
	public InputType getInputType() {
		return InputType.URL;
	}

	@Override
	public List<URL> getURLs() throws Exception {
		List<URL> urls = new ArrayList<URL>();

		StarSelectorDialog starSelector = StarSelectorDialog.getInstance();

		// Ask for object information.
		starSelector.showDialog();

		if (!starSelector.isCancelled()) {
			String auid = starSelector.getAuid();
			String starName = starSelector.getStarName();

			// Get the star name if we don't have it.
			VSXWebServiceXMLStarInfoSource infoSrc = new VSXWebServiceXMLStarInfoSource();

			if (starName == null) {
				info = infoSrc.getStarByAUID(null, auid);
				starName = info.getDesignation();
			} else {
				info = infoSrc.getStarByName(null, starName);
				auid = info.getAuid();
			}

			int numObs = MAX_OBS_AT_ONCE;

			// String name = starName.replace("+", "%2B").replace(" ", "+");

			urlStr = baseVsxUrlString + "&ident=" + auid + "&data=" + numObs;

			if (!starSelector.wantAllData()) {
				urlStr += "&fromjd=" + starSelector.getMinDate().getJulianDay();
				urlStr += "&tojd=" + starSelector.getMaxDate().getJulianDay();
			}

			urls.add(new URL(urlStr));
		} else {
			throw new CancellationException();
		}

		// To satisfy logic in new star from obs source plug-in task. We are
		// actually interested in just the partial URL string we constructed.
		return urls;
	}

	@Override
	public NewStarType getNewStarType() {
		return NewStarType.NEW_STAR_FROM_DATABASE;
	}

	@Override
	public String getInputName() {
		// TODO Auto-generated method stub
		return super.getInputName();
	}

	@Override
	public String getGroup() {
		return "Internal";
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new VSXAIDObservationRetriever();
	}

	class VSXAIDObservationRetriever extends AbstractObservationRetriever {

		@Override
		public Integer getNumberOfRecords() throws ObservationReadError {
			return info.getObsCount();
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			Integer pageNum = 1;

			do {
				if (interrupted)
					break;
				pageNum = requestObservations(pageNum);
			} while (pageNum != null);
		}

		@Override
		public String getSourceType() {
			// TODO: localise
			return "AAVSO International Database";
		}

		@Override
		public String getSourceName() {
			return getValidObservations().get(0).getName();
		}

		// Helpers

		private Integer requestObservations(Integer pageNum)
				throws ObservationReadError {

			Integer id = null;

			try {
				String currUrlStr = urlStr;
				if (pageNum != null) {
					currUrlStr += "&page=" + pageNum;
				}

				URL vsxUrl = new URL(currUrlStr);

				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(vsxUrl.openStream());

				document.getDocumentElement().normalize();

				// Has an observation count element been supplied?
				Integer obsCount = null;
				NodeList obsCountNodes = document.getElementsByTagName("Count");
				if (obsCountNodes.getLength() != 0) {
					Element obsCountElt = (Element) obsCountNodes.item(0);
					obsCount = Integer.parseInt(obsCountElt.getTextContent());
				}

				if (obsCount == null) {
					pageNum = null;
				}

				NodeList obsNodes = document
						.getElementsByTagName("Observation");
				for (int i = 0; i < obsNodes.getLength(); i++) {

					if (interrupted)
						break;

					NodeList obsDetails = obsNodes.item(i).getChildNodes();

					double jd = Double.POSITIVE_INFINITY;
					double mag = Double.POSITIVE_INFINITY;
					double error = 0;
					SeriesType band = null;
					String obscode = null;
					ObsType obsType = null;
					ValidationType valType = null;
					String comp1 = null;
					String comp2 = null;
					String kMag = null;
					String charts = null;
					String commentCode = null;
					String comments = null;
					boolean transformed = false;
					boolean fainterThan = false;
					boolean isUncertain = false;
					DateInfo hJD = null;
					String airmass = null;
					String group = null;
					MTypeType mType = null;
					String credit = null;
					String pubref = null;
					String digitizer = null;
					String name = info.getDesignation();

					for (int j = 0; j < obsDetails.getLength(); j++) {

						if (interrupted)
							break;

						Element detailElt = (Element) obsDetails.item(j);
						String nodeName = detailElt.getNodeName();
						String nodeValue = detailElt.getTextContent();

						if ("Id".equalsIgnoreCase(nodeName)) {
							id = Integer.parseInt(nodeValue);
						} else if ("JD".equalsIgnoreCase(nodeName)) {
							jd = Double.parseDouble(nodeValue);
						} else if ("Mag".equalsIgnoreCase(nodeName)) {
							mag = Double.parseDouble(nodeValue);
						} else if ("uncertainty".equalsIgnoreCase(nodeName)) {
							if (!nodeValue.startsWith("NA")) {
								// We've seen NA, NAN
								error = Double.parseDouble(nodeValue);
							}
						} else if ("uncertain".equalsIgnoreCase(nodeName)) {
							isUncertain = Boolean.parseBoolean(nodeValue);
						} else if ("fainterthan".equalsIgnoreCase(nodeName)) {
							fainterThan = Boolean.parseBoolean(nodeValue);
						} else if ("band".equalsIgnoreCase(nodeName)) {
							band = SeriesType.getSeriesFromShortName(nodeValue);
						} else if ("transformed".equalsIgnoreCase(nodeName)) {
							transformed = "yes".equals(nodeValue);
						} else if ("airmass".equalsIgnoreCase(nodeName)) {
							airmass = nodeValue;
						} else if ("comp1".equalsIgnoreCase(nodeName)) {
							comp1 = nodeValue;
						} else if ("comp2".equalsIgnoreCase(nodeName)) {
							comp2 = nodeValue;
						} else if ("KMag".equalsIgnoreCase(nodeName)) {
							kMag = nodeValue;
						} else if ("hjd".equalsIgnoreCase(nodeName)) {
							hJD = new DateInfo(Double.parseDouble(nodeValue));
						} else if ("group".equalsIgnoreCase(nodeName)) {
							group = nodeValue;
						} else if ("obscode".equalsIgnoreCase(nodeName)) {
							obscode = nodeValue;
						} else if ("obstype".equalsIgnoreCase(nodeName)) {
							// TODO: change type from ObsType in
							// ValidObservation to String or make ObsType a
							// pseudo-enum like SeriesType so we can add new
							// ones; we're getting this string from the
							// database, so best to just use the AID's value
							obsType = ObsType.getObsTypeFromName(nodeValue);
						} else if ("charts".equalsIgnoreCase(nodeName)) {
							charts = nodeValue;
						} else if ("commentcode".equalsIgnoreCase(nodeName)) {
							commentCode = nodeValue;
						} else if ("comments".equalsIgnoreCase(nodeName)) {
							comments = nodeValue;
						} else if ("valflag".equalsIgnoreCase(nodeName)) {
							valType = getValidationType(nodeValue);
						} else if ("mtype".equalsIgnoreCase(nodeName)) {
							mType = getMType(nodeValue);
						} else if ("credit".equalsIgnoreCase(nodeName)) {
							credit = nodeValue;
						} else if ("pubref".equalsIgnoreCase(nodeName)) {
							pubref = nodeValue;
						} else if ("digitizer".equalsIgnoreCase(nodeName)) {
							digitizer = nodeValue;
						} else if ("name".equalsIgnoreCase(nodeName)) {
							name = nodeValue;
						}
					}

					if (jd != Double.POSITIVE_INFINITY
							&& mag != Double.POSITIVE_INFINITY) {

						ValidObservation ob = new ValidObservation();

						if (id != null) {
							ob.setRecordNumber(id);
						}

						ob.setDateInfo(new DateInfo(jd));
						ob.setMagnitude(getMagnitude(mag, error, fainterThan,
								isUncertain));
						ob.setBand(band);
						ob.setObsCode(obscode);
						ob.setObsType(obsType);
						ob.setValidationType(valType);
						ob.setCompStar1(comp1);
						ob.setCompStar2(comp2);
						ob.setKMag(kMag);
						ob.setHJD(hJD);
						ob.setCharts(charts);
						ob.setCommentCode(commentCode);
						ob.setComments(comments);
						ob.setTransformed(transformed);
						ob.setAirmass(airmass);
						ob.setGroup(group);
						ob.setMType(mType);
						ob.setCredit(credit);
						ob.setADSRef(pubref);
						ob.setDigitizer(digitizer);
						ob.setName(name);

						collectObservation(ob);

						incrementProgress();
					}
				}
			} catch (MalformedURLException e) {
				throw new ObservationReadError(
						"Unable to obtain information for " + info.getAuid());
			} catch (ParserConfigurationException e) {
				throw new ObservationReadError(
						"Unable to obtain information for " + info.getAuid());
			} catch (SAXException e) {
				throw new ObservationReadError(
						"Unable to obtain information for " + info.getAuid());
			} catch (IOException e) {
				throw new ObservationReadError(
						"Unable to obtain information for " + info.getAuid());
			}

			if (pageNum != null) {
				pageNum++;
			}

			return pageNum;
		}

		private Magnitude getMagnitude(double mag, double error,
				boolean fainterThan, boolean isUncertain) {

			MagnitudeModifier modifier = fainterThan ? MagnitudeModifier.FAINTER_THAN
					: MagnitudeModifier.NO_DELTA;

			return new Magnitude(mag, modifier, isUncertain, error);
		}

		/*
		 * According to:
		 * https://sourceforge.net/apps/mediawiki/vstar/index.php?title
		 * =AAVSO_International_Database_Schema
		 * (https://sourceforge.net/apps/mediawiki
		 * /vstar/index.php?title=Valflag:) we have: Z = Prevalidated, P =
		 * Published observation, T = Discrepant, V = Good, Y = Deleted
		 * (filtered out via SQL). Our query converts any occurrence of 'T' to
		 * 'D'. Currently we convert everything to Good (V,G), Discrepant (D),
		 * or Prevalidated (Z) below.
		 */
		private ValidationType getValidationType(String valflag) {
			ValidationType type;

			if ("Z".equals(valflag)) {
				type = ValidationType.PREVALIDATION;
			} else if ("D".equals(valflag)) {
				type = ValidationType.DISCREPANT;
			} else {
				type = ValidationType.GOOD;
			}

			return type;
		}

		private MTypeType getMType(String mtypeStr) {
			MTypeType result = null;

			// If mtypeStr is null, we use the ValidObservation's
			// constructed default (standard magnitude type).

			if (mtypeStr != null) {
				if (mtypeStr == "DIFF") {
					result = MTypeType.DIFF;
				} else if (mtypeStr == "STEP") {
					result = MTypeType.STEP;
				}
			}

			return result;
		}
	}
}