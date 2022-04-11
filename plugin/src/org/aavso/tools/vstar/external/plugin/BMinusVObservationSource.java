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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.swing.JPanel;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.plugin.PluginComponentFactory;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ObservationPlotModel;
import org.aavso.tools.vstar.util.Pair;
import org.aavso.tools.vstar.util.Tolerance;
import org.aavso.tools.vstar.util.comparator.JDComparator;
import org.apache.commons.math.stat.inference.TestUtils;

/**
 * This plug-in is an additive source of B-V observations if such bands exist in
 * the current dataset.
 */
public class BMinusVObservationSource extends ObservationSourcePluginBase {

	private Double testTimeTolerance;
	private List<ValidObservation> testVObs;
	private List<ValidObservation> testBObs;

	private SeriesType bvSeries;
	private Pair<TextArea, JPanel> velaFilterFieldAndPanel;
	private TextArea velaFilterField;

	public BMinusVObservationSource() {
		super();
		testTimeTolerance = null;
		testVObs = new ArrayList<ValidObservation>();
		testBObs = new ArrayList<ValidObservation>();
		isAdditive = true;
		bvSeries = SeriesType.create("B-V", "B-V", Color.MAGENTA, false, false);
		velaFilterFieldAndPanel = PluginComponentFactory.createVeLaFilterPane();
		velaFilterField = velaFilterFieldAndPanel.first;
	}

	@Override
	public String getDisplayName() {
		return "Create B-V series...";
	}

	@Override
	public String getDescription() {
		return "B-V series creator";
	}

	@Override
	public AbstractObservationRetriever getObservationRetriever() {
		return new BMinusVRetriever(requestTimeTolerance());
	}

	@Override
	public InputType getInputType() {
		return InputType.NONE;
	}

	/**
	 * Request the time tolerance with which to compare a pair of B and V
	 * observations in order to determine whether to include them in the subset.
	 * 
	 * @return The tolerance in days or a fraction thereof, or null if the user does
	 *         not wish to specify a tolerance.
	 */
	private Optional<Double> requestTimeTolerance() {
		Optional<Double> tolerance = Optional.empty();

		if (testTimeTolerance != null) {
			tolerance = Optional.of(testTimeTolerance);
			setVelaFilterStr("");
		} else {
			do {
				DoubleField timeToleranceField = new DoubleField("Tolerance (days)", null, 1.0, 1.0);

				List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();
				fields.add(timeToleranceField);

				MultiEntryComponentDialog dlg = new MultiEntryComponentDialog("B,V Time Delta", fields,
						Optional.of(velaFilterFieldAndPanel.second));

				Optional<Double> value = Optional.of(timeToleranceField.getValue());

				if (dlg.isCancelled()) {
					tolerance = Optional.empty();
					break;
				} else {
					tolerance = value;
					// Also, set the VeLa filter string.
					String str = velaFilterField.getValue().trim();
					setVelaFilterStr(str);
				}
			} while (tolerance.get() <= 0);
		}

		return tolerance;
	}

	class BMinusVRetriever extends AbstractObservationRetriever {

		private Optional<Double> tolerance;

		private List<ValidObservation> b;
		private List<ValidObservation> v;

		private Integer records;

		public BMinusVRetriever(Optional<Double> tolerance) {
			super(getVelaFilterStr());
			this.tolerance = tolerance;
		}

		@Override
		public Integer getNumberOfRecords() throws ObservationReadError {
			if (tolerance.isPresent()) {
				findBandVObsPairs(tolerance.get());

				records = b != null && v != null && b.size() != 0 && v.size() != 0 ? Math.min(b.size(), v.size()) : 0;
			} else {
				records = null;
			}

			return records;
		}

		@Override
		public void retrieveObservations() throws ObservationReadError, InterruptedException {

			if (records != null && records > 0) {
				for (int i = 0; i < records; i++) {
					double deltaMag = b.get(i).getMag() - v.get(i).getMag();
					double bUncertainty = b.get(i).getMagnitude().getUncertainty();
					double vUncertainty = v.get(i).getMagnitude().getUncertainty();
					double meanError = Math.sqrt(bUncertainty * bUncertainty + vUncertainty * vUncertainty);
					double meanJD = (b.get(i).getJD() + v.get(i).getJD()) / 2;

					ValidObservation bvOb = new ValidObservation();

					bvOb.setDateInfo(new DateInfo(meanJD));
					bvOb.setMagnitude(new Magnitude(deltaMag, meanError));
					String bObsCode = b.get(i).getObsCode();
					String vObsCode = v.get(i).getObsCode();
					String bvObsCode = bObsCode;
					if (!bObsCode.equals(vObsCode)) {
						bvObsCode = bObsCode + "," + vObsCode;
					}
					bvOb.setObsCode(bvObsCode);
					bvOb.setBand(bvSeries);

					// Set the record number to the earliest B or V observation,
					// so that sorting by record will maintain a reasonable
					// order, with B-V appearing between B and V in the list.
					bvOb.setRecordNumber(Math.min(b.get(i).getRecordNumber(), v.get(i).getRecordNumber()));

					// Setting the name is important at least for being able
					// to load observations again when saved as AAVSO Download
					// format saved files to be able to be loaded. Choose V
					// observation arbitrarily.
					bvOb.setName(v.get(i).getName());

					collectObservation(bvOb);
				}
			}
		}

		@Override
		public String getSourceType() {
			return "B-V";
		}

		@Override
		public String getSourceName() {
			return "B-V";
		}

		@Override
		public Set<SeriesType> seriesToExcludeWhenAdditive() {
			Set<SeriesType> series = new HashSet<SeriesType>();
			series.add(SeriesType.getSeriesFromDescription("B-V"));
			return series;
		}

		// Helpers

		/**
		 * Find the subset of adjacent-in-time B and V observation pairs with the same
		 * observer code and that fall within the optional time tolerance constraint.
		 * 
		 * @param tolerance Time tolerance as a fraction of a day or null if no time
		 *                  tolerance is to be applied.
		 */
		private void findBandVObsPairs(Double tolerance) {

			try {
				Mediator mediator = Mediator.getInstance();

				// Get all B and V observations into a sorted sequence.
				List<ValidObservation> bObs;
				List<ValidObservation> vObs;

				if (testTimeTolerance != null) {
					bObs = testBObs;
					vObs = testVObs;
				} else {
					ObservationPlotModel model = mediator.getObservationPlotModel(AnalysisType.RAW_DATA);
					bObs = model.getObservations(SeriesType.Johnson_B);
					vObs = model.getObservations(SeriesType.Johnson_V);
				}

				List<ValidObservation> bAndVObs = new ArrayList<ValidObservation>();

				bAndVObs.addAll(bObs);
				bAndVObs.addAll(vObs);
				bAndVObs.sort(JDComparator.instance);

				// Look for B and V pairs (with the same non-null observer code)
				// and
				// select that subset, e.g.
				// V,V,V,V,V,V,B,B,V,B,V,V,V,B,B,V has the pairs-wise subset:
				// V,B,B,V,B,V, V,B,B,V
				List<ValidObservation> bAndVObsSubset = new ArrayList<ValidObservation>();

				int i = 0;
				// The stopping condition of the iteration over B,V observations
				// is
				// two short of the end of the collection, since we examine the
				// next
				// two observations.
				while (i < bAndVObs.size() - 1) {
					ValidObservation first = bAndVObs.get(i);
					ValidObservation second = bAndVObs.get(i + 1);

					if ((first.getBand() == SeriesType.Johnson_B && second.getBand() == SeriesType.Johnson_V)
							|| (first.getBand() == SeriesType.Johnson_V && second.getBand() == SeriesType.Johnson_B)) {

						if (first.getObsCode() != null && second.getObsCode() != null
								&& first.getObsCode().equals(second.getObsCode())) {

							double delta = second.getJD() - first.getJD();
							if (tolerance == null || delta <= tolerance) {
								// We found a B,V or V,B pair with same observer
								// code and within the time tolerance requested.
								bAndVObsSubset.add(first);
								bAndVObsSubset.add(second);
							}

							// Whether the pair was within the time tolerance or
							// not, we need to move onto the next pair, skipping
							// the current pair.
							i += 2;
							continue;
						}
					}

					// We either didn't find a B,V or V,B pair or we found a pair whose members have
					// different observer codes. Either way, we advance past the first of the pair
					// only, since the second and subsequent observation may constitute a pair of
					// interest.
					i++;
				}

				// Separate B and V pairs.
				b = new ArrayList<ValidObservation>();
				v = new ArrayList<ValidObservation>();

				for (ValidObservation ob : bAndVObsSubset) {
					if (ob.getBand() == SeriesType.Johnson_B) {
						b.add(ob);
					} else {
						v.add(ob);
					}
				}
			} catch (Throwable t) {
				// b or v may be null; the caller needs to check
			}
		}
	}

	@Override
	public Boolean test() {
		testTimeTolerance = 1.0;

		String obsCode = "FOO";
		String targetName = "TU Mus";

		ValidObservation bOb1 = new ValidObservation();
		bOb1.setDateInfo(new DateInfo(2459645.1134785));
		bOb1.setMagnitude(new Magnitude(5, 0.001));
		bOb1.setObsCode(obsCode);
		bOb1.setBand(SeriesType.Johnson_B);
		bOb1.setName(targetName);
		testBObs.add(bOb1);

		ValidObservation vOb1 = new ValidObservation();
		vOb1.setDateInfo(new DateInfo(2459645.1134785));
		vOb1.setMagnitude(new Magnitude(4, 0.002));
		vOb1.setObsCode(obsCode);
		vOb1.setBand(SeriesType.Johnson_V);
		vOb1.setName(targetName);
		testVObs.add(vOb1);

		ValidObservation bOb2 = new ValidObservation();
		bOb2.setDateInfo(new DateInfo(2459646.2));
		bOb2.setMagnitude(new Magnitude(5, 0));
		bOb2.setObsCode(obsCode);
		bOb2.setBand(SeriesType.Johnson_B);
		bOb2.setName(targetName);
		testBObs.add(bOb2);

		ValidObservation vOb2 = new ValidObservation();
		vOb2.setDateInfo(new DateInfo(2459646.3));
		vOb2.setMagnitude(new Magnitude(4.5, 0));
		vOb2.setObsCode(obsCode);
		vOb2.setBand(SeriesType.Johnson_V);
		vOb2.setName(targetName);
		testVObs.add(vOb2);

		boolean success = true;

		try {
			// create B-V observations
			AbstractObservationRetriever retriever = getObservationRetriever();
			retriever.getNumberOfRecords(); // identifies B & V observations
			retriever.retrieveObservations();

			List<ValidObservation> bvObs = retriever.getValidObservations();

			success &= 2 == bvObs.size();

			ValidObservation ob1 = bvObs.get(0);
			success &= targetName.equals(ob1.getName());
			success &= 2459645.1134785 == ob1.getJD();
			success &= 1 == ob1.getMag();
			success &= Tolerance.areClose(0.002236, ob1.getMagnitude().getUncertainty(), 1e-6, true);
			success &= obsCode == ob1.getObsCode();
			success &= bvSeries == ob1.getBand();

			ValidObservation ob2 = bvObs.get(1);
			success &= targetName.equals(ob2.getName());
			success &= 2459646.25 == ob2.getJD();
			success &= 0.5 == ob2.getMag();
			success &= 0 == ob2.getMagnitude().getUncertainty();
			success &= obsCode == ob2.getObsCode();
			success &= bvSeries == ob2.getBand();
		} catch (Exception e) {
			success = false;
		}

		return success;
	}
}