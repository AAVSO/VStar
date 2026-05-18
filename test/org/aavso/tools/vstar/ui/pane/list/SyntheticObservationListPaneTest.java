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
import java.util.List;
import java.util.Locale;

import javax.swing.JTable;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.ui.model.list.RawDataMeanObservationTableModel;

import junit.framework.TestCase;

/**
 * Pure unit tests for {@link SyntheticObservationListPane}.
 *
 * Uses {@link RawDataMeanObservationTableModel} as the concrete synthetic
 * table model type. Construction only calls {@code Mediator.getInstance()}
 * (safe) and creates a plain {@code JPanel}, so no display is needed.
 *
 * Part of issue #579 (prong C): GUI code coverage.
 */
public class SyntheticObservationListPaneTest extends TestCase {

	private ValidObservation ob1;
	private ValidObservation ob2;
	private List<ValidObservation> observations;
	private RawDataMeanObservationTableModel model;

	@Override
	protected void setUp() {
		Locale.setDefault(Locale.ENGLISH);

		ob1 = new ValidObservation();
		ob1.setDateInfo(new DateInfo(2451545.0));
		ob1.setMagnitude(new Magnitude(5.5, 0.01));

		ob2 = new ValidObservation();
		ob2.setDateInfo(new DateInfo(2451546.0));
		ob2.setMagnitude(new Magnitude(6.0, 0.02));

		observations = new ArrayList<ValidObservation>(Arrays.asList(ob1, ob2));
		model = new RawDataMeanObservationTableModel(observations);
	}

	public void testConstructionDoesNotThrow() {
		SyntheticObservationListPane<RawDataMeanObservationTableModel> pane =
				new SyntheticObservationListPane<RawDataMeanObservationTableModel>(
						model, "Mean observations");
		assertNotNull(pane);
	}

	public void testConstructionWithNullSummaryDoesNotThrow() {
		SyntheticObservationListPane<RawDataMeanObservationTableModel> pane =
				new SyntheticObservationListPane<RawDataMeanObservationTableModel>(
						model, null);
		assertNotNull(pane);
	}

	public void testGetObsTableModelReturnsSameInstance() {
		SyntheticObservationListPane<RawDataMeanObservationTableModel> pane =
				new SyntheticObservationListPane<RawDataMeanObservationTableModel>(
						model, "Mean observations");
		assertSame(model, pane.getObsTableModel());
	}

	public void testGetObsTableIsNotNull() {
		SyntheticObservationListPane<RawDataMeanObservationTableModel> pane =
				new SyntheticObservationListPane<RawDataMeanObservationTableModel>(
						model, "Mean observations");
		JTable table = pane.getObsTable();
		assertNotNull(table);
	}

	public void testObsTableRowCountMatchesObservations() {
		SyntheticObservationListPane<RawDataMeanObservationTableModel> pane =
				new SyntheticObservationListPane<RawDataMeanObservationTableModel>(
						model, "Mean observations");
		assertEquals(2, pane.getObsTable().getModel().getRowCount());
	}

	public void testGetLastObSelectedIsNullInitially() {
		SyntheticObservationListPane<RawDataMeanObservationTableModel> pane =
				new SyntheticObservationListPane<RawDataMeanObservationTableModel>(
						model, "Mean observations");
		assertNull(pane.getLastObSelected());
	}

	public void testConstructionWithEmptyObservations() {
		RawDataMeanObservationTableModel emptyModel =
				new RawDataMeanObservationTableModel(
						new ArrayList<ValidObservation>());
		SyntheticObservationListPane<RawDataMeanObservationTableModel> pane =
				new SyntheticObservationListPane<RawDataMeanObservationTableModel>(
						emptyModel, "Empty");
		assertNotNull(pane);
		assertEquals(0, pane.getObsTable().getModel().getRowCount());
	}

	public void testObsTableModelMatchesExpectedRowCount() {
		SyntheticObservationListPane<RawDataMeanObservationTableModel> pane =
				new SyntheticObservationListPane<RawDataMeanObservationTableModel>(
						model, "Mean observations");
		assertEquals(model.getRowCount(),
				pane.getObsTable().getModel().getRowCount());
	}
}
