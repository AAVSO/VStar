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
package org.aavso.tools.vstar.ui.model;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.ObservationRetrieverBase;
import org.aavso.tools.vstar.input.SimpleTextFormatReader;
import org.aavso.tools.vstar.util.Notifier;

/**
 * This class manages models, in particular, the data models that under-pin
 * the table and chart views.
 * 
 * This is a Singleton since only one manager per application instance
 * needs to exist.
 * 
 * TODO: if we store GUI components here also, it should be DocManager 
 * again...or ArtefactManager (like that one!) or...
 * 
 */
public class ModelManager {

	// Current models.
	
	private List<ValidObservation> validObsList;
	private List<InvalidObservation> invalidObsList;

	private ValidObservationTableModel validObsTableModel;
	private InvalidObservationTableModel invalidObsTableModel;

	private ObservationPlotModel obsPlotModel;

	// TODO: mean, phase plot models...

	private ModeType currentMode;
	
	private String newStarFileName;
	
	// New star creation notifier.
	private Notifier<NewStarType> newStarNotifier;

	// Mode change notifier.
	private Notifier<ModeType> modeChangeNotifier;
	
	/**
	 * @return the newStarNotifier
	 */
	public Notifier<NewStarType> getNewStarNotifier() {
		return newStarNotifier;
	}

	/**
	 * @return the modeChangeNotifier
	 */
	public Notifier<ModeType> getModeChangeNotifier() {
		return modeChangeNotifier;
	}

	/**
	 * Change the mode of VStar's focus (i.e what is 
	 * to be presented to the user).
	 * 
	 * @param mode The mode to change to.
	 */
	public void changeMode(ModeType mode) {
		this.currentMode = mode;
		this.getModeChangeNotifier().notifyListeners(mode);
		// TODO: Combine command with Analysis menu selection to
		// set the table/plot to view from ModelManager. For now
		// just assume Analysis->Raw Data.
	}
	
	/**
	 * Create observation table and plot models from a file.
	 * 
	 * @param obsFile
	 *            The file from which to create the observation models.
	 */
	public void createObservationModelsFromFile(File obsFile)
			throws IOException, ObservationReadError {

		// TODO: factory -> DocType, retriever instance + delimiter
		
		// TODO: Use an abstract factory to determine observation
		// retriever class to use given the file type, along
		// with all other classes of relevance to us! The
		// concrete factory could be stored in the model manager. 
		// Factory could give us NewStarType, ObservationRetrieverBase,
		// (In)ValidObservation (sub)classes etc. Actually, NewStarType
		// could be used to give us the concrete factory. For files
		// we need first determine NewStarType via file content.

		String delimiter = "\t";
		
		ObservationRetrieverBase simpleTextFormatReader = new SimpleTextFormatReader(
				new LineNumberReader(new FileReader(obsFile.getPath())), delimiter);

		simpleTextFormatReader.retrieveObservations();

		List<ValidObservation> validObs = simpleTextFormatReader
				.getValidObservations();

		List<InvalidObservation> invalidObs = simpleTextFormatReader
				.getInvalidObservations();

		if (!validObs.isEmpty()) {
			this.validObsList = validObs;

			this.validObsTableModel =
					new ValidObservationTableModel(validObs);

			this.obsPlotModel =
					new ObservationPlotModel(obsFile.getName(), validObs);
			
			// TODO: same for means ...
		}

		if (!invalidObs.isEmpty()) {
			this.invalidObsList = invalidObs;

			this.invalidObsTableModel = 
					new InvalidObservationTableModel(invalidObs);
		}
		
		this.newStarFileName = obsFile.getPath();

		// TODO: NewStarType enum value should depend upon factory or 
		//       some other setting
		this.newStarNotifier.notifyListeners(NewStarType.NEW_STAR_FROM_SIMPLE_FILE);
	}

	/**
	 * @return the validObsList
	 */
	public List<ValidObservation> getValidObsList() {
		return validObsList;
	}

	/**
	 * @return the invalidObsList
	 */
	public List<InvalidObservation> getInvalidObsList() {
		return invalidObsList;
	}

	/**
	 * @return the validObsTableModel
	 */
	public ValidObservationTableModel getValidObsTableModel() {
		return validObsTableModel;
	}

	/**
	 * @return the invalidObsTableModel
	 */
	public InvalidObservationTableModel getInvalidObsTableModel() {
		return invalidObsTableModel;
	}

	// Singleton member, constructor, and getter.

	private static ModelManager docMgr = new ModelManager();

	/**
	 * Private constructor.
	 */
	private ModelManager() {
		this.newStarNotifier = new Notifier<NewStarType>();
		this.modeChangeNotifier = new Notifier<ModeType>();
		
		this.currentMode = ModeType.PLOT_OBS_MODE;
		this.newStarFileName = null;
	}

	/**
	 * Return the Singleton instance.
	 */
	public static ModelManager getInstance() {
		return docMgr;
	}

	/**
	 * @return the obsPlotModel
	 */
	public ObservationPlotModel getObsPlotModel() {
		return obsPlotModel;
	}

	/**
	 * @return the newStarFileName
	 */
	public String getNewStarFileName() {
		return newStarFileName;
	}
}
