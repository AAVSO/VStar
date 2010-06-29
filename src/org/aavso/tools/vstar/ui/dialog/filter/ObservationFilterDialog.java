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
package org.aavso.tools.vstar.ui.dialog.filter;

import java.awt.Container;
import java.awt.Dimension;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.data.filter.IObservationFieldMatcher;
import org.aavso.tools.vstar.data.filter.ObservationFilter;
import org.aavso.tools.vstar.ui.dialog.AbstractOkCancelDialog;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This dialog permits the user to specify a conjunctive filter, i.e. a set of
 * sub-filters each of which must be true (i.e. provide an match) for the
 * overall filter to be true (i.e. match). The result of applying a filter is
 * that some subset of the current observations will be captured in a collection
 * and a message sent.
 */
public class ObservationFilterDialog extends AbstractOkCancelDialog implements
		Listener<NewStarMessage> {

	private NewStarMessage newStarMessage;

	private ObservationFilter filter;

	private List<ObservationFilterPane> filterPanes;

	/**
	 * Constructor.
	 */
	public ObservationFilterDialog() {
		super("Filter Observations");

		newStarMessage = null;

		filter = new ObservationFilter();

		filterPanes = new LinkedList<ObservationFilterPane>();

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
	}

	// Create a filter pane.
	private JPanel createFiltersPane() {
		JPanel panel = new JPanel();

		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Create filter panes for each unique filter.
		for (int i = 0; i < ObservationFilter.MATCHERS.size(); i++) {
			ObservationFilterPane filterPane = new ObservationFilterPane();
			filterPanes.add(filterPane);
			panel.add(filterPane);
			panel.add(Box.createRigidArea(new Dimension(75, 10)));
		}

		return panel;
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
		// Add all non-null (valid) matchers to the filter.
		for (ObservationFilterPane filterPane : filterPanes) {
			IObservationFieldMatcher matcher = filterPane.getFieldMatcher();

			if (matcher != null) {
				filter.addMatcher(matcher);
			}
		}

		// TODO: at time of filtering, also get current mean obs list
		// and apply getFilteredObservations() to that, then create the
		// set of valid obs from both filterings?
		
		// Apply the filter (and all its sub-filters) to the full set of
		// observations.
		List<ValidObservation> obs = newStarMessage.getObservations();
		
		Set<ValidObservation> filteredObs = filter
				.getFilteredObservations(obs);

		// Send a message containing the observation subset.
		FilteredObservationMessage msg = new FilteredObservationMessage(this,
				filteredObs);

		Mediator.getInstance().getFilteredObservationNotifier()
				.notifyListeners(msg);

		// Clear state for next use of this dialog.
		filter.reset();
		
		setVisible(false);
	}

	@Override
	public void update(NewStarMessage info) {
		newStarMessage = info;
	}

	@Override
	public boolean canBeRemoved() {
		return false;
	}
}
