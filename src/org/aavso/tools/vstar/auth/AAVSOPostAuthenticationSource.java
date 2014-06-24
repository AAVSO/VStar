/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2014  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.auth;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aavso.tools.vstar.exception.AuthenticationError;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.input.database.IAuthenticationSource;
import org.aavso.tools.vstar.ui.resources.LoginType;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class authenticates an AAVSO user via an http POST method.
 */
public class AAVSOPostAuthenticationSource implements IAuthenticationSource {

	private String endPoint;
	private boolean authenticated;

	public AAVSOPostAuthenticationSource(String endPoint) {
		this.endPoint = endPoint;
		authenticated = false;
	}

	@Override
	public boolean authenticate(String username, String password)
			throws AuthenticationError, ConnectionException {

		try {
			// Create a POST request for the authentication end point.
			URL url = new URL(endPoint);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");

			// Note: despite this XML content type, JSON is returned without the
			// Accept property below. See:
			// http://en.wikipedia.org/wiki/POST_(HTTP)#Use_for_submitting_web_forms
			String type = "application/x-www-form-urlencoded";
			conn.setRequestProperty("Content-Type", type);

			// This is required to get an XML response! Indeed, "Content-Type"
			// can be omitted with no ill-effect.
			conn.setRequestProperty("Accept", "application/xml");

			// Create the POST message data.
			String data = String.format("%s=%s&%s=%s", "username", username,
					"password", password);

			conn.setRequestProperty("Content-Length", String.valueOf(data
					.length()));

			// Send the POST request.
			OutputStream os = conn.getOutputStream();
			os.write(data.getBytes());
			os.flush();
			os.close();

			if (conn.getResponseCode() == 200) {
				authenticated = true;
				ResourceAccessor.getLoginInfo().setUserName(username);
			} else {
				String message = "Authentication failed";
				throw new AuthenticationError(message);
			}

			processResponse(conn);

		} catch (MalformedURLException e) {
			throw new ConnectionException(e.getLocalizedMessage());
		} catch (IOException e) {
			throw new ConnectionException(e.getLocalizedMessage());
		} catch (ParserConfigurationException e) {
			throw new ConnectionException(e.getLocalizedMessage());
		} catch (SAXException e) {
			throw new ConnectionException(e.getLocalizedMessage());
		}

		return authenticated;
	}

	@Override
	public LoginType getLoginType() {
		return LoginType.AAVSO;
	}

	// Process the POST response, extracting ID, authentication token.
	private void processResponse(HttpURLConnection conn) throws IOException,
			ParserConfigurationException, SAXException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(conn.getInputStream());

		document.getDocumentElement().normalize();

		NodeList idNodes = document.getElementsByTagName("id");
		if (idNodes.getLength() == 1) {
			String id = idNodes.item(0).getTextContent();
			ResourceAccessor.getLoginInfo().setMember(!"".equals(id));
		}

		NodeList tokenNodes = document.getElementsByTagName("token");
		if (tokenNodes.getLength() == 1) {
			String token = tokenNodes.item(0).getTextContent();
			ResourceAccessor.getLoginInfo().setToken(token);
		}
	}
}
