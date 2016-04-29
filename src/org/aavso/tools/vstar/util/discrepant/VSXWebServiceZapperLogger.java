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
package org.aavso.tools.vstar.util.discrepant;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.aavso.tools.vstar.ui.dialog.MessageBox;

/**
 * A discrepant reporter that writes to the Zapper log using the VSX web
 * service.
 */
public class VSXWebServiceZapperLogger implements IDiscrepantReporter {

	private String endPoint;

	/**
	 * Constructor
	 */
	public VSXWebServiceZapperLogger() {
		this.endPoint = "https://www.aavso.org/vsx/index.php?view=api.zapperlog";
	}

	@Override
	public void lodge(DiscrepantReport report) {

		try {
			// Create a POST request for the authentication end point.
			URL url = new URL(endPoint);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestMethod("POST");

			// Create the POST message data.
			String auidEncoded = URLEncoder.encode(report.getAuid(), "UTF-8");
			String nameEncoded = URLEncoder.encode(report.getName(), "UTF-8");
			String editorEncoded = URLEncoder.encode(report.getEditor(),
					"UTF-8");
			String commentsEncoded = URLEncoder.encode(report.getComments(),
					"UTF-8");

			String data = String.format("%s=%s&%s=%s&%s=%s&%s=%s&%s=%s&%s=%s",
					"auid", auidEncoded, "name", nameEncoded, "unique_id",
					report.getUniqueId(), "editor", editorEncoded, "editdate",
					report.getJd(), "editorcomments", commentsEncoded);

			conn.setRequestProperty("Content-Length",
					String.valueOf(data.length()));

			// Send the POST request.
			OutputStream os = conn.getOutputStream();
			os.write(data.getBytes());
			os.flush();
			os.close();

			if (conn.getResponseCode() == 200) {
				MessageBox.showMessageDialog("Discrepant Report",
						"Lodged Discrepant Report with AAVSO");
			} else {
				MessageBox.showErrorDialog("Discrepant Reporting Error",
						"Unable to lodge report");
			}
		} catch (MalformedURLException e) {
			MessageBox.showErrorDialog("Discrepant Reporting Error", e);
		} catch (IOException e) {
			MessageBox.showErrorDialog("Discrepant Reporting Error", e);
		}
	}
}
