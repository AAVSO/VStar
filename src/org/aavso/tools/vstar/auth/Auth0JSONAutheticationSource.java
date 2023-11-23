/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2009  AAVSO (http://www.aavso.org/)
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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.net.ssl.HttpsURLConnection;

import org.aavso.tools.vstar.exception.AuthenticationError;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.input.database.IAuthenticationSource;
import org.aavso.tools.vstar.ui.resources.LoginInfo;
import org.aavso.tools.vstar.ui.resources.LoginType;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;

/**
 * This class authenticates a VStar user via an http POST method that makes use
 * of an Auth0 web service to obtain user information.
 */
public class Auth0JSONAutheticationSource implements IAuthenticationSource {

	private static final String AUTH_URL = "https://apps.aavso.org/auth/external";

	private boolean authenticated = false;

	@Override
	public boolean authenticate(String username, String password) throws AuthenticationError, ConnectionException {
		try {
			// Open a connection to the end-point.
			URL url = new URL(AUTH_URL);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");

			// Construct the JSON POST body.
			String uuid = username;
			String code = password;

			StringBuffer jsonBuf = new StringBuffer();
			
			jsonBuf.append("{\n");
			jsonBuf.append(String.format("\"code\": %s", code));
			jsonBuf.append(",\n");
			jsonBuf.append(String.format("\"identifier\": \"%s\"", uuid));
			jsonBuf.append("\n}");
			
			String json = jsonBuf.toString();

			// Send the POST request.
			OutputStream os = conn.getOutputStream();
			os.write(json.getBytes("UTF-8"));
			os.close();
			
			if (conn.getResponseCode() == 200) {
				authenticated = true;

				// Get the JSON result string.
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuffer resultBuf = new StringBuffer();
				String out;
				while ((out = reader.readLine()) != null) {
					resultBuf.append(out);
				}
				String responseJSON = resultBuf.toString();
				
				java.util.UUID u;
				
				reader.close();
				conn.disconnect();

				// Populate login info from a map of JSON key-string value pairs.
				Map<String, String> results = parseJSONString(responseJSON);
				LoginInfo info = ResourceAccessor.getLoginInfo();
				info.setMember(Boolean.parseBoolean(results.get("is_member")));
				info.setObserverCode(results.get("obscode"));
				info.setUserName(results.get("email"));
				info.setToken(results.get("token"));
				info.setType(getLoginType());

			} else {
				String message = "Authentication failed";
				throw new AuthenticationError(message);
			}
		} catch (MalformedURLException e) {
			throw new ConnectionException(e.getLocalizedMessage());
		} catch (IOException e) {
			throw new ConnectionException(e.getLocalizedMessage());
		}

		return authenticated;
	}

	/**
	 * Given a JSON string, return a map of keys to value strings.
	 * 
	 * @param json A JSON string.
	 * @return A mapping from key to string value.
	 */
	public static Map<String, String> parseJSONString(String json) {
		Map<String, String> key2ValueMap = new HashMap<String, String>();

		json = json.trim().replace("{", "").replace("}", "");

		StringTokenizer pairLexer = new StringTokenizer(json, ",");
		while (pairLexer.hasMoreElements()) {
			String pairStr = (String) pairLexer.nextElement();
			String[] pair = pairStr.split(":\\s+");
			key2ValueMap.put(
					trimAndRemoveQuotes(pair[0]),
					trimAndRemoveQuotes(pair[1]));
		}

		return key2ValueMap;
	}

	private static String trimAndRemoveQuotes(String str) {
		return str.trim().replace("\"", "");
	}
	
	@Override
	public LoginType getLoginType() {
		return LoginType.AAVSO;
	}
}
