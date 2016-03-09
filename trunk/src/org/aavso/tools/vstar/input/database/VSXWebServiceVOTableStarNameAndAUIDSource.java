/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2013  AAVSO (http://www.aavso.org/)
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * This class obtains star name and AUID information from the VSX web service
 * via the get.votable method. A new instance of this class should be created
 * for each new star.
 * 
 * @deprecated
 */
public class VSXWebServiceVOTableStarNameAndAUIDSource implements
		IStarInfoSource {

	private String baseVsxUrlString;

	private Map<String, Integer> fieldId2DataIndexMap;
	private List<String> data;

	private enum Coord {
		RA(0), Dec(1);

		private int index;

		private Coord(int index) {
			this.index = index;
		}

		public int getIndex() {
			return index;
		}
	}

	public VSXWebServiceVOTableStarNameAndAUIDSource() {
		baseVsxUrlString = "https://www.aavso.org/vsx/index.php?view=query.votable";
		fieldId2DataIndexMap = new HashMap<String, Integer>();
		data = new ArrayList<String>();
	}

	/**
	 * @see org.aavso.tools.vstar.input.IStarInfoSource#getStarByAUID
	 *      (java.sql.Connection, java.lang.String)
	 */
	@Override
	public StarInfo getStarByAUID(Connection connection, String auid)
			throws SQLException {
		return retrieveData("ident=" + auid, auid);
	}

	/**
	 * @see org.aavso.tools.vstar.input.IStarInfoSource#getStarByName
	 *      (java.sql.Connection, java.lang.String)
	 */
	@Override
	public StarInfo getStarByName(Connection connection, String name)
			throws SQLException {

		return retrieveData("ident=" + name.replace(" ", "+"), name);
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
	 * @throws SQLException
	 *             If an exception occurred.
	 */
	public StarInfo retrieveData(String queryParam, String id)
			throws SQLException {

		StarInfo info = null;

		try {
			URL vsxUrl = new URL(baseVsxUrlString + "&" + queryParam
					+ "&format=d");

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(vsxUrl.openStream());

			document.getDocumentElement().normalize();

			NodeList fieldNodes = document.getElementsByTagName("FIELD");
			for (int i = 0; i < fieldNodes.getLength(); i++) {
				Element elt = (Element) fieldNodes.item(i);
				fieldId2DataIndexMap.put(elt.getAttribute("id"), i);
			}

			NodeList dataNodes = document.getElementsByTagName("TD");
			for (int i = 0; i < dataNodes.getLength(); i++) {
				Element elt = (Element) dataNodes.item(i);
				data.add(elt.getTextContent());
			}

			if (!data.isEmpty()) {
				String name = data.get(fieldId2DataIndexMap.get("name"));
				String auid = data.get(fieldId2DataIndexMap.get("auid"));
				Double period = getPossiblyNullDoubleValue("period");
				Double epoch = getPossiblyNullDoubleValue("epoch");
				String varType = data.get(fieldId2DataIndexMap.get("varType"));
				String spectralType = data.get(fieldId2DataIndexMap
						.get("specType"));
				String discoverer = data.get(fieldId2DataIndexMap.get("disc"));
				RAInfo ra = new RAInfo(EpochType.J2000, getCoord(Coord.RA));
				DecInfo dec = new DecInfo(EpochType.J2000, getCoord(Coord.Dec));

				info = new StarInfo(name, auid, period, epoch, varType,
						spectralType, discoverer, ra, dec, null);
			} else {
				throw new SQLException("Unable to obtain information for " + id);
			}
		} catch (MalformedURLException e) {
			// TODO: fix SQLException usage, above and below!
			throw new SQLException(e);
		} catch (ParserConfigurationException e) {
			throw new SQLException(e);
		} catch (SAXException e) {
			throw new SQLException(e);
		} catch (IOException e) {
			throw new SQLException(e);
		}

		return info;
	}

	private Double getPossiblyNullDoubleValue(String id) {
		Double value = null;

		if (fieldId2DataIndexMap.keySet().contains(id)) {
			try {
				int index = fieldId2DataIndexMap.get(id);
				value = Double.parseDouble(data.get(index));
			} catch (NumberFormatException e) {
				// Nothing to do.
			}
		}

		return value;
	}

	private Double getCoord(Coord coord) {
		Double value = null;

		if (fieldId2DataIndexMap.keySet().contains("radec2000")) {
			try {
				int index = fieldId2DataIndexMap.get("radec2000");
				String[] fields = data.get(index).split(",");
				if (fields.length == 2) {
					value = Double.parseDouble(fields[coord.getIndex()]);
				}
			} catch (NumberFormatException e) {
				// Nothing to do.
			}
		}

		return value;
	}
}
