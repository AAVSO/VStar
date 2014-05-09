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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.ObservationSelectionMessage;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.prefs.NumericPrecisionPrefs;

/**
 * This plug-in accumulates observation selections (e.g. minima or maxima) and
 * allows mean time between selections to be calculated.
 */
public class MeanTimeBetweenSelectionTool extends GeneralToolPluginBase {

	@Override
	public void invoke() {
		new ObservationCollectionDialog();
	}

	@Override
	public String getDescription() {
		return "Accumulates selected observations and calculates mean time between selections";
	}

	@Override
	public String getDisplayName() {
		return "Mean time between selections";
	}

	@SuppressWarnings("serial")
	class ObservationCollectionDialog extends JDialog {

		private List<ValidObservation> obs;
		private DefaultListModel obModel;

		private JList obList;
		private JTextField meanJDField;
		private JTextField meanMagField;

		ObservationCollectionDialog() {
			super(DocumentManager.findActiveWindow());
			setTitle("Observation Selection");
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

			meanJDField = new JTextField();
			meanJDField.setBorder(BorderFactory
					.createTitledBorder("Mean time between selections"));
			meanJDField.setEditable(false);
			topPane.add(meanJDField);

			meanMagField = new JTextField();
			meanMagField.setBorder(BorderFactory
					.createTitledBorder("Mean magnitude"));
			meanMagField.setEditable(false);
			topPane.add(meanMagField);

			topPane.add(createButtonPane());
			contentPane.add(topPane);

			Mediator.getInstance().getObservationSelectionNotifier()
					.addListener(createObSelectionListener());

			this.pack();
			this
					.setLocationRelativeTo(Mediator.getUI()
							.getContentPane());
			this.setVisible(true);
		}

		protected JPanel createButtonPane() {
			JPanel panel = new JPanel(new BorderLayout());

			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(createCancelButtonListener());
			panel.add(cancelButton, BorderLayout.LINE_START);

			JButton clearButton = new JButton("Clear");
			clearButton.addActionListener(createClearButtonListener());
			panel.add(clearButton, BorderLayout.LINE_END);

			this.getRootPane().setDefaultButton(clearButton);

			return panel;
		}

		// Return a listener for the "Clear" button.
		protected ActionListener createClearButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					obModel.clear();
					obs.clear();
					meanJDField.setText("");
					meanMagField.setText("");
					pack();
				}
			};
		}

		// Return a listener for the "cancel" button.
		protected ActionListener createCancelButtonListener() {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			};
		}

		private Listener<ObservationSelectionMessage> createObSelectionListener() {
			return new Listener<ObservationSelectionMessage>() {
				@Override
				public void update(ObservationSelectionMessage info) {
					// TODO: why do we see multiple update() calls per mouse
					// click? Raise a tracker re: this.
					addObservation(info.getObservation());
				}

				@Override
				public boolean canBeRemoved() {
					return false;
				}
			};
		}

		public void addObservation(ValidObservation ob) {
			if (!obs.contains(ob)) {
				obModel.addElement(String.format("JD: "
						+ NumericPrecisionPrefs.getTimeOutputFormat()
						+ ", Mag: "
						+ NumericPrecisionPrefs.getMagOutputFormat(), ob
						.getJD(), ob.getMag()));
				obs.add(ob);
				if (obs.size() > 1) {
					computeMeanJD();
					computeMeanMag();
				}
				pack();
			}
		}

		private void computeMeanJD() {
			// Get the sum of all JD intervals between observations.
			double sum = 0;

			double lastJD = obs.get(0).getJD();

			for (int i = 1; i < obs.size(); i++) {
				ValidObservation ob = obs.get(i);
				sum += ob.getJD() - lastJD;
				lastJD = ob.getJD();
			}

			// There's N JDs, but only N-1 intervals.
			double mean = sum / (obs.size()-1);

			meanJDField.setText(String.format(NumericPrecisionPrefs
					.getTimeOutputFormat()
					+ " days", mean));
		}

		private void computeMeanMag() {
			// Get the sum of all observation magnitude values.
			double sum = 0;

			for (int i = 0; i < obs.size(); i++) {
				ValidObservation ob = obs.get(i);
				sum += ob.getMag();
			}

			double mean = sum / obs.size();

			meanMagField.setText(String.format(NumericPrecisionPrefs
					.getMagOutputFormat(), mean));
		}
	}
}
