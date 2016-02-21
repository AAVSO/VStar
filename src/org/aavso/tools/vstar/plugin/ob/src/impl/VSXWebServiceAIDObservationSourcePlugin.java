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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidationType;
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
 */
public class VSXWebServiceAIDObservationSourcePlugin extends
		ObservationSourcePluginBase {

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
		return InputType.NONE;
	}

	@Override
	public NewStarType getNewStarType() {
		return NewStarType.NEW_STAR_FROM_DATABASE;
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

		private String baseVsxUrlString;

		private StarSelectorDialog starSelector;

		public VSXAIDObservationRetriever() {
			baseVsxUrlString = "https://www.aavso.org/vsx/index.php?view=api.object";
			starSelector = StarSelectorDialog.getInstance();
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {
			// Ask for object information.
			starSelector.showDialog();

			if (!starSelector.isCancelled()) {
				String auid = starSelector.getAuid();
				String starName = starSelector.getStarName();
				// setInputInfo(null, starSelector.getStarName());

				try {
					// Get the star name if we don't have it.
					VSXWebServiceXMLStarInfoSource infoSrc = new VSXWebServiceXMLStarInfoSource();

					if (starName == null) {
						StarInfo info = infoSrc.getStarByAUID(null, auid);
						starName = info.getDesignation();
					}

					// Get the XML document.
					int numObs = 100; // TODO: need a web service call to get
										// this and divide into a number of obs
										// get calls

					// TODO: limit the max number you can get back to N (e.g.
					// 1000) and use a continuation token of some kind to ask
					// "are there more observations remaining?"

					String name = starName.replace("+", "%2B")
							.replace(" ", "+");
					
					URL vsxUrl = new URL(baseVsxUrlString + "&ident=" + name
							+ "&data=" + numObs);

					DocumentBuilderFactory factory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document document = builder.parse(vsxUrl.openStream());

					document.getDocumentElement().normalize();

					// Collect all the child element text of the VSXObject
					// element.
					NodeList obsNodes = document
							.getElementsByTagName("Observation");
					for (int i = 0; i < obsNodes.getLength(); i++) {
						NodeList obsDetails = obsNodes.item(i).getChildNodes();

						double jd = Double.POSITIVE_INFINITY;
						double mag = Double.POSITIVE_INFINITY;
						SeriesType band = null;
						String obscode = null;
						ValidationType valType = null;

						for (int j = 0; j < obsDetails.getLength(); j++) {
							Element detailElt = (Element) obsDetails.item(j);
							String nodeName = detailElt.getNodeName();
							String nodeValue = detailElt.getTextContent();

							if ("JD".equalsIgnoreCase(nodeName)) {
								jd = Double.parseDouble(nodeValue);
							} else if ("magnitude".equalsIgnoreCase(nodeName)) {
								mag = Double.parseDouble(nodeValue);
							} else if ("obscode".equalsIgnoreCase(nodeName)) {
								obscode = nodeValue;
							} else if ("valflag".equalsIgnoreCase(nodeName)) {
								valType = getValidationType(nodeValue);
							} else if ("band".equalsIgnoreCase(nodeName)) {
								int bandIndex = Integer.parseInt(nodeValue);
								band = SeriesType.getSeriesFromIndex(bandIndex);
							}
						}

						if (jd != Double.POSITIVE_INFINITY
								&& mag != Double.POSITIVE_INFINITY) {
							ValidObservation ob = new ValidObservation();
							ob.setDateInfo(new DateInfo(jd));
							ob.setMagnitude(new Magnitude(mag, 0));
							ob.setBand(band);
							ob.setObsCode(obscode);
							ob.setValidationType(valType);
							ob.setName(starName);
							
							collectObservation(ob);
						}
					}
				} catch (MalformedURLException e) {
					throw new ObservationReadError(
							"Unable to obtain information for " + auid);
				} catch (ParserConfigurationException e) {
					throw new ObservationReadError(
							"Unable to obtain information for " + auid);
				} catch (SAXException e) {
					throw new ObservationReadError(
							"Unable to obtain information for " + auid);
				} catch (IOException e) {
					throw new ObservationReadError(
							"Unable to obtain information for " + auid);
				}
			}
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
	}
}