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
package org.aavso.tools.vstar.ui.pane.list;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.RowFilter;

import org.aavso.tools.vstar.data.IOrderedObservationSource;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.DiscrepantObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.ExcludedObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.FilteredObservationMessage;
import org.aavso.tools.vstar.ui.mediator.message.SeriesCreationMessage;
import org.aavso.tools.vstar.ui.mediator.message.SeriesVisibilityChangeMessage;
import org.aavso.tools.vstar.ui.model.list.ValidObservationTableModel;
import org.aavso.tools.vstar.util.notification.Listener;

/**
 * This class filters observations by whether or not they are visible in the
 * plot, information obtained from the model.
 */
public class VisibleSeriesRowFilter extends RowFilter<IOrderedObservationSource, Integer> {

	private ValidObservationTableModel tableModel;
	private Set<SeriesType> visibleSeries;
	private Set<ValidObservation> filteredObs;
	private Map<SeriesType, List<ValidObservation>> userDefinedObs;
	private AnalysisType analysisType;

	/**
	 * Constructor
	 * 
	 * @param visibleSeries The initially visible series.
	 * @param analysisType  The analysis type (raw, phase) under which this table
	 *                      was created.
	 */
	public VisibleSeriesRowFilter(ValidObservationTableModel tableModel, Set<SeriesType> visibleSeries,
			AnalysisType analysisType) {
		super();
		this.tableModel = tableModel;
		this.visibleSeries = visibleSeries;
		this.filteredObs = null;
		this.userDefinedObs = new HashMap<SeriesType, List<ValidObservation>>();
		this.userDefinedObs.putAll(tableModel.getObsSourceListMap());
		this.analysisType = analysisType;

		Mediator.getInstance().getSeriesVisibilityChangeNotifier().addListener(createSeriesVisibilityChangeListener());
		Mediator.getInstance().getFilteredObservationNotifier().addListener(createFilteredObservationListener());
		Mediator.getInstance().getExcludedObservationNotifier().addListener(createExcludedObservationListener());
		Mediator.getInstance().getDiscrepantObservationNotifier().addListener(createDiscrepantObservationListener());
		Mediator.getInstance().getSeriesCreationNotifier().addListener(createSeriesCreationListener());
	}

	@Override
	public boolean include(javax.swing.RowFilter.Entry<? extends IOrderedObservationSource, ? extends Integer> entry) {

		boolean visible = false;

		int id = entry.getIdentifier();
		IOrderedObservationSource model = entry.getModel();
		ValidObservation ob = model.getObservations().get(id);

		SeriesType series;

		if (ob.isDiscrepant()) {
			series = SeriesType.DISCREPANT;
		} else if (ob.isExcluded()) {
			series = SeriesType.Excluded;
		} else if (ob.getMagnitude().isFainterThan()) {
			series = SeriesType.FAINTER_THAN;
		} else {
			series = ob.getBand();
		}

		if (ob.getSeries() == ob.getBand()) {
			visible = visibleSeries.contains(series);
		} else {
			series = ob.getSeries();
			if (userDefinedObs.containsKey(series)) {
				visible = visibleSeries.contains(series);
			}
		}

		if (!visible) {
			if (visibleSeries.contains(SeriesType.Filtered) && filteredObs != null) {
				// The observation is not visible because the series to which it belongs
				// in the plot is not visible currently. It may be filtered however, so check
				// whether the filtered series is in the set of visible series and whether the
				// observation is in the set of filtered observations.
				visible = filteredObs.contains(ob);
			}
		}

		return visible;
	}

	// Returns a filtered observation listener to update the set of filtered
	// observations.
	private Listener<FilteredObservationMessage> createFilteredObservationListener() {
		return new Listener<FilteredObservationMessage>() {
			public void update(FilteredObservationMessage info) {
				if (Mediator.getInstance().getAnalysisType() == analysisType) {
					if (info == FilteredObservationMessage.NO_FILTER) {
						// Do nothing. We want to keep the filtered observations
						// in case the Filter series is selected again.
					} else {
						filteredObs = info.getFilteredObs();
					}
					tableModel.fireTableDataChanged();
				}
			}

			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	// Returns a series visibility change listener to update the visible
	// series.
	private Listener<SeriesVisibilityChangeMessage> createSeriesVisibilityChangeListener() {
		return new Listener<SeriesVisibilityChangeMessage>() {
			public void update(SeriesVisibilityChangeMessage info) {
				if (Mediator.getInstance().getAnalysisType() == analysisType) {
					visibleSeries = info.getVisibleSeries();
					tableModel.fireTableDataChanged();
				}
			}

			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	// Returns a series creation listener to trigger a row filtering
	// operation.
	protected Listener<SeriesCreationMessage> createSeriesCreationListener() {
		return new Listener<SeriesCreationMessage>() {
			@Override
			public void update(SeriesCreationMessage info) {
				if (Mediator.getInstance().getAnalysisType() == analysisType) {
					userDefinedObs.put(info.getType(), info.getObs());
					tableModel.fireTableDataChanged();
				}
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	// Returns an excluded observation listener to trigger a row filtering
	// operation.
	private Listener<ExcludedObservationMessage> createExcludedObservationListener() {
		return new Listener<ExcludedObservationMessage>() {
			@Override
			public void update(ExcludedObservationMessage info) {
				if (Mediator.getInstance().getAnalysisType() == analysisType) {
					tableModel.fireTableDataChanged();
				}
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}

	// Returns a discrepant observation listener to trigger a row filtering
	// operation.
	private Listener<DiscrepantObservationMessage> createDiscrepantObservationListener() {
		return new Listener<DiscrepantObservationMessage>() {
			@Override
			public void update(DiscrepantObservationMessage info) {
				if (Mediator.getInstance().getAnalysisType() == analysisType) {
					tableModel.fireTableDataChanged();
				}
			}

			@Override
			public boolean canBeRemoved() {
				return true;
			}
		};
	}
}
