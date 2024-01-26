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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.SwingWorker;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AuthenticationError;
import org.aavso.tools.vstar.exception.CancellationException;
import org.aavso.tools.vstar.exception.ConnectionException;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.input.database.Authenticator;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.ui.dialog.AdditiveLoadFileOrUrlChooser;
import org.aavso.tools.vstar.ui.dialog.Checkbox;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.TextField;
import org.aavso.tools.vstar.ui.dialog.plugin.manager.PluginManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.ProgressInfo;
import org.aavso.tools.vstar.ui.mediator.message.ProgressType;
import org.aavso.tools.vstar.ui.resources.ResourceAccessor;
import org.aavso.tools.vstar.util.plugin.URLAuthenticator;

/**
 * A concurrent task in which a new star from observation source plug-in request
 * is handled.
 */
public class NewStarFromObSourcePluginTask extends SwingWorker<Void, Void> {

	private Mediator mediator = Mediator.getInstance();

	private ObservationSourcePluginBase obSourcePlugin;

	private List<InputStream> streams;

	private AbstractObservationRetriever retriever;

	private int obsCount;

	private boolean cancelled;

	/**
	 * Constructor.
	 * 
	 * @param obSourcePlugin
	 *            The plugin that will be used to obtain observations.
	 */
	public NewStarFromObSourcePluginTask(
			ObservationSourcePluginBase obSourcePlugin) {
		this.obSourcePlugin = obSourcePlugin;
		obsCount = 0;
		cancelled = false;
	}

	/**
	 * Configure the plug-in for observation retrieval.
	 */
	public void configure() {
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

			// Set input streams and name, if requested by the plug-in.
			streams = new ArrayList<InputStream>();

			switch (obSourcePlugin.getInputType()) {
			case FILE:
			case FILE_OR_URL:
				// Does the plug-in supply files? Or do we have to ask the user
				// for a file?
				List<File> files = obSourcePlugin.getFiles();
				if (files != null) {
					String fileNames = "";
					for (File file : files) {
						streams.add(new FileInputStream(file));
						fileNames += file.getName() + ", ";
					}
					fileNames = fileNames.substring(0,
							fileNames.lastIndexOf(", "));
					obSourcePlugin.setInputInfo(streams, fileNames);
				} else {
					// Request a file or URL from the user.
					AdditiveLoadFileOrUrlChooser fileChooser = PluginComponentFactory
							.chooseFileForReading(
									obSourcePlugin.getDisplayName(),
									obSourcePlugin
											.getAdditionalFileExtensions(),
									obSourcePlugin.getInputType() == InputType.FILE_OR_URL);

					if (fileChooser != null) {
						// Which plugin was selected in the end?
						// We only ask this for plugins that can share the
						// common file/URL dialog approach (with a list of
						// plugins). There will only be a non-empty optional
						// value if all observation source plugins
						// have been not been requested to be shown in the file
						// menu.
						if (obSourcePlugin.getInputType() == InputType.FILE_OR_URL) {
							Optional<ObservationSourcePluginBase> selectedPlugin = fileChooser
									.getSelectedPlugin();
							if (selectedPlugin.isPresent()) {
								obSourcePlugin = selectedPlugin.get();
							}
						}

						// If a file was chosen or a URL obtained, use as input.
						obSourcePlugin
								.setAdditive(fileChooser.isLoadAdditive());

						// Set multiple file selection state now that we know
						// what plugin we have.

						if (fileChooser.isUrlProvided()) {
							String urlStr = fileChooser.getUrlString();
							URL url = new URL(urlStr);
							streams.add(url.openConnection().getInputStream());
							obSourcePlugin.setInputInfo(streams, urlStr);
						} else {
							File[] selectedFiles = fileChooser.getSelectedFiles();
							if (selectedFiles.length != 0) {
							    for (File file : selectedFiles) {
    								streams.add(new FileInputStream(file));
    								obSourcePlugin.setInputInfo(streams,
    										file.getName());
							    }
							} else {
								throw new CancellationException();
							}
						}

						obSourcePlugin.setVelaFilterStr(fileChooser
								.getVeLaFilter());
					} else {
						throw new CancellationException();
					}
				}
				break;

			case URL:
				// If the plug-in specifies a basic auth http username and
				// password, create and set an authenticator.
				String userName = obSourcePlugin.getUsername();
				String password = obSourcePlugin.getPassword();

				if (userName != null && password != null) {
					java.net.Authenticator.setDefault(new URLAuthenticator(
							userName, password));
				}

				// Does the plug-in supply URLs? Or do we have to ask the user
				// for a URL?
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
					// Request a URL from the user.
					TextField urlField = new TextField("URL");
					TextField velaFilterField = new TextField("VeLa Filter");
					Checkbox additiveLoadCheckbox = new Checkbox(
							"Add to current?", false);
					MultiEntryComponentDialog urlDialog = new MultiEntryComponentDialog(
							"Enter URL", urlField, additiveLoadCheckbox);
					if (!urlDialog.isCancelled()
							&& !urlField.getValue().matches("^\\s*$")) {
						String urlStr = urlField.getValue();
						obSourcePlugin.setVelaFilterStr(velaFilterField
								.getValue());
						obSourcePlugin.setAdditive(additiveLoadCheckbox
								.getValue());
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
				String str = obSourcePlugin.getVelaFilterStr();
				break;
			}

			// Retrieve the observations. If the retriever can return
			// the number of records, we can show updated progress,
			// otherwise just show busy state.
			retriever = obSourcePlugin.getObservationRetriever();
			
			// #PMAK#20211229#1#:
			//	if the retriever has configuration dialog (see, for example, ASAS-SN plug-in)
			//  that was canceled, getObservationRetriever() returns null.
			//  It is essentially the same as CancellationException
			//  that cannot be thrown from within getObservationRetriever()
			if (retriever == null)
				cancelled = true;
		} catch (CancellationException ex) {
			cancelled = true;
		} catch (ConnectionException ex) {
			MessageBox.showErrorDialog("Authentication Source Error",
					ex.getLocalizedMessage());
		} catch (AuthenticationError ex) {
			MessageBox.showErrorDialog("Authentication Error",
					ex.getLocalizedMessage());
		} catch (Exception ex) {
			MessageBox.showErrorDialog("Observation Source Error",
					ex.getLocalizedMessage());
		} finally {
			// #PMAK#20201121#1#: close input streams if no retriever returned.
			if (retriever == null)
				try {
					closeStreams();
				} catch (IOException ex) {
					// we unlikely be here
					MessageBox.showErrorDialog("Observation Source Error",
							ex.getLocalizedMessage());
				}
			Mediator.getUI().setCursor(null);
		}
	}

	public boolean isConfigured() {
		return retriever != null;
	}

	/**
	 * Main task. Executed in background thread.
	 */
	public Void doInBackground() {
		try {

			createObservationArtefacts();

		} catch (Exception ex) {
			// Experience shows that if we get to this point, a different
			// exception has already been caught and reported in the event
			// thread. There is no point in reporting a null exception!
			if (ex.getLocalizedMessage() != null) {
				MessageBox.showErrorDialog("Observation Source Error",
						ex.getLocalizedMessage());
			}
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
			int plotPortion = 0;
			Integer numRecords = retriever.getNumberOfRecords();
			if (numRecords == null) {
				// Show busy.
				mediator.getProgressNotifier().notifyListeners(
						ProgressInfo.BUSY_PROGRESS);
			} else {
				// Start progress tracking.
				plotPortion = (int) (numRecords * 0.2);

				mediator.getProgressNotifier().notifyListeners(
						new ProgressInfo(ProgressType.MAX_PROGRESS, numRecords
								+ plotPortion));

				mediator.getProgressNotifier().notifyListeners(
						ProgressInfo.START_PROGRESS);
			}

			// Note: A reset may cause problems if isAdditive() is true, so make
			// conditional.
			if (!obSourcePlugin.isAdditive()) {
				ValidObservation.reset();
			}

			try {
				retriever.retrieveObservations();

				if (retriever.getValidObservations().isEmpty()) {
					String msg = "No observations found.";
					MessageBox.showErrorDialog("Observation Read Error", msg);
				} else {
					// Create plots, tables.
					mediator.createNewStarObservationArtefacts(
							obSourcePlugin.getNewStarType(),
							retriever.getStarInfo(), plotPortion,
							obSourcePlugin.isAdditive());

					obsCount = retriever.getValidObservations().size();
				}
			} finally {
				closeStreams();
			}
		} catch (InterruptedException e) {
			ValidObservation.restore();
			done();
		} catch (Throwable t) {
			ValidObservation.restore();
			done();
			MessageBox.showErrorDialog(
					"New Star From Observation Source Read Error",
					t.getLocalizedMessage());
		}
	}

	/**
	 * Executed in event dispatching thread.
	 */
	public void done() {
		if (cancelled
				|| obsCount != 0
				|| (obSourcePlugin.isAdditive() && !mediator
						.getNewStarMessageList().isEmpty())) {
			// Either there were observations loaded or this was a failed
			// additive load and there exist previously loaded observations, so
			// we want to complete progress. We also want to complete progress
			// if the dialog is cancelled. Doing so ensures menu and tool bar
			// icons have their state restored. Question: why not just do this
			// unconditionally then?
			mediator.getProgressNotifier().notifyListeners(
					ProgressInfo.COMPLETE_PROGRESS);
		}

		mediator.getProgressNotifier().notifyListeners(
				ProgressInfo.CLEAR_PROGRESS);
	}
	
	private void closeStreams() throws IOException
	{
		// Close all streams
		for (InputStream stream : streams) {
			stream.close();
		}
	}
}