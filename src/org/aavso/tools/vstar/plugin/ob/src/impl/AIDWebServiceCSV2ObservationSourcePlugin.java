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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.MTypeType;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidationType;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.exception.ObservationValidationError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.ui.VStar;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.util.locale.LocaleProps;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.csvreader.CsvReader;

/**
 * This intrinsic observation source plug-in retrieves AID observations via the
 * experimental, optimised VSX web service.
 */
public class AIDWebServiceCSV2ObservationSourcePlugin extends
		AIDWebServiceObservationSourcePluginBase {

	public AIDWebServiceCSV2ObservationSourcePlugin() {
		super("api.object", "&csv");
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new VSXAIDCSVObservationRetriever();
	}

	@Override
	protected String addURLs(String auid) {

		String urlStr = null;

		// Collect series as a comma-delimited short name list.
		StringBuffer seriesBuf = new StringBuffer();
		List<SeriesType> seriesList = starSelector.getSelectedSeries();

		for (int i = 0; i < seriesList.size(); i++) {
			SeriesType series = seriesList.get(i);
			seriesBuf.append(series.getShortName());
			if (i < seriesList.size() - 1) {
				seriesBuf.append(",");
			}
		}

		if (starSelector.wantAllData()) {
			// Request AID data for object over whole time range and for the
			// requested series.
			urlStr = createAIDUrlForAUID(auid, seriesBuf.toString(),
					starSelector.getObsCodes(),
					starSelector.loadMinimalFields());
		} else {
			// Request AID data for object over a range and for the
			// requested series.
			urlStr = createAIDUrlForAUID(auid, starSelector.getMinDate()
					.getJulianDay(), starSelector.getMaxDate().getJulianDay(),
					seriesBuf.toString(), starSelector.getObsCodes(),
					starSelector.loadMinimalFields());
		}

		urlStrs.add(urlStr);

		return urlStr;
	}

	class VSXAIDCSVObservationRetriever extends AbstractObservationRetriever {

		public VSXAIDCSVObservationRetriever() {
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

			// Read observations over potentially many "pages" for each URL.
			String urlStr = urlStrs.get(0);

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

			NodeList obsCountNodes = document.getElementsByTagName("Count");
			if (obsCountNodes.getLength() != 0) {
				Element obsCountElt = (Element) obsCountNodes.item(0);
				obsCount = Integer.parseInt(obsCountElt.getTextContent());
			}

			if (obsCount == null) {
				pageNum = null;
			}

			NodeList dataNodes = document.getElementsByTagName("Data");

			if (dataNodes.getLength() == 1) {
				Element dataElt = (Element) dataNodes.item(0);
				String data = getCharacterDataFromElement(dataElt);

				BufferedReader streamReader = new BufferedReader(
						new StringReader(data));

				try {
					CsvReader csvReader = new CsvReader(streamReader);

					if (csvReader.readHeaders()) {
						int recordNum = 1;

						while (csvReader.readRecord()) {
							ValidObservation ob = retrieveNextObservation(
									csvReader, recordNum);

							if (ob != null) {
								collectObservation(ob);
							}

							incrementProgress();

							recordNum++;
						}
					} else {
						throw new ObservationReadError(
								"No CSV header in AID data stream");
					}
				} catch (Exception e) {
					throw new ObservationReadError(e.getLocalizedMessage());
				}
			} else {
				throw new ObservationReadError(
						"Only one Data element expected in AID CSV stream");
			}

			if (pageNum != null) {
				pageNum++;
			}

			return pageNum;
		}

		/**
		 * Extract and return CDATA element text from the specified element.
		 * 
		 * @param e
		 *            The element possibly containing CDATA
		 * @return The element's CDATA text
		 */
		private String getCharacterDataFromElement(Element e) {
			String data = "";

			Node child = e.getFirstChild();
			if (child instanceof CharacterData) {
				CharacterData cd = (CharacterData) child;
				data = cd.getData();
			}

			return data;
		}

		/**
		 * Retrieve next observation from CSV.
		 * 
		 * @param reader
		 *            The CSV reader
		 * @param recordNum
		 *            The record number
		 * @return The observation read from the current CSV record
		 * @throws ObservationReadError
		 *             if an error occurred during observation processing.
		 */
		private ValidObservation retrieveNextObservation(CsvReader reader,
				int recordNum) throws ObservationReadError {

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
			String cMag = null;
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

			String lastHeader = null;

			try {
				for (String header : reader.getHeaders()) {

					lastHeader = header;

					if (interrupted)
						break;

					String value = reader.get(header);

					try {
						if ("obsID".equalsIgnoreCase(header)) {
							id = Integer.parseInt(value);
						} else if ("JD".equalsIgnoreCase(header)) {
							jd = getPossiblyNullDouble(value);
						} else if ("mag".equalsIgnoreCase(header)) {
							mag = getPossiblyNullDouble(value);
							Magnitude magnitude = magnitudeFieldValidator
									.validate(value);
							mag = magnitude.getMagValue();
							// TODO: can this occur in AID now (as ":")?
							isUncertain = magnitude.isUncertain();
						} else if ("uncert".equalsIgnoreCase(header)) {
							Double uncertainty = getPossiblyNullDouble(value);
							if (uncertainty != null) {
								error = uncertainty;
							}
						} else if ("fainterThan".equalsIgnoreCase(header)) {
							fainterThan = "1".equals(value);
						} else if ("band".equalsIgnoreCase(header)) {
							band = SeriesType.getSeriesFromShortName(value);
						} else if ("transformed".equalsIgnoreCase(header)) {
							transformed = "1".equalsIgnoreCase(value);
						} else if ("airmass".equalsIgnoreCase(header)) {
							airmass = value;
						} else if ("compStar1".equalsIgnoreCase(header)) {
							comp1 = value;
						} else if ("compStar2".equalsIgnoreCase(header)) {
							comp2 = value;
						} else if ("CMag".equalsIgnoreCase(header)) {
							cMag = value;
						} else if ("KMag".equalsIgnoreCase(header)) {
							kMag = value;
							// TODO: no hjd in AID/CSV obs?
							// } else if ("hjd".equalsIgnoreCase(header)) {
							// Double hjd = getPossiblyNullDouble(value);
							// if (hjd != null) {
							// hJD = new DateInfo(hjd);
							// }
						} else if ("group".equalsIgnoreCase(header)) {
							group = value;
						} else if ("by".equalsIgnoreCase(header)) {
							obscode = value;
						} else if ("obstype".equalsIgnoreCase(header)) {
							obsType = value;
						} else if ("charts".equalsIgnoreCase(header)) {
							charts = value;
						} else if ("comCode".equalsIgnoreCase(header)) {
							commentCode = value;
						} else if ("comment".equalsIgnoreCase(header)) {
							comments = value;
						} else if ("val".equalsIgnoreCase(header)) {
							valType = getValidationType(value);
						} else if ("mtype".equalsIgnoreCase(header)) {
							mType = getMType(value);
						} else if ("credit".equalsIgnoreCase(header)) {
							credit = value;
						} else if ("adsRef".equalsIgnoreCase(header)) {
							pubref = value;
						} else if ("digitizer".equalsIgnoreCase(header)) {
							digitizer = value;
						} else if ("starName".equalsIgnoreCase(header)) {
							name = value;
						}
						// TODO: obsAffil,software,obsName,obsCountry
					} catch (ObservationValidationError e) {
						log(Level.WARNING, String.format(
								"Error for column %s: \"%s\"", header, value),
								recordNum);
					} catch (NumberFormatException e) {
						log(Level.WARNING, String.format(
								"Number expected for column %s, was \"%s\"",
								header, value), recordNum);
					}
				}
			} catch (IOException e) {
				// No such header for this record
				log(Level.WARNING,
						String.format("No \"%s\" column", lastHeader),
						recordNum);
			}

			ValidObservation ob = null;
			
			if (jd != null && mag != null && error != null) {

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
				ob.setCMag(cMag);
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

	private void log(Level level, String msg, int recordNum) {
		VStar.LOGGER
				.log(level, String.format("%s (record %d)", msg, recordNum));
	}
}
