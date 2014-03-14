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
package org.aavso.tools.vstar.ui.task;

import java.awt.Cursor;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AuthenticationError;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.input.database.Authenticator;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.ui.dialog.AdditiveLoadFileSelectionChooser;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.NewStarType;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;
import org.aavso.tools.vstar.util.plugin.URLAuthenticator;

/**
 * A concurrent task in which a new star from observation source plug-in request
 * is handled.
 */
public class NewStarFromObSourcePluginTask extends SwingWorker<Void, Void> {

	private Mediator mediator = Mediator.getInstance();

	private ObservationSourcePluginBase obSourcePlugin;

	/**
	 * Constructor.
	 * 
	 * @param obSourcePlugin
	 *            The plugin that will be used to obtain observations.
	 */
	public NewStarFromObSourcePluginTask(
			ObservationSourcePluginBase obSourcePlugin) {
		this.obSourcePlugin = obSourcePlugin;
	}

	/**
	 * Main task. Executed in background thread.
	 */
	public Void doInBackground() {
		try {
			if (obSourcePlugin.requiresAuthentication()) {
				Mediator.getUI().setCursor(
						Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

				Authenticator.getInstance().authenticate();
				if (!obSourcePlugin
						.additionalAuthenticationSatisfied(ResourceAccessor
								.getLoginInfo())) {
					throw new AuthenticationError(
							"Plug-in authentication failed");
				}
			}

			createObservationArtefacts();

		} catch (CancellationException ex) {
			// Nothing to do; dialog cancelled.
		} catch (ConnectionException ex) {
			MessageBox.showErrorDialog("Authentication Source Error", ex);
		} catch (AuthenticationError ex) {
			MessageBox.showErrorDialog("Authentication Error", ex);
		} catch (Exception ex) {
			MessageBox.showErrorDialog("Discrepant Reporting Error", ex);
		} finally {
			Mediator.getUI().setCursor(null);
		}

		return null;
	}

	/**
	 * Create observation table and plot models from an observation source
	 * plug-in.
	 */
	protected void createObservationArtefacts() {

		try {
			// Set input streams and name, if requested by the plug-in.
			List<InputStream> streams = new ArrayList<InputStream>();

			// TODO: ask plugin once whether load is additive; may be overridden
			// by file, URL, or other dialog

			boolean isAdditive = false;

			switch (obSourcePlugin.getInputType()) {
			case FILE:
				List<File> files = obSourcePlugin.getFiles();
				if (files != null) {
					String fileNames = "";
					for (File file : files) {
						streams.add(new FileInputStream(file));
						fileNames += file.getName() + ", ";
					}
					fileNames = fileNames.substring(0, fileNames
							.lastIndexOf(", "));
					obSourcePlugin.setInputInfo(streams, fileNames);
				} else {
					AdditiveLoadFileSelectionChooser fileChooser = PluginComponentFactory
							.chooseFileForReading(obSourcePlugin
									.getDisplayName());
					if (fileChooser != null) {
						File file = fileChooser.getSelectedFile();
						isAdditive = fileChooser.isLoadAdditive();
						streams.add(new FileInputStream(file));
						obSourcePlugin.setInputInfo(streams, file.getName());
					} else {
						throw new CancellationException();
					}
				}
				break;

			case URL:
				// If the plugin specifies a username and password, create and
				// set an authenticator.
				String userName = obSourcePlugin.getUsername();
				String password = obSourcePlugin.getPassword();

				if (userName != null && password != null) {
					java.net.Authenticator.setDefault(new URLAuthenticator(
							userName, password));
				}

				// Obtain the plugin's URLs and create input streams.
				List<URL> urls = obSourcePlugin.getURLs();
				if (urls != null) {
					String urlStrs = "";
					for (URL url : urls) {
						streams.add(url.openStream());
						urlStrs += url.getPath() + ", ";
					}
					urlStrs = urlStrs.substring(0, urlStrs.lastIndexOf(", "));
					obSourcePlugin.setInputInfo(streams, urlStrs);
				} else {
					// TODO: use a custom dialog based upon TextDialog class
					// that includes a isLoadAdditive() method
					String urlStr = JOptionPane
							.showInputDialog("Enter Observation Source URL");
					if (urlStr != null && urlStr.trim().length() != 0) {
						URL url = new URL(urlStr);
						streams.add(url.openStream());
						obSourcePlugin.setInputInfo(streams, urlStr);
					} else {
						throw new CancellationException();
					}
				}

				break;

			case NONE:
				obSourcePlugin.setInputInfo(null, null);
				break;
			}

			// Retrieve the observations.
			AbstractObservationRetriever retriever = obSourcePlugin
					.getObservationRetriever();

			ValidObservation.reset();

			retriever.retrieveObservations();

			if (retriever.getValidObservations().isEmpty()) {
				throw new ObservationReadError(
						"No observations for the specified period or error in observation source.");
			}

			// Create plots, tables.
			NewStarType type = NewStarType.NEW_STAR_FROM_ARBITRARY_SOURCE;
			mediator.createNewStarObservationArtefacts(type, retriever
					.getStarInfo(), 0, isAdditive);

		} catch (InterruptedException e) {
			ValidObservation.restore();

			mediator.getProgressNotifier().notifyListeners(
					ProgressInfo.COMPLETE_PROGRESS);

			mediator.getProgressNotifier().notifyListeners(
					ProgressInfo.CLEAR_PROGRESS);
		} catch (CancellationException e) {
			ValidObservation.restore();

			mediator.getProgressNotifier().notifyListeners(
					ProgressInfo.COMPLETE_PROGRESS);

			mediator.getProgressNotifier().notifyListeners(
					ProgressInfo.CLEAR_PROGRESS);
		} catch (Throwable t) {
			ValidObservation.restore();

			MessageBox.showErrorDialog(
					"New Star From Observation Source Read Error", t);

			mediator.getProgressNotifier().notifyListeners(
					ProgressInfo.COMPLETE_PROGRESS);

			mediator.getProgressNotifier().notifyListeners(
					ProgressInfo.CLEAR_PROGRESS);
		}
	}

	/**
	 * Executed in event dispatching thread.
	 */
	public void done() {
		mediator.getProgressNotifier().notifyListeners(
				ProgressInfo.COMPLETE_PROGRESS);

		mediator.getProgressNotifier().notifyListeners(
				ProgressInfo.CLEAR_PROGRESS);
	}
}