/**
 * VStar: a statistical analysis tool for variable star data.
 * Copyright (C) 2012  AAVSO (http://www.aavso.org/)
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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
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

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.DocumentManager;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This dialog collects filter information, permitting their selection and
 * deletion, and most importantly, permitting previously created filters to be
 * recreated.
 */
@SuppressWarnings("serial")
public class ObservationFiltersDialog extends JDialog implements
		ListSelectionListener {

	private boolean firstUse;

	private JList filterList;
	private DefaultListModel filterListModel;

	private Map<String, FilteredObservationMessage> filterMap;

	private JButton selectButton;
	private JButton showDescriptionButton;
	private JButton createSeriesButton;
	private JButton deleteButton;

	/**
	 * Constructor.
	 */
	public ObservationFiltersDialog() {
		super(DocumentManager.findActiveWindow());
		this.setTitle("Filters");
		this.setModal(true);

		this.firstUse = true;
		this.filterMap = new TreeMap<String, FilteredObservationMessage>();

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
			setLocationRelativeTo(Mediator.getUI().getContentPane());
			firstUse = false;
		}

		this.getRootPane().setDefaultButton(selectButton);
		this.setVisible(true);
	}

	private JPanel createListPane() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		filterListModel = new DefaultListModel();
		filterList = new JList(filterListModel);
		filterList
				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		filterList.addListSelectionListener(this);
		JScrollPane modelListScroller = new JScrollPane(filterList);

		panel.add(modelListScroller);

		return panel;
	}

	private JPanel createButtonPane() {
		JPanel panel = new JPanel(new FlowLayout());

		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(createDeleteButtonListener());
		deleteButton.setEnabled(false);
		panel.add(deleteButton);

		showDescriptionButton = new JButton("Show Description");
		showDescriptionButton
				.addActionListener(createShowDescriptionButtonListener());
		showDescriptionButton.setEnabled(false);
		panel.add(showDescriptionButton);

		createSeriesButton = new JButton("Create Series");
		createSeriesButton
				.addActionListener(createCreateSeriesButtonListener());
		createSeriesButton.setEnabled(false);
		panel.add(createSeriesButton);

		selectButton = new JButton("Select");
		selectButton.addActionListener(createSelectButtonListener());
		selectButton.setEnabled(false);
		panel.add(selectButton);

		this.getRootPane().setDefaultButton(selectButton);

		return panel;
	}

	// List selection listener to update button states.
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {

			if (filterList.getSelectedIndex() == -1) {
				selectButton.setEnabled(false);
				showDescriptionButton.setEnabled(false);
				deleteButton.setEnabled(false);
				createSeriesButton.setEnabled(false);
			} else {
				selectButton.setEnabled(true);
				showDescriptionButton.setEnabled(true);
				deleteButton.setEnabled(true);
				createSeriesButton.setEnabled(true);
			}
		}
	}

	// Return a listener for the "Select" button.
	private ActionListener createSelectButtonListener() {
		final ObservationFiltersDialog me = this;
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedModelIndex = filterList.getSelectedIndex();
				String desc = (String) filterListModel.get(selectedModelIndex);
				FilteredObservationMessage filterMsg = filterMap.get(desc);

				FilteredObservationMessage newFilterMsg = new FilteredObservationMessage(
						me, filterMsg.getDescription(), filterMsg
								.getFilteredObs());

				Mediator.getInstance().getFilteredObservationNotifier()
						.notifyListeners(newFilterMsg);
			}
		};
	}

	// Return a listener for the "Show Filter" button.
	private ActionListener createShowDescriptionButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedFilterIndex = filterList.getSelectedIndex();
				String desc = (String) filterListModel.get(selectedFilterIndex);
				FilteredObservationMessage filterMsg = filterMap.get(desc);
				List<ITextComponent> fields = new ArrayList<ITextComponent>();
				fields.add(new TextField("Name", filterMsg.getDescription()
						.getFilterName()));
				fields.add(new TextField("Description", filterMsg
						.getDescription().getFilterDescription(),
						TextField.Kind.AREA));
				new TextDialog("Description", fields);
			}
		};
	}

	// Return a listener for the "Delete" button.
	private ActionListener createDeleteButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedModelIndex = filterList.getSelectedIndex();

				String desc = (String) filterListModel
						.remove(selectedModelIndex);
				filterMap.remove(desc);
				pack();
			}
		};
	}

	// Return a listener for the "Create Series" button.
	private ActionListener createCreateSeriesButtonListener() {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int selectedFilterIndex = filterList.getSelectedIndex();
				String desc = (String) filterListModel.get(selectedFilterIndex);
				FilteredObservationMessage msg = filterMap.get(desc);
				List<ValidObservation> obs = new ArrayList<ValidObservation>(
						msg.getFilteredObs());
				SeriesTypeCreationDialog dlg = new SeriesTypeCreationDialog(obs);
				dlg.showDialog();
			}
		};
	}

	/**
	 * Return a filter listener, the purpose of which is to store new filter
	 * information.
	 */
	public Listener<FilteredObservationMessage> createFilterListener() {
		final ObservationFiltersDialog me = this;
		return new Listener<FilteredObservationMessage>() {
			@Override
			public void update(FilteredObservationMessage info) {
				if (info.getSource() != me) {
					String desc = info.getDescription().getFilterName();

					// Remove an existing filter.
					if (filterMap.containsKey(desc)) {
						filterMap.remove(desc);
						filterListModel.removeElement(desc);
					}

					filterMap.put(desc, info);
					filterListModel.addElement(desc);
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
	 * Return a new star listener, the purpose of which is to clear the
	 * collections and disable the buttons.
	 */
	public Listener<NewStarMessage> createNewStarListener() {
		return new Listener<NewStarMessage>() {
			@Override
			public void update(NewStarMessage info) {
				filterListModel.clear();
				pack();
				filterMap.clear();
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
