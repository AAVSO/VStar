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
package org.aavso.tools.vstar.plugin.period.impl;

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase;
import org.aavso.tools.vstar.ui.dialog.NumberField;
import org.aavso.tools.vstar.util.period.wwz.WeightedWaveletZTransform;

/**
 * Weighted Wavelet Z Transform plugin abstract base class.
 */
abstract public class WeightedWaveletZTransformPluginBase extends
		PeriodAnalysisPluginBase {

	protected WeightedWaveletZTransform wwt;

	protected Double currDecay;
	protected Double currTimeDivisions;

	protected NumberField decayField;
	protected NumberField timeDivisionsField;

	/**
	 * Constructor
	 */
	public WeightedWaveletZTransformPluginBase() {
		super();
		wwt = null;
		currDecay = 0.01;
		currTimeDivisions = 50.0;
	}

	protected List<NumberField> createNumberFields(NumberField ... moreFields) {
		List<NumberField> fields = new ArrayList<NumberField>();

		for (NumberField field : moreFields) {
			fields.add(field);
		}
		
		decayField = new NumberField("Decay", null, null, currDecay);
		fields.add(decayField);

		timeDivisionsField = new NumberField("Time Divisions", null, null,
				currTimeDivisions);
		fields.add(timeDivisionsField);

		return fields;
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.PluginBase#getGroup()
	 */
	@Override
	public String getGroup() {
		return "WWZ";
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase#reset()
	 */
	@Override
	public void reset() {
		currDecay = 0.001;
		currTimeDivisions = 50.0;
	}

	/**
	 * @see org.aavso.tools.vstar.plugin.period.PeriodAnalysisPluginBase#interrupt()
	 */
	@Override
	public void interrupt() {
		wwt.interrupt();
	}
}
