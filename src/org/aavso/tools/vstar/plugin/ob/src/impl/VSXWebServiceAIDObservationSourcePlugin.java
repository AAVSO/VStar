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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.MTypeType;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.MagnitudeModifier;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidationType;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.input.database.VSXWebServiceStarInfoSource;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.dialog.StarSelectorDialog;
import org.aavso.tools.vstar.ui.mediator.NewStarType;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This intrinsic observation source plug-in retrieves AID observations via the
 * VSX web service.
 */
public class VSXWebServiceAIDObservationSourcePlugin extends
		ObservationSourcePluginBase {

	private final static int MAX_OBS_AT_ONCE = 1000;
	private final static String BASE_URL = "https://www.aavso.org/vsx/index.php?view=api.object";

	private String urlStr;
	private StarInfo info;

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
	public static String createAIDUrlForAUID(String auid, double minJD,
			double maxJD) {

		StringBuffer urlStrBuf = new StringBuffer(BASE_URL);

		urlStrBuf.append("&ident=");
		urlStrBuf.append(auid);
		urlStrBuf.append("&data=");
		urlStrBuf.append(MAX_OBS_AT_ONCE);
		urlStrBuf.append("&fromjd=");
		urlStrBuf.append(minJD);
		urlStrBuf.append("&tojd=");
		urlStrBuf.append(maxJD);
		urlStrBuf.append("&att");
		urlStrBuf.append("&where=mtype%3D0+or+mtype+is+null");

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
	public static String createAIDUrlForAUID(String auid) {

		StringBuffer urlStrBuf = new StringBuffer(BASE_URL);

		urlStrBuf.append("&ident=");
		urlStrBuf.append(auid);
		urlStrBuf.append("&data=");
		urlStrBuf.append(MAX_OBS_AT_ONCE);
		urlStrBuf.append("&att");
		urlStrBuf.append("&where=mtype%3D0+or+mtype+is+null");

		return urlStrBuf.toString();
	}

	public VSXWebServiceAIDObservationSourcePlugin() {
		// baseVsxUrlString =
		// "https://www.aavso.org/vsx/index.php?view=api.csv";
		info = null;
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
			setAdditive(starSelector.isLoadAdditive());

			String auid = starSelector.getAuid();
			String starName = starSelector.getStarName();

			// Get the star name if we don't have it.
			VSXWebServiceStarInfoSource infoSrc = new VSXWebServiceStarInfoSource();

			if (starName == null) {
				info = infoSrc.getStarByAUID(null, auid);
				starName = info.getDesignation();
			} else {
				info = infoSrc.getStarByName(null, starName);
				auid = info.getAuid();
			}

			if (starSelector.wantAllData()) {
				urlStr = createAIDUrlForAUID(auid);
			} else {
				urlStr = createAIDUrlForAUID(auid, starSelector.getMinDate()
						.getJulianDay(), starSelector.getMaxDate()
						.getJulianDay());
			}

			urls.add(new URL(urlStr));
		} else {
			throw new CancellationException();
		}

		// Return the URLs list to satisfy logic in new star from obs source
		// plug-in task. We are actually interested in just the partial URL
		// string we constructed, which will be used in retrieveObservations().
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

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new VSXAIDObservationRetriever();
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
		this.urlStr = urlStr;
	}

	class VSXAIDObservationRetriever extends AbstractObservationRetriever {

		public VSXAIDObservationRetriever() {
			info.setRetriever(this);
		}

		@Override
		public StarInfo getStarInfo() {
			return info;
		}

		@Override
		public Integer getNumberOfRecords() throws ObservationReadError {
			return info.getObsCount();
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			Integer pageNum = 1;

			do {
				try {
					String currUrlStr = urlStr;
					if (pageNum != null) {
						currUrlStr += "&page=" + pageNum;
					}

					URL vsxUrl = new URL(currUrlStr);

					DocumentBuilderFactory factory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();

					InputStream stream = new UTF8FilteringInputStream(
							vsxUrl.openStream());
					Document document = builder.parse(stream);

					document.getDocumentElement().normalize();

					// pageNum = requestObservationDetailsAsElements(document,
					// pageNum);

					pageNum = requestObservationDetailsAsAttributes(document,
							pageNum);
				} catch (MalformedURLException e) {
					throw new ObservationReadError(
							"Unable to obtain information for "
									+ info.getDesignation());
				} catch (ParserConfigurationException e) {
					throw new ObservationReadError(
							"Unable to obtain information for "
									+ info.getDesignation());
				} catch (SAXException e) {
					throw new ObservationReadError(
							"Unable to obtain information for "
									+ info.getDesignation());
				} catch (IOException e) {
					throw new ObservationReadError(
							"Unable to obtain information for "
									+ info.getDesignation());
				}
			} while (pageNum != null && !interrupted);
		}

		// TODO: replace above with this or delete
		// public void retrieveObservations2() throws ObservationReadError,
		// InterruptedException {
		//
		// try {
		// URL vsxUrl = new URL(urlStr);
		//
		// InputStream stream = vsxUrl.openStream();
		// InputStreamReader reader = new InputStreamReader(stream);
		// // TODO: create CSV reader, read lines, create obs list.
		// } catch (Throwable t) {
		// throw new ObservationReadError(
		// "Error when attempting to read observation source.");
		// } catch (IOException e) {
		// throw new ObservationReadError(
		// "Unable to obtain information for "
		// + info.getDesignation());
		// }
		// }

		@Override
		public String getSourceType() {
			return LocaleProps.get("DATABASE_OBS_SOURCE");
		}

		@Override
		public String getSourceName() {
			return info.getDesignation();
		}

		// Helpers

		private Integer requestObservationDetailsAsElements(Document document,
				Integer pageNum) throws ObservationReadError {

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

			NodeList obsNodes = document.getElementsByTagName("Observation");
			for (int i = 0; i < obsNodes.getLength(); i++) {

				if (interrupted)
					break;

				NodeList obsDetails = obsNodes.item(i).getChildNodes();

				ValidObservation ob = retrieveObservation(new NodeListSequence(
						obsDetails));

				if (ob != null) {
					collectObservation(ob);
				}

				incrementProgress();
			}

			if (pageNum != null) {
				pageNum++;
			}

			return pageNum;
		}

		private Integer requestObservationDetailsAsAttributes(
				Document document, Integer pageNum) throws ObservationReadError {

			// Has an observation count been supplied?
			// If so, more observations remain than the ones about to be
			// retrieved here.
			Integer obsCount = null;

			NodeList dataNodes = document.getElementsByTagName("Data");
			if (dataNodes.getLength() == 1) {
				Element dataElt = (Element) dataNodes.item(0);
				String count = dataElt.getAttribute("Count");
				if (count != null && count.trim().length() != 0) {
					obsCount = Integer.parseInt(count);
				}
			}

			if (obsCount == null) {
				pageNum = null;
			}

			NodeList obsNodes = document.getElementsByTagName("Observation");

			for (int i = 0; i < obsNodes.getLength(); i++) {

				if (interrupted)
					break;

				NamedNodeMap obsDetails = obsNodes.item(i).getAttributes();

				ValidObservation ob = retrieveObservation(new NamedNodeMapSequence(
						obsDetails));

				if (ob != null) {
					collectObservation(ob);
				}

				incrementProgress();
			}

			if (pageNum != null) {
				pageNum++;
			}

			return pageNum;
		}

		/**
		 * Given an XML node of some kind (Element, Node) corresponding to the
		 * details of a single observation, retrieve that observation.
		 * 
		 * @param obsDetails
		 *            A sequence of observation details.
		 * @return The observation.
		 * @throws ObservationReadError
		 *             if an error occurred during observation processing.
		 */
		private ValidObservation retrieveObservation(INodeSequence obsDetails)
				throws ObservationReadError {

			Integer id = null;
			Double jd = null;
			Double mag = null;
			double error = 0;
			SeriesType band = null;
			String obscode = null;
			String obsType = null;
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

				Node detailNode = obsDetails.item(j);
				String nodeName = detailNode.getNodeName();
				String nodeValue = detailNode.getTextContent();

				if ("Id".equalsIgnoreCase(nodeName)) {
					id = Integer.parseInt(nodeValue);
				} else if ("JD".equalsIgnoreCase(nodeName)) {
					jd = getPossiblyNullDouble(nodeValue);
				} else if ("Mag".equalsIgnoreCase(nodeName)) {
					mag = getPossiblyNullDouble(nodeValue);
				} else if ("uncertainty".equalsIgnoreCase(nodeName)) {
					error = getPossiblyNullDouble(nodeValue);
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
					Double hjd = getPossiblyNullDouble(nodeValue);
					if (hjd != null) {
						hJD = new DateInfo(hjd);
					}
				} else if ("group".equalsIgnoreCase(nodeName)) {
					group = nodeValue;
				} else if ("obscode".equalsIgnoreCase(nodeName)) {
					obscode = nodeValue;
				} else if ("obstype".equalsIgnoreCase(nodeName)) {
					obsType = nodeValue;
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

			ValidObservation ob = null;

			if (id != null && jd != null && mag != null
					&& valType != ValidationType.BAD) {

				ob = new ValidObservation();

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
			} else {
				invalidObservations.add(new InvalidObservation(id + "",
						"Invalid"));
			}

			return ob;
		}

		private Magnitude getMagnitude(double mag, double error,
				boolean fainterThan, boolean isUncertain) {

			MagnitudeModifier modifier = fainterThan ? MagnitudeModifier.FAINTER_THAN
					: MagnitudeModifier.NO_DELTA;

			return new Magnitude(mag, modifier, isUncertain, error);
		}

		private ValidationType getValidationType(String valflag) {
			ValidationType type;

			// - V,Z,U => Good
			// - T,N => discrepant
			// - Y(,Q) filtered out server side

			switch (valflag.charAt(0)) {
			case 'V':
			case 'Z':
			case 'U':
				type = ValidationType.GOOD;
				break;

			case 'T':
			case 'N':
				type = ValidationType.DISCREPANT;
				break;

			default:
				// In case anything else slips through, e.g. Y,Q.
				type = ValidationType.BAD;
				break;
			}

			return type;
		}

		private MTypeType getMType(String mtypeStr) {
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

	private Double getPossiblyNullDouble(String valStr) {
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

}