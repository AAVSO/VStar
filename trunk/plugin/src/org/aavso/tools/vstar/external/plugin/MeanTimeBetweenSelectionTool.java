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
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This plug-in accumulates observation selections and allows mean time between
 * selections to be calculated.
 */
public class MeanTimeBetweenSelectionTool extends GeneralToolPluginBase {

	private ObservationCollectionDialog obsCollectionDialog;

	public MeanTimeBetweenSelectionTool() {
		obsCollectionDialog = new ObservationCollectionDialog();

		Mediator.getInstance().getObservationSelectionNotifier().addListener(
				createObSelectionListener());
	}

	@Override
	public void invoke() {
		obsCollectionDialog.showDialog();
	}

	@Override
	public String getDescription() {
		return "Accumulates selected observations and calculates mean time between selections.";
	}

	@Override
	public String getDisplayName() {
		return "Mean time between selections";
	}

	private Listener<ObservationSelectionMessage> createObSelectionListener() {
		return new Listener<ObservationSelectionMessage>() {
			@Override
			public void update(ObservationSelectionMessage info) {
				// TODO: why do we see multiple update() calls per mouse click?
				obsCollectionDialog.addObservation(info.getObservation());
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	class ObservationCollectionDialog extends AbstractOkCancelDialog {

		private List<ValidObservation> obs;
		private DefaultListModel obModel;

		private JList obList;
		private JTextField meanField;

		public ObservationCollectionDialog() {
			super("Observation Selection");
			setAlwaysOnTop(true);
			setModal(false);

			obs = new ArrayList<ValidObservation>();

			Container contentPane = this.getContentPane();

			JPanel topPane = new JPanel();
			topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
			topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			obModel = new DefaultListModel();
			obList = new JList(obModel);
			obList.setBorder(BorderFactory.createTitledBorder("Observations"));
			topPane.add(obList);

			meanField = new JTextField();
			meanField.setBorder(BorderFactory.createTitledBorder("Mean time"));
			meanField.setEditable(false);
			topPane.add(meanField);

			topPane.add(createButtonPane());
			contentPane.add(topPane);

			this.pack();
		}

		public void addObservation(ValidObservation ob) {
			if (!obs.contains(ob)) {
				obModel.addElement(ob.toSimpleFormatString());
				obs.add(ob);
			}
		}

		@Override
		public void showDialog() {
			obModel.removeAllElements();
			obs.clear();
			super.showDialog();
		}

		@Override
		protected void okAction() {
			if (!obs.isEmpty()) {
				// Get the sum of all JD intervals between observations.
				double sum = 0;

				double lastJD = obs.get(0).getJD();

				for (int i = 1; i < obs.size(); i++) {
					ValidObservation ob = obs.get(i);
					sum += ob.getJD() - lastJD;
					lastJD = ob.getJD();
				}

				// Now take the mean of these JD intervals.
				double mean = sum / obs.size();

				meanField.setText(String.format("%f days", mean));

				setCancelled(false);
			}
		}

		@Override
		protected void cancelAction() {
			// Nothing to do.
		}
	}
}
