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
import java.util.Set;

import org.aavso.tools.vstar.data.DateInfo;
import org.aavso.tools.vstar.data.Magnitude;
import org.aavso.tools.vstar.data.SeriesType;
import org.aavso.tools.vstar.data.ValidObservation;
import org.aavso.tools.vstar.exception.ObservationReadError;
import org.aavso.tools.vstar.input.AbstractObservationRetriever;
import org.aavso.tools.vstar.plugin.InputType;
import org.aavso.tools.vstar.plugin.ObservationSourcePluginBase;
import org.aavso.tools.vstar.ui.dialog.DoubleField;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.MultiEntryComponentDialog;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ObservationPlotModel;
import org.aavso.tools.vstar.util.comparator.JDComparator;

/**
 * This plug-in is an additive source of B-V observations if such bands exist in
 * the current dataset.
 */
public class BMinusVObservationSource extends ObservationSourcePluginBase {

	private SeriesType bvSeries;

	public BMinusVObservationSource() {
		super();
		isAdditive = true;
		bvSeries = SeriesType.create("B-V", "B-V", Color.MAGENTA, false, false);
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
		return new BMinusVRetriever();
	}

	@Override
	public InputType getInputType() {
		return InputType.NONE;
	}

	/**
	 * Request the time tolerance with which to compare a pair of B and V
	 * observations in order to determine whether to include them in the subset.
	 * 
	 * @return The tolerance in days or a fraction thereof, or null if the user
	 *         does not wish to specify a tolerance.
	 */
	private Double requestTimeTolerance() {
		Double tolerance = null;

		do {
			DoubleField timeToleranceField = new DoubleField(
					"Fraction of a day", null, 1.0, 1.0);

			List<ITextComponent<?>> fields = new ArrayList<ITextComponent<?>>();
			fields.add(timeToleranceField);

			MultiEntryComponentDialog dlg = new MultiEntryComponentDialog(
					"B,V Time Delta", fields);

			Double value = timeToleranceField.getValue();

			if (dlg.isCancelled()) {
				break;
			} else {
				tolerance = value;
			}
		} while (tolerance <= 0);

		return tolerance;
	}

	class BMinusVRetriever extends AbstractObservationRetriever {

		private List<ValidObservation> b;
		private List<ValidObservation> v;

		private int records;

		@Override
		public Integer getNumberOfRecords() throws ObservationReadError {

			Double tolerance = requestTimeTolerance();

			findBandVObsPairs(tolerance);

			records = b != null && v != null && b.size() != 0 && v.size() != 0 ? Math
					.min(b.size(), v.size()) : 0;

			return records;
		}

		@Override
		public void retrieveObservations() throws ObservationReadError,
				InterruptedException {

			if (records > 0) {
				for (int i = 0; i < records; i++) {
					// Note: simplifying assumption: B and V elements correspond
					// somewhat in time!
					double deltaMag = b.get(i).getMag() - v.get(i).getMag();
					double meanJD = b.get(i).getJD();
					ValidObservation bvOb = new ValidObservation();
					bvOb.setDateInfo(new DateInfo(meanJD));
					bvOb.setMagnitude(new Magnitude(deltaMag, 0));
					bvOb.setBand(bvSeries);
					collectObservation(bvOb);
				}
			}
			
			SeriesType bvtype = SeriesType.getSeriesFromDescription("B-V");
			
			if (validObservationCategoryMap.containsKey(bvtype)) {
				List<ValidObservation> bvobs = validObservationCategoryMap.get(bvtype);
				System.out.println(bvobs.size());
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
		 * Find the subset of adjacent-in-time B and V observation pairs with
		 * the same observer code and that fall within the optional time
		 * tolerance constraint.
		 * 
		 * @param tolerance
		 *            Time tolerance as a fraction of a day or null if no time
		 *            tolerance is to be applied.
		 */
		private void findBandVObsPairs(Double tolerance) {

			Mediator mediator = Mediator.getInstance();

			ObservationPlotModel model = mediator
					.getObservationPlotModel(AnalysisType.RAW_DATA);

			// Get all B and V observations into a sorted sequence.
			List<ValidObservation> bObs = model
					.getObservations(SeriesType.Johnson_B);

			List<ValidObservation> vObs = model
					.getObservations(SeriesType.Johnson_V);

			List<ValidObservation> bAndVObs = new ArrayList<ValidObservation>();

			bAndVObs.addAll(bObs);
			bAndVObs.addAll(vObs);
			bAndVObs.sort(JDComparator.instance);

			// Look for B and V pairs (with the same non-null observer code) and
			// select that subset, e.g.
			// V,V,V,V,V,V,B,B,V,B,V,V,V,B,B,V has the pairs-wise subset:
			// V,B,B,V,B,V, V,B,B,V
			List<ValidObservation> bAndVObsSubset = new ArrayList<ValidObservation>();

			for (int i = 0; i < bAndVObs.size() - 1; i += 2) {
				ValidObservation first = bAndVObs.get(i);
				ValidObservation second = bAndVObs.get(i + 1);

				if ((first.getBand() == SeriesType.Johnson_B && second
						.getBand() == SeriesType.Johnson_V)
						|| (first.getBand() == SeriesType.Johnson_V && second
								.getBand() == SeriesType.Johnson_B)) {

					if (first.getObsCode() != null
							&& second.getObsCode() != null
							&& first.getObsCode().equals(second.getObsCode())) {

						double delta = second.getJD() - first.getJD();
						if (tolerance == null || delta <= tolerance) {
							bAndVObsSubset.add(first);
							bAndVObsSubset.add(second);
						}
					}
				}
			}

			// Separate B and V pairs.
			b = new ArrayList<ValidObservation>();
			v = new ArrayList<ValidObservation>();

			for (int i = 0; i < bAndVObsSubset.size(); i++) {
				ValidObservation ob = bAndVObsSubset.get(i);

				if (ob.getBand() == SeriesType.Johnson_B) {
					b.add(ob);
				} else {
					v.add(ob);
				}
			}
		}
	}
}
