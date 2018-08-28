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
package org.aavso.tools.vstar.example.plugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.AlgorithmError;
import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisComponentFactory;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisDialogBase;
import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.ui.NamedComponent;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.mediator.message.NewStarMessage;
import org.aavso.tools.vstar.ui.mediator.message.PeriodAnalysisSelectionMessage;
import org.aavso.tools.vstar.util.model.Harmonic;
import org.aavso.tools.vstar.util.model.PeriodAnalysisDerivedMultiPeriodicModel;
import org.aavso.tools.vstar.util.notification.Listener;
import org.aavso.tools.vstar.util.period.IPeriodAnalysisAlgorithm;
import org.aavso.tools.vstar.util.period.PeriodAnalysisCoordinateType;
import org.aavso.tools.vstar.util.period.dcdft.PeriodAnalysisDataPoint;

/**
 * VStar period analysis plug-in test.
 * 
 * This plug-in generates random periods/powers and shows them on a line plot,
 * in a table, with the selected period displayed in a label component. A new
 * phase plot can be generated with that period.
 */
public class PeriodAnalysisPluginTest extends PeriodAnalysisPluginBase {

	final private int N = 100;

	private double period;

	protected final static String NAME = "Period Analysis Plugin Test 1";

	class TestAlgorithm implements IPeriodAnalysisAlgorithm {
		private List<Double> domain;
		private List<Double> range;

		@Override
		public String getRefineByFrequencyName() {
			return "None";
		}

		@Override
		public Map<PeriodAnalysisCoordinateType, List<Double>> getResultSeries() {

			Map<PeriodAnalysisCoordinateType, List<Double>> values = new HashMap<PeriodAnalysisCoordinateType, List<Double>>();
			values.put(PeriodAnalysisCoordinateType.PERIOD, domain);
			values.put(PeriodAnalysisCoordinateType.POWER, range);

			return values;
		}

		@Override
		public Map<PeriodAnalysisCoordinateType, List<Double>> getTopHits() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void multiPeriodicFit(List<Harmonic> harmonics,
				PeriodAnalysisDerivedMultiPeriodicModel model)
				throws AlgorithmError {
			// TODO Auto-generated method stub
		}

		@Override
		public List<PeriodAnalysisDataPoint> refineByFrequency(
				List<Double> freqs, List<Double> variablePeriods,
				List<Double> lockedPeriod) throws AlgorithmError {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void execute() throws AlgorithmError {
			// Create a set of random values to be plotted. A real plug-in would
			// instead apply some algorithm to the observations.
			domain = new ArrayList<Double>();
			range = new ArrayList<Double>();
			for (int i = 0; i < N; i++) {
				domain.add((double) i);
				range.add(Math.random());
			}
		}

		@Override
		public void interrupt() {
			// Here you would normally set a flag that the execute method checks
			// to determine whether to exit a potentially long-running loop.
		}
	}

	private IPeriodAnalysisAlgorithm algorithm;

	@Override
	public void executeAlgorithm(List<ValidObservation> obs)
			throws AlgorithmError {

		algorithm = new TestAlgorithm();
		algorithm.execute();
	}

	@Override
	public String getDescription() {
		return "Period Analysis Plugin Test: generates random powers for periods.";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase#getGroup()
	 */
	@Override
	public String getGroup() {
		return "Test";
	}

	@Override
	public JDialog getDialog(SeriesType seriesType) {
		return new PeriodAnalysisDialog();
	}

	@Override
	public String getDisplayName() {
		return NAME;
	}

	@Override
	protected void newStarAction(NewStarMessage msg) {
		// Nothing to do
	}

	@SuppressWarnings("serial")
	class PeriodAnalysisDialog extends PeriodAnalysisDialogBase {
		PeriodAnalysisDialog() {
			super(NAME, false, true, false);
			prepareDialog();
			this.setNewPhasePlotButtonState(false);
		}

		@Override
		protected Component createContent() {
			// Random plot.
			Component plot = PeriodAnalysisComponentFactory.createLinePlot(
					"Random Periods", "", algorithm.getResultSeries(),
					PeriodAnalysisCoordinateType.PERIOD,
					PeriodAnalysisCoordinateType.POWER, true, false);

			// Data table.
			PeriodAnalysisCoordinateType[] columns = {
					PeriodAnalysisCoordinateType.PERIOD,
					PeriodAnalysisCoordinateType.POWER };

			Component table = PeriodAnalysisComponentFactory.createDataTable(
					columns, algorithm.getResultSeries(), algorithm);

			// Random period label component.
			JPanel randomPeriod = new RandomPeriodComponent(this);

			// Return tabbed pane of plot and period display component.
			return PluginComponentFactory.createTabs(new NamedComponent("Plot",
					plot), new NamedComponent("Data", table),
					new NamedComponent("Random Period", randomPeriod));
		}

		// Send a period change message when the new-phase-plot button
		// is clicked.
		@Override
		protected void newPhasePlotButtonAction() {
			sendPeriodChangeMessage(period);
		}

		@Override
		protected void findHarmonicsButtonAction() {
			// TODO Auto-generated method stub
		}

		@Override
		public void startup() {
			// TODO Auto-generated method stub
		}

		@Override
		public void cleanup() {
			// TODO Auto-generated method stub
		}
	}

	/**
	 * This class simply shows the currently selected (from plot or table) and
	 * updates the period member to be used when the new-phase-plot button is
	 * clicked. It's not really necessary, just shows a custom GUI component.
	 */
	@SuppressWarnings("serial")
	class RandomPeriodComponent extends JPanel implements
			Listener<PeriodAnalysisSelectionMessage> {

		private JLabel label;
		private PeriodAnalysisDialog dialog;

		public RandomPeriodComponent(PeriodAnalysisDialog dialog) {
			super();
			this.dialog = dialog;
			label = new JLabel("Period: None selected");
			this.add(label, BorderLayout.CENTER);

			Mediator.getInstance().getPeriodAnalysisSelectionNotifier()
					.addListener(this);
		}

		// Period analysis selection update handler methods.
		public void update(PeriodAnalysisSelectionMessage msg) {
			if (msg.getSource() != this) {
				try {
					period = msg.getDataPoint().getPeriod();
					label.setText("Period: " + String.format("%1.4f", period));
					dialog.setNewPhasePlotButtonState(true);
					dialog.setFindHarmonicsButtonState(true);
				} catch (ArrayIndexOutOfBoundsException e) {
				}
			}
		}

		public boolean canBeRemoved() {
			return false;
		}
	}

	@Override
	public void reset() {
	}

	@Override
	public void interrupt() {
		algorithm.interrupt();
	}
}
