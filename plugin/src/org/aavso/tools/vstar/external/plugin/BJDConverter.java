/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2010  AAVSO (http://www.aavso.org/)
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
package org.aavso.tools.vstar.external.plugin;

import java.awt.Container;
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.ValidObservation.JDflavour;
import org.aavso.tools.vstar.plugin.ObservationToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.StarInfo;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.model.plot.ISeriesInfoProvider;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.coords.DecInfo;
import org.aavso.tools.vstar.util.coords.RAInfo;
import org.aavso.tools.vstar.util.help.Help;

import org.aavso.tools.vstar.external.lib.ConvertHelper;

/**
 * Converts currently loaded observations to BJD_TDB if they are not already
 * in BJD_TDB.
 * 
 * TODO:<br/>
 * - undoable edits!
 * 
 */
public class BJDConverter extends ObservationToolPluginBase {

	private static final int CHUNK_SIZE = 200;
	
	private static Cursor waitCursor = new Cursor(Cursor.WAIT_CURSOR);
	
	@Override
	public String getDisplayName() {
		return "BJD_TDB Converter";
	}

	@Override
	public String getDescription() {
		return "BJD_TDB Converter";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "BJD Converter.pdf";
	}

	@Override
	public void invoke(ISeriesInfoProvider seriesInfo) {
		NewStarMessage msg = Mediator.getInstance().getLatestNewStarMessage();
		if (msg != null) {
			List<ValidObservation> obs = Mediator.getInstance().getValidObsList();
			int count = 0;			
			for (ValidObservation ob : obs) {
				if (ob.getJDflavour() == JDflavour.JD || ob.getJDflavour() == JDflavour.HJD) {
					count++;
				}
			}
			if (count == 0) {
				MessageBox.showMessageDialog("BJD_TDB Converter",
						"No observations with JD or HJD");
				return;
			}
			if (!showConfirmDialog2("BJD_TDB Converter", count + " JD or/and HJD observations found. Convert them to BJD_TDB?", getDocName()))
				return;
			Pair<RAInfo, DecInfo> coords = getCoordinates(msg.getStarInfo());
			if (coords != null) {
				Pair<Integer, Integer> result = null;
				Container contentPane = Mediator.getUI().getContentPane();
				Cursor defaultCursor = contentPane.getCursor();
				contentPane.setCursor(waitCursor);
				try {
					result = convertObsToTDB(obs, coords.first, coords.second);
				} finally {
					contentPane.setCursor(defaultCursor);
				}
				if (result != null) {				
					updateUI();
					String message = null;
					if (result.first > 0) {
						message = String.format("%d JD observations converted.", result.first);
					}
					if (result.second > 0) {
						if (message != null)
							message += "\n";
						else
							message = "";
						message += String.format("%d HJD observations converted.", result.second);
					}
					MessageBox.showMessageDialog("BJD_TDB Converter", message);
				} else {
					// We should never be here
					MessageBox.showWarningDialog("BJD_TDB Converter",
							"The previously loaded observations have NOT been converted to BJD_TDB.");
					
				}
			} else {
				MessageBox.showWarningDialog("BJD_TDB Converter",
						"Canceled by user: the previously loaded observations have NOT been converted to BJD_TDB.");
			}
		}
	}

	private boolean showConfirmDialog2(String title, String msg, String helpTopic) {
		ConfirmDialogWithHelp dlg = new ConfirmDialogWithHelp(title, msg, helpTopic);
		return !dlg.isCancelled();
	}
	
	@SuppressWarnings("serial")
	private class ConfirmDialogWithHelp extends AbstractOkCancelDialog {
		
		String helpTopic;
		
		ConfirmDialogWithHelp(String title, String msg, String helpTopic) {
			super(title);
			
			this.helpTopic = helpTopic;
			
			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
			topPane.add(createMessagePane(msg));

			// OK, Cancel, Help
			JPanel buttonPane = createButtonPane2();
			topPane.add(buttonPane);
			this.helpTopic = helpTopic;

			contentPane.add(topPane);
			
			this.pack();
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			okButton.requestFocusInWindow();
			this.setVisible(true);
			
		}
		
		private JPanel createMessagePane(String msg) {
			JPanel panel = new JPanel();
			JLabel labelMsg = new JLabel(msg);
			panel.add(labelMsg);
			return panel;
		}

		/**
		 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#helpAction()
		 */
		@Override
		protected void helpAction() {
			Help.openPluginHelp(helpTopic);
		}

		/**
		 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
		 */
		@Override
		protected void cancelAction() {
			// Nothing to do.
		}

		/**
		 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
		 */
		@Override
		protected void okAction() {
			cancelled = false;
			setVisible(false);
			dispose();
		}
	}
	
	/**
	 * Return RA and Dec. First look for coordinates in any of our loaded
	 * datasets. Use the first coordinates found. We are making the simplifying
	 * assumption that all data sets correspond to the same object! If not
	 * found, ask the user to enter them. If none are supplied, null is
	 * returned.
	 * 
	 * @param info
	 *            a StarInfo object possibly containing coordinates
	 * @param otherCoords
	 *            Coordinates to use if info contains none.
	 * @return A pair of coordinates: RA and Declination
	 */
	private Pair<RAInfo, DecInfo> getCoordinates(StarInfo info) {
		RAInfo ra = info.getRA();
		DecInfo dec = info.getDec();

		if (ra == null || dec == null) {
			// Ask the user for J2000.0 RA/DEC and if that is cancelled,
			// indicate that BJD conversion cannot take place.
			ConvertHelper.CoordDialog coordDialog = new ConvertHelper.CoordDialog();
			if (coordDialog.isCancelled()) {
				return null;
			}
			Pair<RAInfo, DecInfo> coordinates = coordDialog.getCoordinates();
			ra = coordinates.first;
			dec = coordinates.second;
		}

		if (ra != null && dec != null) {
			return new Pair<RAInfo, DecInfo>(ra, dec);
		}
		
		return null;
	}
	
	/**
	 * Update UI
	 */
	private void updateUI() {
		
		// PMAK (2021-06-03):
		// There is no way to recalculate observation phases (as for VStar 2.21.3)
		// So we are switching to RAW plot and trying to delete existing phase plot.
		
		Mediator mediator = Mediator.getInstance();
		
		mediator.changeAnalysisType(AnalysisType.RAW_DATA);

		try {
			mediator.dropPhasePlotAnalysis();
		} catch (Exception e) {
			MessageBox.showWarningDialog("BJD Conversion", 
				"Cannot delete current Phase Plot. Please recreate it to reflect changes.");
		}
	
		// Updates RAW plot and data table.
		Mediator.getInstance().updatePlotsAndTables();
		
	}

	Pair<Integer, Integer> convertObsToTDB(List<ValidObservation> obs, RAInfo ra, DecInfo dec) {
		List<ValidObservation> obsJD = collectObservationsOfType(obs, JDflavour.JD);
		List<ValidObservation> obsHJD = collectObservationsOfType(obs, JDflavour.HJD);
		List<Double> timesUTCtoBJD;
		List<Double> timesHJDtoBJD;
		try {
			timesUTCtoBJD = getConvertedTimes(obsJD, ra, dec, JDflavour.JD);
		} catch (Exception ex) {
			MessageBox.showErrorDialog("Error", ex.getMessage());
			return null;
		}
		try {
			timesHJDtoBJD = getConvertedTimes(obsHJD, ra, dec, JDflavour.HJD);
		} catch (Exception ex) {
			MessageBox.showErrorDialog("Error", ex.getMessage());
			return null;
		}
		
		for (int i = 0; i < obsJD.size(); i++) {
			obsJD.get(i).setJD(timesUTCtoBJD.get(i));
			obsJD.get(i).setJDflavour(JDflavour.BJD);
		}
		for (int i = 0; i < obsHJD.size(); i++) {
			obsHJD.get(i).setJD(timesHJDtoBJD.get(i));
			obsHJD.get(i).setJDflavour(JDflavour.BJD);
		}
		return new Pair<Integer, Integer>(obsJD.size(), obsHJD.size());
	}
	
	private List<ValidObservation> collectObservationsOfType(List<ValidObservation> obs, JDflavour f) {
		List<ValidObservation> result = new ArrayList<ValidObservation>();
		for (ValidObservation ob : obs) {
			if (ob.getJDflavour() == f) {
				result.add(ob);
			}
		}
		return result;
	}

	private List<Double> getConvertedTimes(List<ValidObservation> obs, RAInfo ra, DecInfo dec, JDflavour f) throws Exception {
		List<Double> result = new ArrayList<Double>(); 
		
		String func = null;
		
		if (f == JDflavour.JD)
			func = "utc2bjd";
		else if (f == JDflavour.HJD)
			func = "hjd2bjd";
		else
			throw new Exception("Invalid JD flavor");
		
		List<ValidObservation> obs_chunk = null;
		
		int counter = 0;
		for (ValidObservation ob : obs) {
			if (ob.getJDflavour() != f) {
				throw new Exception("Invalid JD flavor");
			}
			if (obs_chunk == null) {
				obs_chunk = new ArrayList<ValidObservation>();
			}
			obs_chunk.add(ob);
			counter++;
			if (counter == CHUNK_SIZE) {
				result.addAll(convertChunk(obs_chunk, ra, dec, func));
				obs_chunk = null;
				counter = 0;
			}
		}
		if (obs_chunk != null) {
			result.addAll(convertChunk(obs_chunk, ra, dec, func));
			obs_chunk = null;
			counter = 0;
		}
		
		return result;
	}
	
	private List<Double> convertChunk(List<ValidObservation> obs_chunk, RAInfo ra, DecInfo dec, String func) throws Exception {
		List<Double> times = new ArrayList<Double>();
		for (ValidObservation ob : obs_chunk) {
			times.add(ob.getJD());
		}
		List<Double> result = ConvertHelper.getConvertedListOfTimes(times, ra.toDegrees(), dec.toDegrees(), func);
		return result;
	}
	
}
