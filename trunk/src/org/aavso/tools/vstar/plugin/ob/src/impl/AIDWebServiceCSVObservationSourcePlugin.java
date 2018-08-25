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
 * VSX web service.
 */
public class AIDWebServiceCSVObservationSourcePlugin extends
		AIDWebServiceObservationSourcePluginBase {

	public AIDWebServiceCSVObservationSourcePlugin() {
		super("&csv");
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {

		return new VSXAIDAttributeObservationRetriever();
	}

	class VSXAIDAttributeObservationRetriever extends
			AbstractObservationRetriever {

		public VSXAIDAttributeObservationRetriever() {
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

						pageNum = requestObservationDetails(document,
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

		private Integer requestObservationDetails(Document document,
				Integer pageNum) throws ObservationReadError {

			// Has an observation count been supplied?
			// If so, more observations remain than the ones about to be
			// retrieved here.
			// TODO: only needed the first time or even get from info object?
			Integer obsCount = null;
			NodeList obsCountNodes = document.getElementsByTagName("Count");
			if (obsCountNodes.getLength() != 0) {
				Element obsCountElt = (Element) obsCountNodes.item(0);
				obsCount = Integer.parseInt(obsCountElt.getTextContent());
			}

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

			if (dataNodes.getLength() == 1) {
				Element dataElt = (Element) dataNodes.item(0);
				String data = getCharacterDataFromElement(dataElt);

				// byte[] allBytes = null;
				// List<byte[]> byteArrayList = new ArrayList<byte[]>();
				// int totalBytes = 0;
				try {
					BufferedReader streamReader = new BufferedReader(
							new StringReader(data));

					// Obtain bytes from stream in order to re-use in analyser
					// and reader.
					// int lineCount = 0;
					// String line;
					// while ((line = streamReader.readLine()) != null) {
					// byte[] bytes = line.getBytes();
					// byteArrayList.add(bytes);
					// totalBytes += bytes.length + 1;
					// lineCount++;
					// }

					// int i = 0;
					// allBytes = new byte[total];
					// for (byte[] bytes : byteArrayList) {
					// for (byte b : bytes) {
					// allBytes[i++] = b;
					// }
					// allBytes[i++] = '\n';
					// }

					CsvReader csvReader = new CsvReader(streamReader);

					if (csvReader.readHeaders()) {

						while (csvReader.readRecord()) {
							ValidObservation ob = retrieveNextObservation(csvReader);

							if (ob != null) {
								collectObservation(ob);
							}

							incrementProgress();
						}
					}

					// Analyse the observation file and create an observation
					// retriever.
					// ObservationSourceAnalyser analyser = new
					// ObservationSourceAnalyser(
					// new LineNumberReader(new InputStreamReader(
					// new ByteArrayInputStream(allBytes))),
					// getInputName());
					// analyser.analyse();
					//
					// AbstractObservationRetriever retriever = new
					// TextFormatObservationReader(
					// new LineNumberReader(new InputStreamReader(
					// new ByteArrayInputStream(allBytes))),
					// analyser);
					//
					// retriever.retrieveObservations();

				} catch (Exception e) {
					// TODO: move analyser creation into reader class so we can
					// handle
					// this more efficiently, passing top-level stream not
					// reader...or perhaps
					// pass lines, handling exception there
				}
			}

			if (pageNum != null) {
				pageNum++;
			}

			return pageNum;
		}

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
		 * @return The observation
		 * @throws ObservationReadError
		 *             if an error occurred during observation processing.
		 */
		private ValidObservation retrieveNextObservation(CsvReader reader)
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
						System.out.printf("Error on %s", header);
					}
				}
			} catch (IOException e) {
				// No such header
				System.out.printf("no %s for obs Id %d", lastHeader, id);
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