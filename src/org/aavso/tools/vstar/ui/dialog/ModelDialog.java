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
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.ModelCreationMessage;
import org.aavso.tools.vstar.ui.mediator.message.ModelSelectionMessage;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.util.model.IModel;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This dialog displays created models and permits their selection and deletion.
 */
public class ModelDialog extends JDialog implements ListSelectionListener {

	private boolean firstUse;

	private JList modelList;
	private DefaultListModel modelListModel;

	private Map<String, IModel> modelMap;

	private JButton selectButton;
	private JButton deleteButton;

	/**
	 * Constructor.
	 */
	public ModelDialog() {
		super();
		this.setTitle("Models");
		this.setModal(true);
		this.setAlwaysOnTop(true);

		this.firstUse = true;
		this.modelMap = new TreeMap<String, IModel>();

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

		modelListModel = new DefaultListModel();
		modelList = new JList(modelListModel);
		modelList
				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		modelList.addListSelectionListener(this);
		JScrollPane modelListScroller = new JScrollPane(modelList);

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

	        if (modelList.getSelectedIndex() == -1) {
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
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedModelIndex = modelList.getSelectedIndex();
				String desc = (String) modelListModel.get(selectedModelIndex);
				IModel model = modelMap.get(desc);
				ModelSelectionMessage msg = new ModelSelectionMessage(this,
						model);
				Mediator.getInstance().getModelSelectionNofitier()
						.notifyListeners(msg);
			}
		};
	}

	// Return a listener for the "Delete" button.
	private ActionListener createDeleteButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedModelIndex = modelList.getSelectedIndex();

				String desc = (String) modelListModel
						.remove(selectedModelIndex);
				modelMap.remove(desc);
			}
		};
	}

	/**
	 * Return a model creation listener.
	 */
	public Listener<ModelCreationMessage> createModelCreationListener() {
		return new Listener<ModelCreationMessage>() {
			@Override
			public void update(ModelCreationMessage info) {
				String desc = info.getModel().getDescription();

				if (!modelMap.containsKey(desc)) {
					modelMap.put(desc, info.getModel());

					modelListModel.addElement(desc);
					pack();
				}
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * Return a new star listener.
	 */
	public Listener<NewStarMessage> createNewStarListener() {
		return new Listener<NewStarMessage>() {
			@Override
			public void update(NewStarMessage info) {
				modelList.removeAll();
				modelMap.clear();
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
