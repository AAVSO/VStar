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
package org.aavso.tools.vstar.ui.dialog.search;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.text.Format;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.ListDataListener;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.filter.ObservationFilter;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.NewStarMessage;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This dialog permits the user to specify a conjunctive filter, i.e. a set of
 * sub-filters each of which must be true (i.e. provide an match) for the
 * overall filter to be true (i.e. match). The result of applying a filter is
 * that some subset of the current observations will be captured in a collection
 * and a message sent.
 */
public class ObservationFilterDialog extends AbstractOkCancelDialog {

	private final static String NONE = "None";

	private final static Map<Class<?>, Format> classToFormatMap;

	static {
		classToFormatMap = new HashMap<Class<?>, Format>();
		classToFormatMap.put(Double.class, NumberFormat.getInstance());
	}

	private NewStarMessage newStarMessage;

	private ObservationFilter filter;

	private List<Component> filterPanes;

	/**
	 * Constructor.
	 */
	public ObservationFilterDialog() {
		super("Filter Observations");

		newStarMessage = null;

		filter = new ObservationFilter();

		filterPanes = new LinkedList<Component>();

		Container contentPane = this.getContentPane();

		JPanel topPane = new JPanel();
		topPane.setLayout(new BoxLayout(topPane, BoxLayout.PAGE_AXIS));
		topPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Filters
		topPane.add(createFiltersPane());

		// OK, Cancel
		topPane.add(createButtonPane());

		contentPane.add(topPane);

		this.pack();

		Mediator.getInstance().getNewStarNotifier().addListener(
				createNewStarListener());
	}

	// Create a filter pane.
	private JPanel createFiltersPane() {
		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Create the first filter.
		Component filterPane = createFilter();
		// TODO: if a filterPane is removed, validate() must be called on
		// 'panel'.
		filterPanes.add(filterPane);
		panel.add(filterPane);

		panel.add(Box.createRigidArea(new Dimension(75, 10)));

		return panel;
	}

	// Create a single filter.
	// TODO: make this a separate Pane class so it knows which bits
	// are selected etc.

	private Component createFilter() {
		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createEtchedBorder());

		// Create the filter name menu.
		JComboBox filterNamesList = new JComboBox(new String[] { NONE });
		for (String filterName : ObservationFilter.MATCHERS.keySet()) {
			filterNamesList.addItem(filterName);
		}
		panel.add(filterNamesList);

		panel.add(Box.createHorizontalGlue());

		// Create the filter operations menu.
		JComboBox filterOpsList = new JComboBox(new String[] { NONE });
		// filterOpsList.setModel();
		// TODO: model must dynamically change when names menu item selected
		panel.add(filterOpsList);

		panel.add(Box.createHorizontalGlue());

		// Create the value text field.
		// TODO: use JFormattedTextField, classToFormatMap
		JTextField valueField = new JTextField("        ");
		panel.add(valueField);

		panel.add(Box.createHorizontalGlue());

		// Create +/- buttons.
		// TODO: handle these properly later
		// TODO: should be disabled when only one filter exists
		JButton removeButton = new JButton("-");
		panel.add(removeButton);

		panel.add(Box.createHorizontalGlue());

		JButton addButton = new JButton("+");
		panel.add(addButton);

		return panel;
	}

	private Listener<NewStarMessage> createNewStarListener() {
		return new Listener<NewStarMessage>() {
			@Override
			public void update(NewStarMessage info) {
				newStarMessage = info;
			}

			@Override
			public boolean canBeRemoved() {
				return false;
			}
		};
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#cancelAction()
	 */
	@Override
	protected void cancelAction() {
		// Nothing to be done.
	}

	/**
	 * @see org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog#okAction()
	 */
	@Override
	protected void okAction() {
		// TODO: check that all filters are valid, add each to filter
		// Qun: How to create filter objects? Via "prototype" creation
		// methods on an instance or an if statement and instanceof?

		if (true) {
			List<ValidObservation> obs = newStarMessage.getObservations();

			Map<Integer, ValidObservation> filteredObs = filter
					.getFilteredObservations(obs);

			// TODO: send message with obs map
			
			setVisible(false);
		}
	}
}
