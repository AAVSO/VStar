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
package org.aavso.tools.vstar.ui.pane.plot;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.dialog.MessageBox;
import org.aavso.tools.vstar.ui.dialog.PhaseParameterDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.AnalysisTypeChangeMessage;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This class represents a widget containing a button that when pressed
 * will result in a new phase plot for the current dataset being generated 
 * after suitable parameter entry via a dialog.
 * 
 * @deprecated See View and Analysis menus for phase plot items.
 */
public class NewPhasePlotButtonPane extends JButton {

	private ObservationAndMeanPlotPane plotPane;
	
	/**
	 * Constructor
	 * 
	 * @param visibilityMapSrc
	 *            A source of plot series visibility mappings.
	 */
	public NewPhasePlotButtonPane(PhaseAndMeanPlotPane plotPane) {
		super("New Phase Plot");
		this.plotPane = plotPane;		
		this.addActionListener(this.createNewPhasePlotButtonListener());
	}

	// Return a listener for the "new phase plot" button.
	private ActionListener createNewPhasePlotButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					PhaseParameterDialog phaseDialog = Mediator.getInstance()
							.getPhaseParameterDialog();
					phaseDialog.showDialog();
					if (!phaseDialog.isCancelled()) {
						double period = phaseDialog.getPeriod();
						double epoch = phaseDialog.getEpoch();
						MainFrame.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						
						Mediator.getInstance().createPhasePlotArtefacts(period,
								epoch, plotPane.getObsModel().getSeriesVisibilityMap());
						
						MainFrame.getInstance().setCursor(null);
					}
				} catch (Exception ex) {
					MainFrame.getInstance().setCursor(null);
					MessageBox.showErrorDialog(MainFrame.getInstance(),
							"New Phase Plot", ex);
				}
			}
		};
	}
	
	// Returns an analysis type change listener.
	private Listener<AnalysisTypeChangeMessage> createAnalysisTypeChangeListener() {
		return new Listener<AnalysisTypeChangeMessage>() {
			@Override
			public void update(AnalysisTypeChangeMessage msg) {
				plotPane = msg.getObsAndMeanChartPane();
			}
			
			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}
}
