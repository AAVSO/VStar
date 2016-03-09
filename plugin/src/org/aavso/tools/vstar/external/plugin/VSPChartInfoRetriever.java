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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.EpochType;
import org.aavso.tools.vstar.util.coords.RAInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This plug-in retrieves chart information for the specified star from the
 * AAVSO VSP web service.
 */
public class VSPChartInfoRetriever extends GeneralToolPluginBase {

	@Override
	public String getDisplayName() {
		return "VSP Information";
	}

	@Override
	public String getDescription() {
		return "Retrieve VSP chart information";
	}

	@Override
	public void invoke() {
		// https://www.aavso.org/apps/vsp/api/chart/?star=SS+Cyg&fov=60&maglimit=14.5&format=xml

		String urlStr = "https://www.aavso.org/apps/vsp/api/chart/";
		try {
			String star = "R Car".replace(" ", "+");
			String fov = "480";
			String magLimit = "10";

			urlStr += "?star=" + star;
			urlStr += "&fov=" + fov;
			urlStr += "&maglimit=" + magLimit;
			// TODO: add other params: north=up|down, east=up|down etc
			urlStr += "&format=xml";

			retrieve(new URL(urlStr));
		} catch (MalformedURLException e) {
			MessageBox.showErrorDialog("VSP Retriever", "Invalid VSP API URL: "
					+ urlStr);
		}
	}

	// Helpers

	private void retrieve(URL url) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(url.openStream());

			document.getDocumentElement().normalize();

			String star = getEltAsStr(document, "star");
			String auid = getEltAsStr(document, "auid");
			Double magLimit = getEltAsDouble(document, "maglimit");
			Double fov = getEltAsDouble(document, "fov");
			String chartID = getEltAsStr(document, "chartid");
			String chartURL = getEltAsStr(document, "image_uri");
			RAInfo ra = decimalRA(getEltAsStr(document, "ra"));
			DecInfo dec = decimalDec(getEltAsStr(document, "dec"));

			NodeList refStarNodes = document.getElementsByTagName("list-item");
			for (int i = 0; i < refStarNodes.getLength(); i++) {

				Element refStarElt = (Element) refStarNodes.item(i);
				String refStarAUID = getEltAsStr(refStarElt, "auid");
				System.out.println(">> AUID: " + refStarAUID);
				String refStarComments = getEltAsStr(refStarElt, "comments");
				// TODO: get label, RA, Dec of ref star
				
				NodeList refBandsNodes = refStarElt
						.getElementsByTagName("bands");
				Element refBandsElt = (Element) refBandsNodes.item(0);

				NodeList refBandListNodes = refBandsElt
						.getElementsByTagName("list-item");

				// Handle all bands for the current reference star.
				for (int j = 0; j < refBandListNodes.getLength(); j++) {

					Element refBandListNode = (Element) refBandListNodes
							.item(j);
					NodeList refBandListChildNodes = refBandListNode
							.getChildNodes();

					// Handle a single band for the current reference star.
					// Instead of iterating over each list-item element's child
					// nodes, we could ask for each (band, mag, error) by
					// element tag name.
					Double mag = null;
					Double error = null;

					for (int k = 0; k < refBandListChildNodes.getLength(); k++) {
						try {
							Element bandInfo = (Element) refBandListChildNodes
									.item(k);
							String name = bandInfo.getNodeName();
							String value = bandInfo.getTextContent();

							if (name.equals("band")) {
								SeriesType series = SeriesType
										.getSeriesFromShortName(value);
								System.out.println(">> series: " + series);
							} else if (name.equals("mag")) {
								mag = Double.parseDouble(value);
							} else if (name.equals("error")) {
								error = Double.parseDouble(value);
							}
						} catch (NumberFormatException e) {
							e.printStackTrace();
						}
					}

					if (mag != null && error != null) {
						Magnitude magnitude = new Magnitude(mag, error);
					}

					// TODO: compute B-V from B and V
					// TODO: determine which bands to allow through via UI
					// checkboxes

					// TODO: create a mapping from reference star AUID to a list
					// of band info objects, e.g.
					// Map<String, List<BandInfo>> where BandInfo is a class or
					// pair containing band (series) and magnitude.
				}
			}
		} catch (MalformedURLException e) {
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}
	}

	private String getEltAsStr(Document document, String tagName) {
		NodeList nodes = document.getElementsByTagName(tagName);
		Element elt = (Element) nodes.item(0);
		return elt.getTextContent();
	}

	private String getEltAsStr(Element parentElt, String tagName) {
		NodeList nodes = parentElt.getElementsByTagName(tagName);
		Element elt = (Element) nodes.item(0);
		return elt.getTextContent();
	}

	private Double getEltAsDouble(Document document, String tagName)
			throws NumberFormatException {
		NodeList nodes = document.getElementsByTagName(tagName);
		Element elt = (Element) nodes.item(0);
		return Double.parseDouble(elt.getTextContent());
	}

//	private Double getEltAsDouble(Element parentElt, String tagName)
//			throws NumberFormatException {
//		NodeList nodes = parentElt.getElementsByTagName(tagName);
//		Element elt = (Element) nodes.item(0);
//		return Double.parseDouble(elt.getTextContent());
//	}

	private RAInfo decimalRA(String raStr) throws NumberFormatException {
		String[] raFields = raStr.split(":");
		int h = Integer.parseInt(raFields[0]);
		int m = Integer.parseInt(raFields[1]);
		double s = Double.parseDouble(raFields[2]);
		return new RAInfo(EpochType.B1950, h, m, s);
	}

	private DecInfo decimalDec(String decStr) throws NumberFormatException {
		String[] decFields = decStr.split(":");
		int d = Integer.parseInt(decFields[0]);
		int m = Integer.parseInt(decFields[1]);
		double s = Double.parseDouble(decFields[2]);
		return new DecInfo(EpochType.B1950, d, m, s);
	}

	// <photometry>
	// <list-item>
	// <auid>000-BCP-115</auid>
	// <bands>
	// <list-item>
	// <band>V</band>
	// <mag>5.11</mag>
	// <error>0.0</error>
	// </list-item>

}
