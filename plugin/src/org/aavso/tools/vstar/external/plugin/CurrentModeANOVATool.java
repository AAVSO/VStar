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

import java.util.ArrayList;
import java.util.List;

import org.aavso.tools.vstar.plugin.GeneralToolPluginBase;
import org.aavso.tools.vstar.ui.dialog.ITextComponent;
import org.aavso.tools.vstar.ui.dialog.TextArea;
import org.aavso.tools.vstar.ui.dialog.TextDialog;
import org.aavso.tools.vstar.ui.mediator.AnalysisType;
import org.aavso.tools.vstar.ui.mediator.Mediator;
import org.aavso.tools.vstar.ui.model.plot.ObservationAndMeanPlotModel;
import org.aavso.tools.vstar.util.stats.BinningResult;

/**
 * This tool computes and displays the ANOVA value for the currently displayed
 * plot's mean.
 */
public class CurrentModeANOVATool extends GeneralToolPluginBase {

	/**
	 * Constructor
	 */
	public CurrentModeANOVATool() {
	}

	@Override
	public void invoke() {
		Mediator mediator = Mediator.getInstance();

		if (!mediator.getNewStarMessageList().isEmpty()) {
			AnalysisType type = mediator.getAnalysisType();

			ObservationAndMeanPlotModel model = mediator
					.getObservationPlotModel(type);

			BinningResult binningResult = model.getBinningResult();

			String msg = binningResult.createAnovaText();

			List<ITextComponent<String>> fields = new ArrayList<ITextComponent<String>>();
			fields.add(new TextArea("ANOVA", msg));
			new TextDialog(
					"ANOVA for " + binningResult.getSeries() + " series",
					fields);
		}
	}

	@Override
	public String getDescription() {
		return "Compute ANOVA for current plot mode's mean series";
	}

	@Override
	public String getDisplayName() {
		return "Current Mode ANOVA";
	}
	
	/**
	 * @see org.aavso.tools.vstar.plugin.IPlugin#getDocName()
	 */
	@Override
	public String getDocName() {
		return "CurrentModeANOVA.pdf";
	}
	
}
