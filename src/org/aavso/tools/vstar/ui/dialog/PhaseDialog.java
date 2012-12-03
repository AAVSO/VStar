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
package org.aavso.tools.vstar.ui.dialog;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.aavso.tools.vstar.ui.MainFrame;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.PhaseChangeMessage;
import org.aavso.tools.vstar.ui.mediator.message.PhaseSelectionMessage;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This dialog collects phase plot information, permitting their selection and
 * deletion, and most importantly, permitting previously created phase plots to
 * be recreated.
 */
@SuppressWarnings("serial")
public class PhaseDialog extends JDialog implements ListSelectionListener {

	private boolean firstUse;

	private JList phaselList;
	private DefaultListModel phaseListModel;

	private Map<String, PhaseChangeMessage> phaseMap;

	private JButton selectButton;
	private JButton deleteButton;

	/**
	 * Constructor.
	 */
	public PhaseDialog() {
		super(DocumentManager.findActiveWindow());
		this.setTitle("Phase Plots");
		this.setModal(true);

		this.firstUse = true;
		this.phaseMap = new TreeMap<String, PhaseChangeMessage>();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		topPane.add(createListPane());
		topPane.add(createButtonPane());

		getContentPane().add(topPane);
		pack();
	}

	/**
	 * Show the dialog. This is intended to be a Singleton class.
	 */
	public void showDialog() {
		if (firstUse) {
			setLocationRelativeTo(MainFrame.getInstance().getContentPane());
			firstUse = false;
		}

		this.getRootPane().setDefaultButton(selectButton);
		this.setVisible(true);
	}

	private JPanel createListPane() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		phaseListModel = new DefaultListModel();
		phaselList = new JList(phaseListModel);
		phaselList
				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		phaselList.addListSelectionListener(this);
		JScrollPane modelListScroller = new JScrollPane(phaselList);

		panel.add(modelListScroller);

		return panel;
	}

	private JPanel createButtonPane() {
		JPanel panel = new JPanel(new BorderLayout());

		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(createDeleteButtonListener());
		deleteButton.setEnabled(false);

		panel.add(deleteButton, BorderLayout.LINE_START);

		selectButton = new JButton("Select");
		selectButton.addActionListener(createSelectButtonListener());
		selectButton.setEnabled(false);
		panel.add(selectButton, BorderLayout.LINE_END);

		this.getRootPane().setDefaultButton(selectButton);

		return panel;
	}

	// List selection listener to update button states.
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {

			if (phaselList.getSelectedIndex() == -1) {
				selectButton.setEnabled(false);
				deleteButton.setEnabled(false);
			} else {
				selectButton.setEnabled(true);
				deleteButton.setEnabled(true);
			}
		}
	}

	// Return a listener for the "Select" button.
	private ActionListener createSelectButtonListener() {
		final PhaseDialog me = this;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedModelIndex = phaselList.getSelectedIndex();
				String desc = (String) phaseListModel.get(selectedModelIndex);
				PhaseChangeMessage changeMsg = phaseMap.get(desc);

				PhaseSelectionMessage selectionMsg = new PhaseSelectionMessage(
						me, changeMsg.getPeriod(), changeMsg.getEpoch(),
						changeMsg.getSeriesVisibilityMap());

				Mediator.getInstance().getPhaseSelectionNotifier()
						.notifyListeners(selectionMsg);
			}
		};
	}

	// Return a listener for the "Delete" button.
	private ActionListener createDeleteButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedModelIndex = phaselList.getSelectedIndex();

				String desc = (String) phaseListModel
						.remove(selectedModelIndex);
				phaseMap.remove(desc);
				pack();
			}
		};
	}

	/**
	 * Return a phase change listener, the purpose of which is to store new
	 * phase plot information.
	 */
	public Listener<PhaseChangeMessage> createPhaseChangeListener() {
		final PhaseDialog me = this;
		return new Listener<PhaseChangeMessage>() {
			@Override
			public void update(PhaseChangeMessage info) {
				if (info.getSource() != me) {
					String desc = info.toString();

					if (!phaseMap.containsKey(desc)) {
						phaseMap.put(desc, info);

						phaseListModel.addElement(desc);
						pack();
					}
				}
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * Return a new star listener, the purpose of which is to clear the
	 * collections and disable the buttons.
	 */
	public Listener<NewStarMessage> createNewStarListener() {
		return new Listener<NewStarMessage>() {
			@Override
			public void update(NewStarMessage info) {
				phaseListModel.clear();
				pack();
				phaseMap.clear();
				selectButton.setEnabled(false);
				deleteButton.setEnabled(false);
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}
}
