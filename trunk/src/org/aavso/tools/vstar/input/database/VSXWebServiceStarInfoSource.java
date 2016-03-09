/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2016  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.input.database;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aavso.tools.vstar.input.IStarInfoSource;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.EpochType;
import org.aavso.tools.vstar.util.coords.RAInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class obtains star name and AUID information from the VSX web service. A
 * new instance of this class should be created for each new star.
 */
public class VSXWebServiceStarInfoSource implements IStarInfoSource {

	private String baseVsxUrlString;

	private Map<String, String> data;

	public VSXWebServiceStarInfoSource() {
		baseVsxUrlString = "https://www.aavso.org/vsx/index.php?view=api.object";
		data = new HashMap<String, String>();
	}

	/**
	 * @see org.aavso.tools.vstar.input.IStarInfoSource#getStarByAUID
	 *      (java.sql.Connection, java.lang.String)
	 */
	@Override
	public StarInfo getStarByAUID(Connection connection, String auid) {
		return retrieveData("ident=" + auid, auid);
	}

	/**
	 * @see org.aavso.tools.vstar.input.IStarInfoSource#getStarByName
	 *      (java.sql.Connection, java.lang.String)
	 */
	@Override
	public StarInfo getStarByName(Connection connection, String name) {
		// Replace "+" with %2B (thanks Patrick) and spaces with "+".
		return retrieveData(
				"ident=" + name.replace("+", "%2B").replace(" ", "+"), name);
	}

	/**
	 * Retrieve information for the specified object, in terms of a VSX web
	 * service query parameter.
	 * 
	 * @param queryParam
	 *            The query parameter, e.g. ident=R+Car
	 * @param id
	 *            An identifier for the object, e.g. R Car, that can be used in
	 *            error messages.
	 * @return The StarInfo instance.
	 */
	public StarInfo retrieveData(String queryParam, String id) {

		StarInfo info = null;

		try {
			// Get the XML document.
			URL vsxUrl = new URL(baseVsxUrlString + "&" + queryParam + "&data=1");

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(vsxUrl.openStream());

			document.getDocumentElement().normalize();

			// Collect all the child element text of the VSXObject element.
			NodeList vsxObjectNodes = document
					.getElementsByTagName("VSXObject");
			NodeList childNodes = vsxObjectNodes.item(0).getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Element elt = (Element) childNodes.item(i);
				data.put(elt.getNodeName(), elt.getTextContent());
			}

			// Create a StarInfo object.
			if (!data.isEmpty()) {
				String name = data.get("Name");
				String auid = data.get("AUID");
				Double period = getPossiblyNullDoubleValue("Period");
				Double epoch = getPossiblyNullDoubleValue("Epoch");
				String varType = data.get("VariabilityType");
				String spectralType = data.get("SpectralType");
				String discoverer = data.get("Discoverer");
				RAInfo ra = new RAInfo(EpochType.J2000, Double.parseDouble(data
						.get("RA2000")));
				DecInfo dec = new DecInfo(EpochType.J2000, Double.parseDouble(data
						.get("Declination2000")));

				// Is there an observation count?
				Integer obsCount = null;
				NodeList obsCountNodes = document
						.getElementsByTagName("Count");
				if (obsCountNodes.getLength() != 0) {
					Element obsCountElt = (Element) obsCountNodes.item(0);
					obsCount = Integer.parseInt(obsCountElt.getTextContent());
				}

				info = new StarInfo(name, auid, period, epoch, varType,
						spectralType, discoverer, ra, dec, obsCount);
			} else {
				throw new IllegalArgumentException(
						"Unable to obtain information for " + id);
			}
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		} catch (ParserConfigurationException e) {
			throw new IllegalArgumentException(e);
		} catch (SAXException e) {
			throw new IllegalArgumentException(e);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}

		return info;
	}

	// Helpers

	private Double getPossiblyNullDoubleValue(String id) {
		Double value = null;

		if (data.keySet().contains(id)) {
			try {
				value = Double.parseDouble(data.get(id));
			} catch (NumberFormatException e) {
				// Nothing to do.
			}
		}

		return value;
	}
}
