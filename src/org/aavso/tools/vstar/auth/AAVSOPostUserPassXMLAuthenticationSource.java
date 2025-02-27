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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;
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
 * This class authenticates an AAVSO user via an http POST method that makes use
 * of a VSX web service to obtain user information.
 * 
 * @deprecated see Auth0JSONAutheticationSource
 */
public class AAVSOPostUserPassXMLAuthenticationSource implements IAuthenticationSource {

	private static final String AUTH_URL = "https://www.aavso.org/apps/api-auth/";

	private String endPoint;
	private boolean authenticated;
	private String userID;

	public AAVSOPostUserPassXMLAuthenticationSource(String endPoint) {
		this.endPoint = endPoint;
		authenticated = false;
	}

	public AAVSOPostUserPassXMLAuthenticationSource() {
		this(AUTH_URL);
	}

	/**
	 * Return the authentiated user ID.
	 * 
	 * @return the user ID
	 */
	public String getUserID() {
		return userID;
	}

	@Override
	public boolean authenticate(String username, String password) throws AuthenticationError, ConnectionException {

		try {
			// Create a POST request for the authentication end point.
			URL url = new URL(endPoint);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setDoInput(true); // TODO: why twice!?
			// conn.setRequestProperty("Authorization", "Basic " + encode);
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
			String usernameEncoded = URLEncoder.encode(username, "UTF-8");
			String passwordEncoded = URLEncoder.encode(password, "UTF-8");
			String data = String.format("%s=%s&%s=%s", "username", usernameEncoded, "password", passwordEncoded);

			conn.setRequestProperty("Content-Length", String.valueOf(data.length()));

			// Send the POST request.
			OutputStream os = conn.getOutputStream();
			os.write(data.getBytes());
			os.flush();
			os.close();

			if (conn.getResponseCode() == 200) {
				authenticated = true;
				ResourceAccessor.getLoginInfo().setUserName(username);
				ResourceAccessor.getLoginInfo().setType(getLoginType());
			} else {
				String message = "Authentication failed";
				throw new AuthenticationError(message);
			}

			processResponse(conn);

			VSXWebServiceMemberInfo memberInfo = new VSXWebServiceMemberInfo();
			memberInfo.retrieveUserInfo(userID, ResourceAccessor.getLoginInfo());

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
	private void processResponse(HttpsURLConnection conn)
			throws IOException, ParserConfigurationException, SAXException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(conn.getInputStream());

		document.getDocumentElement().normalize();

		NodeList idNodes = document.getElementsByTagName("id");
		if (idNodes.getLength() == 1) {
			userID = idNodes.item(0).getTextContent();
		}

		NodeList tokenNodes = document.getElementsByTagName("token");
		if (tokenNodes.getLength() == 1) {
			String token = tokenNodes.item(0).getTextContent();
			ResourceAccessor.getLoginInfo().setToken(token);
		}
	}
}
