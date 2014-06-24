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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Pattern;

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
	private final static Pattern authResponsePattern = Pattern
			.compile("^\\s*\\{\"token\": \"(NO TOKEN AVAILABLE)\", \"id\": (.+)\\}\\s*$");

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

			// See
			// http://en.wikipedia.org/wiki/POST_(HTTP)#Use_for_submitting_web_forms
			// Note: despite this XML type, JSON is returned!
			// String type = "application/x-www-form-urlencoded";

			// conn.setRequestProperty("Content-Type", type);
//			conn.setRequestProperty("Content-Type",
//					"application/x-www-form-urlencoded; charset=utf-8");
			
			// TODO: use this to get XML
			conn.setRequestProperty("Accept", "application/xml");

			String data = String.format("%s=%s&%s=%s", "username", username,
					"password", password);

			conn.setRequestProperty("Content-Length", String.valueOf(data
					.length()));

			// Send the POST request.
			OutputStream os = conn.getOutputStream();
			String encodedData = URLEncoder.encode(data, "utf-8");
			os.write(data.getBytes());
			os.flush();
			os.close();

			if (conn.getResponseCode() == 200) {
				authenticated = true;
			} else {
				String message = "Authentication failed";
				throw new AuthenticationError(message);
			}

			// String response = getResponse(conn);
			processResponse(conn);

			// Matcher authResponseMatcher =
			// authResponsePattern.matcher(response);
			// if (authResponseMatcher.matches()) {
			// String token = authResponseMatcher.group(1);
			// String id = authResponseMatcher.group(2);
			// // TODO: get obscode if exists and add to login info object
			// ResourceAccessor.getLoginInfo().setMember(!"null".equals(id));
			// }
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

	// Helpers

	// Get the response string from the POST method.
	private String getResponse(HttpURLConnection conn) throws IOException {
		InputStream is = conn.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		StringBuffer buf = new StringBuffer();
		while ((line = rd.readLine()) != null) {
			buf.append(line);
			buf.append('\r');
		}
		rd.close();
		return URLDecoder.decode(buf.toString(), "utf-8");
	}

	// Process the POST response, extracting ID, auth token, observer code.
	private void processResponse(HttpURLConnection conn) throws IOException,
			ParserConfigurationException, SAXException {

		//String response = getResponse(conn);
		//System.out.println(response);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
//		InputSource source = new InputSource(conn.getInputStream());
//		source.setEncoding("utf-8");
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

//		NodeList obscodeNodes = document.getElementsByTagName("obscode");
//		if (obscodeNodes.getLength() == 1) {
//			String obscode = obscodeNodes.item(0).getFirstChild()
//					.getTextContent();
//			ResourceAccessor.getLoginInfo().setObserverCode(obscode);
//		}
	}
}
