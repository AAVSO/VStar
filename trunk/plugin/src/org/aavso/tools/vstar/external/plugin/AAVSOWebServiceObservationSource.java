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
package org.aavso.tools.vstar.external.plugin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.input.database.VSXWebServiceVOTableStarNameAndAUIDSource;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.dialog.StarSelectorDialog;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * AAVSO Web Service Observation Source plug-in.
 */
public class AAVSOWebServiceObservationSource extends
		ObservationSourcePluginBase {

	protected StarInfo info;

	@Override
	public String getDescription() {
		return "AAVSO Web Service observation file reader.";
	}

	@Override
	public String getDisplayName() {
		return "New Star from AAVSO Web Service...";
	}

	@Override
	public String getInputName() {
		return info.getDesignation();
	}

	@Override
	public List<URL> getURLs() throws Exception {
		List<URL> urls = null;

		StarSelectorDialog dialog = StarSelectorDialog.getInstance();
		dialog.showDialog();

		if (!dialog.isCancelled()) {
			String urlStr = "http://test.aavso.org/apps/api/aid/observation";

			if (!dialog.wantAllData()) {
				urlStr += "?start=" + dialog.getMinDate().getJulianDay();
				urlStr += "&end=" + dialog.getMaxDate().getJulianDay();
			}

			if (urlStr.endsWith("observation")) {
				urlStr += "?";
			} else {
				urlStr += "&";
			}

			String auid = dialog.getAuid();
			String starName = dialog.getStarName();

			VSXWebServiceVOTableStarNameAndAUIDSource vsx = new VSXWebServiceVOTableStarNameAndAUIDSource();

			// TODO: just use auid or star name and tell Will to fix doc (not
			// just auid)

			if (auid == null) {
				info = vsx.getStarByName(null, starName);
			}

			if (starName == null) {
				info = vsx.getStarByAUID(null, auid);
			}

			urlStr += "star=" + info.getAuid();
			urlStr += "&format=xml";

			// TODO: add URL twice, once to get count, once to get data; indeed,
			// add it multiple times so we can break up the data loading into
			// multiple smaller chunks, so smaller DOMs without having to use
			// the by-page feature.
			urls = new ArrayList<URL>();
			urls.add(new URL(urlStr));
		}

		return urls;
	}

	@Override
	public InputType getInputType() {
		return InputType.URL;
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new AAVSOWebServiceObservationRetriever();
	}

	class AAVSOWebServiceObservationRetriever extends
			AbstractObservationRetriever {

		@Override
		public String getSourceName() {
			return getInputName();
		}

		@Override
		public String getSourceType() {
			return "AAVSO Web Service";
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(getInputStreams().get(0));

				document.getDocumentElement().normalize();

				NodeList objectNodes = document
						.getElementsByTagName("list-item");
				for (int i = 0; i < objectNodes.getLength(); i++) {

					Element obElt = (Element) objectNodes.item(i);
					NodeList obDataNodes = obElt.getChildNodes();

					ValidObservation ob = new ValidObservation();

					for (int j = 0; j < obDataNodes.getLength(); j++) {
						try {
							Element datum = (Element) obDataNodes.item(j);
							String name = datum.getNodeName();
							String value = datum.getTextContent();

							if (name.equals("jd")) {
								double jd = Double.parseDouble(value);
								DateInfo date = new DateInfo(jd);
								ob.setDateInfo(date);
							} else if (name.equals("magnitude")) {
								double magnitude = Double.parseDouble(value);
								double uncertainty = 0;
								Magnitude mag = new Magnitude(magnitude,
										uncertainty);
								ob.setMagnitude(mag);
							}
						} catch (NumberFormatException e) {
							// TODO: create invalid od
						}
					}

					// TODO: get band

					ob.setBand(SeriesType.Visual);

					collectObservation(ob);
				}
			} catch (MalformedURLException e) {
				throw new ObservationReadError(
						"Could not read data for object " + getSourceName());
			} catch (ParserConfigurationException e) {
				throw new ObservationReadError(
						"Error configring parser to read data for object "
								+ getSourceName());
			} catch (SAXException e) {
				throw new ObservationReadError(
						"Error while inspecting data for object "
								+ getSourceName());
			} catch (IOException e) {
				throw new ObservationReadError(
						"Error while reading data for object "
								+ getSourceName());
			}
		}
	}
}
