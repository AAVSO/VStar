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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.MTypeType;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidationType;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
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
public class AIDWebServiceXMLAttributeObservationSourcePlugin extends
		AIDWebServiceObservationSourcePluginBase {

	public AIDWebServiceXMLAttributeObservationSourcePlugin() {
		super("api.object", "&att");
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new VSXAIDAttributeObservationRetriever();
	}

	@Override
	protected String addURLs(String auid) {
		String urlStr = null;

		// Create a list of URLs with different series for the same target
		// and time range.
		for (SeriesType series : starSelector.getSelectedSeries()) {

			if (starSelector.wantAllData()) {
				// Request all AID data for object for requested series.
				urlStr = createAIDUrlForAUID(auid);
			} else {
				// Request AID data for object over a range and for the
				// zeroth requested series.
				urlStr = createAIDUrlForAUID(auid, starSelector.getMinDate()
						.getJulianDay(), starSelector.getMaxDate()
						.getJulianDay(), series.getShortName(), null, false);
			}

			urlStrs.add(urlStr);
		}

		return urlStr;
	}

	class VSXAIDAttributeObservationRetriever extends
			AbstractObservationRetriever {

		public VSXAIDAttributeObservationRetriever() {
			super(getVelaFilterStr());
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

			// Iterate over each series-based URL reading observations over
			// potentially many "pages" for each URL.
			for (String urlStr : urlStrs) {
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

						pageNum = requestObservationDetails(document, pageNum);

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
		}

		@Override
		public String getSourceType() {
			return LocaleProps.get("DATABASE_OBS_SOURCE");
		}

		@Override
		public String getSourceName() {
			return info.getDesignation();
		}

		// Helpers

		/**
		 * Retrieve all observation details from the document.
		 * 
		 * @param document
		 *            The document from which to extract observations.
		 * @param pageNum
		 *            The page number of the document to read.
		 * @return The next page number to read or null if not a multi-page
		 *         document.
		 * @throws ObservationReadError
		 *             If an error occurs when reading the document.
		 */
		private Integer requestObservationDetails(Document document,
				Integer pageNum) throws ObservationReadError {

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
			Double error = 0.0;
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
					transformed = "true".equalsIgnoreCase(nodeValue);
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

			if (id != null && jd != null && mag != null && error != null
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
	}
}