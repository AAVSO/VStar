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
package org.aavso.tools.vstar.ui.pane.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JTable;

import org.aavso.tools.vstar.data.InvalidObservation;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.model.list.InvalidObservationTableModel;
import org.aavso.tools.vstar.ui.model.list.SimpleFormatRawDataColumnInfoSource;
import org.aavso.tools.vstar.ui.model.list.ValidObservationTableModel;

import junit.framework.TestCase;

/**
 * Pure unit tests for {@link ObservationListPane}.
 *
 * Construction is safe headlessly: the constructor uses
 * {@code Mediator.getInstance()} (lazy singleton) and creates a plain
 * {@code JPanel}, so no {@code setVisible} or {@code Mediator.getUI()} call is
 * needed.
 *
 * Part of issue #579 (prong C): GUI code coverage.
 */
public class ObservationListPaneTest extends TestCase {

	private static final double JD1 = 2451545.0;
	private static final double JD2 = 2451546.0;
	private static final double MAG = 5.5;
	private static final double UNCERTAINTY = 0.01;

	private ValidObservation ob1;
	private ValidObservation ob2;
	private List<ValidObservation> observations;
	private ValidObservationTableModel validModel;
	private Set<SeriesType> visibleSeries;

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);

		ob1 = new ValidObservation();
		ob1.setJD(JD1);
		ob1.setMagnitude(new Magnitude(MAG, UNCERTAINTY));
		ob1.setBand(SeriesType.Visual);

		ob2 = new ValidObservation();
		ob2.setJD(JD2);
		ob2.setMagnitude(new Magnitude(6.0, 0.02));
		ob2.setBand(SeriesType.Visual);

		observations = new ArrayList<ValidObservation>(Arrays.asList(ob1, ob2));

		Map<SeriesType, List<ValidObservation>> obsMap =
				new HashMap<SeriesType, List<ValidObservation>>();
		obsMap.put(SeriesType.Visual, observations);

		validModel = new ValidObservationTableModel(obsMap, observations,
				new SimpleFormatRawDataColumnInfoSource());

		visibleSeries = new HashSet<SeriesType>(Arrays.asList(SeriesType.Visual));
	}

	public void testConstructionWithValidDataOnly() {
		ObservationListPane pane = new ObservationListPane(
				"Test", validModel, null, true, visibleSeries,
				AnalysisType.RAW_DATA);
		assertNotNull(pane);
	}

	public void testGetValidDataTableIsNotNull() {
		ObservationListPane pane = new ObservationListPane(
				"Test", validModel, null, true, visibleSeries,
				AnalysisType.RAW_DATA);
		JTable table = pane.getValidDataTable();
		assertNotNull(table);
	}

	public void testGetInvalidDataTableIsNullWhenNotProvided() {
		ObservationListPane pane = new ObservationListPane(
				"Test", validModel, null, true, visibleSeries,
				AnalysisType.RAW_DATA);
		assertNull(pane.getInvalidDataTable());
	}

	public void testValidTableModelRowCountMatchesObservations() {
		ObservationListPane pane = new ObservationListPane(
				"Test", validModel, null, true, visibleSeries,
				AnalysisType.RAW_DATA);
		assertEquals(2, pane.getValidDataTable().getModel().getRowCount());
	}

	public void testGetLastObSelectedIsNullInitially() {
		ObservationListPane pane = new ObservationListPane(
				"Test", validModel, null, true, visibleSeries,
				AnalysisType.RAW_DATA);
		assertNull(pane.getLastObSelected());
	}

	public void testConstructionWithInvalidDataModel() {
		List<InvalidObservation> invalidObs = new ArrayList<InvalidObservation>();
		invalidObs.add(new InvalidObservation("bad line", "parse error"));
		InvalidObservationTableModel invalidModel =
				new InvalidObservationTableModel(invalidObs);

		ObservationListPane pane = new ObservationListPane(
				"Test", validModel, invalidModel, false, visibleSeries,
				AnalysisType.RAW_DATA);
		assertNotNull(pane);
		assertNotNull(pane.getInvalidDataTable());
	}

	public void testGetInvalidDataTableNotNullWhenProvided() {
		List<InvalidObservation> invalidObs = new ArrayList<InvalidObservation>();
		invalidObs.add(new InvalidObservation("bad line", "parse error"));
		InvalidObservationTableModel invalidModel =
				new InvalidObservationTableModel(invalidObs);

		ObservationListPane pane = new ObservationListPane(
				"Test", validModel, invalidModel, false, visibleSeries,
				AnalysisType.RAW_DATA);
		assertEquals(1, pane.getInvalidDataTable().getModel().getRowCount());
	}

	public void testConstructionWithEmptyVisibleSeries() {
		Set<SeriesType> empty = new HashSet<SeriesType>();
		ObservationListPane pane = new ObservationListPane(
				"Test", validModel, null, true, empty,
				AnalysisType.RAW_DATA);
		assertNotNull(pane);
		assertNotNull(pane.getValidDataTable());
	}

	public void testAutoResizeFalseDoesNotThrow() {
		ObservationListPane pane = new ObservationListPane(
				"Test", validModel, null, false, visibleSeries,
				AnalysisType.RAW_DATA);
		assertNotNull(pane.getValidDataTable());
	}

	public void testPhasePlotAnalysisTypeDoesNotThrow() {
		ObservationListPane pane = new ObservationListPane(
				"Test", validModel, null, true, visibleSeries,
				AnalysisType.PHASE_PLOT);
		assertNotNull(pane);
	}
}
